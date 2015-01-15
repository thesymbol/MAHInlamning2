package se.orw.inlamning2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

/**
 * Network controller
 * <p/>
 * Created by Marcus on 2014-10-13.
 */
public class NetworkController extends Service {
    // server configuration
    private static final String ip = "195.178.232.7";
    private static final int port = 7117;
    private RunOnThread thread;
    private Receive receive = null;
    private Buffer<String> receiveBuffer; // Buffer for received items.
    private DataInputStream input;
    private DataOutputStream output;
    private Socket socket;
    private static boolean connected = false;

    /**
     * run once the service is started.
     *
     * @param intent  -
     * @param flags   -
     * @param startId -
     * @return -
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread = new RunOnThread();
        receiveBuffer = new Buffer<String>();
        return Service.START_STICKY;
    }

    /**
     * Binds the service.
     *
     * @param intent -
     * @return -
     */
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalService();
    }

    /**
     * init first connection to server
     */
    public void connect() {
        if (isOnline(this)) {
            if(!connected) {
                thread.start();
                thread.execute(new Connect());
            }
        } else {
            Log.d("Inlamning2.NetworkController", "No network connection available");
        }
    }

    /**
     * disconnect from the server
     */
    public void disconnect() {
        if (isConnected()) {
            thread.execute(new Disconnect());
        }
    }

    /**
     * send data to server
     *
     * @param data A JSON query to send to the server
     */
    public void send(String data) {
        Log.d("Inlamning2.NetworkController", "Trying to send: " + data);
        thread.execute(new Send(data));
    }

    /**
     * Get what the server responded
     *
     * @return get the JSON response from the server
     * @throws InterruptedException
     */
    public String receive() throws InterruptedException {
        return receiveBuffer.get();
    }

    /**
     * Check if the user is connected
     *
     * @return true if connected else false
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * To allow the service to remain intact during activity restarts
     */
    public class LocalService extends Binder {
        public NetworkController getService() {
            return NetworkController.this;
        }
    }

    /**
     * Returns true if the device is online (has network connection)
     *
     * @param context The context
     * @return True if there is internet connection else false
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Receive objects from server.
     */
    private class Receive extends Thread {
        public void run() {
            String result;
            try {
                while (receive != null) {
                    result = input.readUTF();
                    receiveBuffer.put(result);
                }
            } catch (Exception e) {
                receive = null;
            }
        }
    }

    /**
     * Connects to the server and init's the receive listener
     */
    private class Connect implements Runnable {
        public void run() {
            try {
                socket = new Socket(ip, port);
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
                output.flush();
                receiveBuffer.put("CONNECTED");
                receive = new Receive();
                receive.start();
                connected = true;
                Log.d("Inlamning2.NetworkController.Connect", "Connected to the server");
            } catch (Exception e) {
                connected = false;
                e.printStackTrace();
                Log.d("Inlamning2.NetworkController.Connect", "Error could not connect to server");
                receiveBuffer.put("EXCEPTION");
            }
        }
    }

    /**
     * Disconnects the client from the server
     */
    private class Disconnect implements Runnable {
        public void run() {
            try {
                if (input != null) { // close input stream
                    input.close();
                }
                if (output != null) { // close output stream
                    output.close();
                }
                if (socket != null) { // close socket
                    socket.close();
                }
                thread.stop(); // stop the service main thread
                connected = false;
                receiveBuffer.put("CLOSED");
            } catch (IOException e) {
                e.printStackTrace();
                receiveBuffer.put("EXCEPTION");
            }
        }
    }

    /**
     * Send data to the server.
     */
    private class Send implements Runnable {
        private String data;

        public Send(String data) {
            this.data = data;
        }

        public void run() {
            try {
                if(output != null) {
                    output.writeUTF(data);
                    output.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                receiveBuffer.put("EXCEPTION");
            }
        }
    }

    /**
     * Handles all the network threads
     */
    private class RunOnThread {
        private Buffer<Runnable> buffer = new Buffer<Runnable>();
        private Worker worker;

        public void start() {
            if (worker == null) {
                worker = new Worker();
                worker.start();
            }
        }

        public void stop() {
            if (worker != null) {
                worker.interrupt();
                worker = null;
            }
        }

        public void execute(Runnable runnable) {
            buffer.put(runnable);
        }

        private class Worker extends Thread {
            public void run() {
                Runnable runnable;
                while (worker != null) {
                    try {
                        runnable = buffer.get();
                        runnable.run();
                    } catch (InterruptedException e) {
                        worker = null;
                    }
                }
            }
        }
    }

    /**
     * Buffer if the app rotates/restarts
     *
     * @param <T>
     */
    private class Buffer<T> {
        private LinkedList<T> buffer = new LinkedList<T>();

        public synchronized void put(T element) {
            buffer.addLast(element);
            notifyAll();
        }

        public synchronized T get() throws InterruptedException {
            while (buffer.isEmpty()) {
                wait();
            }
            return buffer.removeFirst();
        }
    }
}
