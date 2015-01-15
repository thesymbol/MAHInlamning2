package se.orw.inlamning2;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Controller for app
 * <p/>
 * Created by Marcus on 2014-10-13.
 */
public class Controller {
    // network related variables
    private NetworkController networkController;
    private ServiceConnection serviceConnection;
    private ReceiveListener receiveListener;
    private boolean bound = false;
    // Location related variables
    private LocationManager locationManager;
    private NetGPSLocationListener netGPSLocationListener;
    private JSONHandler jsonHandler;
    // other variables
    private Activity activity;
    private MapsFragment mapsFragment;
    private ConnectFragment connectFragment;
    private ArrayList<String> groups = new ArrayList<String>();
    private String currentGroup = "";
    private String currentName = "";
    private String currentID = "";
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    /**
     * Controller constructor
     *
     * @param activity           The main activity
     * @param savedInstanceState -
     */
    public Controller(Activity activity, Bundle savedInstanceState) {
        this.activity = activity;
        init(savedInstanceState);
    }

    /**
     * Switch to specified fragment
     *
     * @param fragment The fragment to switch to
     * @param tag      The tag for the fragment
     */
    public void switchToFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (tag.equals("")) {
            transaction.replace(R.id.activity_main, fragment);
        } else {
            transaction.replace(R.id.activity_main, fragment, tag);
        }
        transaction.commit();

    }

    /**
     * Called once the application is resumed.
     */
    public void onResume() {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, netGPSLocationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, netGPSLocationListener);
    }

    /**
     * Called once the application is going to pause
     */
    public void onPause() {
        locationManager.removeUpdates(netGPSLocationListener);
    }

    /**
     * Called once the application is destroyed
     */
    public void onDestroy() {
        if (bound) { // unbind service if its bound when we destroy the application
            activity.unbindService(serviceConnection);
            receiveListener.stopListener();
            bound = false;
        }
    }

    /**
     * Connect to the server
     */
    public void networkConnect() {
        networkController.connect();
    }

    /**
     * Connect to the server with specified group and username
     *
     * @param group The group to register (leave to "" to not register)
     * @param name  The username to register (leave to "" to not register)
     */
    public void connectAs(String group, String name) {
        if (!group.equals("") && !name.equals("")) {
            if (currentID.equals("")) { // if we are not logged in already to the server
                currentGroup = group;
                currentName = name;
                networkController.send(jsonHandler.register(group, name));
                Log.d("Inlamning2.Controller.connectAs", "Registering...");
                Toast.makeText(activity, R.string.registerNewUser, Toast.LENGTH_SHORT).show();
            }
            if (!currentGroup.equals(group)) { // if we switch group
                currentGroup = group;
                currentName = name;
                networkController.send(jsonHandler.unregister(currentID));
                networkController.send(jsonHandler.register(group, name));
                Log.d("Inlamning2.Controller.connectAs", "Switching group...");
                Toast.makeText(activity, R.string.switchGroup, Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("Inlamning2.Controller.connectAs", "One or more fields empty...");
            Toast.makeText(activity, R.string.emptyFields, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Disconnect from the server.
     */
    public void disconnect() {
        if(networkController.isConnected()) {
            networkController.send(jsonHandler.unregister(currentID));
            currentGroup = "";
            currentName = "";
            currentID = "";
            mapsFragment.clearMapMarkers();
            mapsFragment.setZoomed(false);
            networkController.disconnect();
        }
    }

    /**
     * Send request to update the groups spinner
     */
    public void updateGroups() {
        networkConnect();
        networkController.send(jsonHandler.groups());
    }

    /**
     * Called when spinner item is selected
     *
     * @param position The spinner item's position
     */
    public void spinnerSelect(int position) {
        connectFragment.setEtGroup(groups.get(position));
    }

    public void switchToMap() {
        switchToFragment(mapsFragment, "");
    }

    /**
     * Empties the group array
     */
    public void clearGroupArray() {
        groups.clear();
    }

    /**
     * Add item to the group's array
     *
     * @param group The group name to add to the array
     */
    public void addToGroupArray(String group) {
        groups.add(group);
    }

    /**
     * Syncs the state the = button should be in according to the navigation drawer position
     */
    public void onPostCreate() {
        drawerToggle.syncState();
    }

    /**
     * Once the app changes configuration (eg rotation).
     *
     * @param newConfig The config
     */
    public void onConfigurationChanged(Configuration newConfig) {
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Handles the drawer on the left sides button
     *
     * @param item Item in the menu
     * @return true if item is selected else false
     */
    public boolean onDrawerToggle(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item);
    }

    /**
     * Inits the left menu
     */
    private void initMenu() {
        String[] menu = {activity.getResources().getString(R.string.map), activity.getResources().getString(R.string.connect)};
        drawerLayout = (DrawerLayout) activity.findViewById(R.id.layout_drawer);
        ListView drawerList = (ListView) activity.findViewById(R.id.left_drawer);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        drawerList.setAdapter(new ArrayAdapter<String>(activity, R.layout.drawer_list_item, menu));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    switchToFragment(mapsFragment, "");
                } else if (position == 1) {
                    switchToFragment(connectFragment, "");
                }
                drawerLayout.closeDrawers();
            }
        });
        drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, R.drawable.ic_drawer, R.string.drawerOpen, R.string.drawerClose);
        drawerLayout.setDrawerListener(drawerToggle);
        if (activity.getActionBar() != null) {
            activity.getActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getActionBar().setHomeButtonEnabled(true);
        }
    }

    /**
     * Initialize the controller variables
     *
     * @param savedInstanceState The saved state from activity
     */
    private void init(Bundle savedInstanceState) {
        // start the intent to start networkController
        Intent intent = new Intent(activity, NetworkController.class);
        if (savedInstanceState == null) {
            activity.startService(intent);
        }
        serviceConnection = new ServiceConn();
        boolean result = activity.bindService(intent, serviceConnection, 0);
        if (!result) {
            Log.d("Inlamning2.Controller", "No binding");
            Toast.makeText(activity, R.string.noBinding, Toast.LENGTH_SHORT).show();
        }

        jsonHandler = new JSONHandler();
        jsonHandler.setController(this);

        // get initial gps location
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        netGPSLocationListener = new NetGPSLocationListener();

        mapsFragment = new MapsFragment();
        mapsFragment.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        connectFragment = new ConnectFragment();
        connectFragment.setController(this);
        connectFragment.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, groups));

        initMenu();

        switchToFragment(mapsFragment, "mapsFragment");
        switchToFragment(connectFragment, "connectFragment");
    }

    /**
     * The networkController service class
     */
    private class ServiceConn implements ServiceConnection {
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            NetworkController.LocalService localService = (NetworkController.LocalService) binder;
            networkController = localService.getService();
            bound = true;
            receiveListener = new ReceiveListener();
            receiveListener.start();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    }

    /**
     * Receiver Listener to receive data from server to be parsed
     */
    private class ReceiveListener extends Thread {
        public void stopListener() {
            interrupt();
            receiveListener = null;
        }

        public void run() {
            String message;
            while (receiveListener != null) {
                try {
                    message = networkController.receive();
                    Log.d("Inlamning2.Controller.ReceiveListener", message);
                    if (message.equals("EXCEPTION") && networkController.isConnected()) {
                        activity.runOnUiThread(new SendToast(R.string.reconnecting));
                        Toast.makeText(activity, R.string.reconnecting, Toast.LENGTH_SHORT).show();
                        networkController.disconnect();
                        networkConnect();
                        connectAs(currentGroup, currentName);
                    } else if (message.equals("CONNECTED")) {
                        activity.runOnUiThread(new SendToast(R.string.connected));
                    } else if (message.equals("CLOSED")) {
                        activity.runOnUiThread(new SendToast(R.string.disconnected));
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(message);
                            String type = jsonObject.getString("type");
                            if (type.equals("locations")) {
                                Log.d("Inlamning2.Controller.ReceiveListener", "locations");
                                activity.runOnUiThread(new UpdateMapUI(jsonObject)); // updates the map with specified markers from jsonObject
                            }
                            if (type.equals("register")) {
                                Log.d("Inlamning2.Controller.ReceiveListener", "register");
                                currentID = jsonObject.getString("id");
                                if (!currentID.equals("")) {
                                    Location temp = netGPSLocationListener.getLastLocation();
                                    networkController.send(jsonHandler.location(currentID, "" + temp.getLatitude(), "" + temp.getLongitude()));
                                }
                            }
                            if (type.equals("groups")) {
                                Log.d("Inlamning2.Controller.ReceiveLister", "groups");
                                activity.runOnUiThread(new UpdateGroupUI(jsonObject));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    receiveListener = null;
                }
            }
        }
    }

    /**
     * Run this class on UI thread to update the map markers
     */
    private class UpdateMapUI implements Runnable {
        private JSONObject jsonObject;

        public UpdateMapUI(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public void run() {
            try {
                jsonHandler.getMembersLocation(jsonObject, currentName, currentGroup, mapsFragment);
                Toast.makeText(activity, R.string.updatingMap, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a toast message to be used on the UI thread (only Toast.LENGTH_SHORT).
     */
    private class SendToast implements Runnable {
        private int stringsID;

        public SendToast(int stringsID) {
            this.stringsID = stringsID;
        }

        public void run() {
            Toast.makeText(activity, stringsID, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Run this class on UI thread to update the groups spinner content
     */
    private class UpdateGroupUI implements Runnable {
        private JSONObject jsonObject;

        public UpdateGroupUI(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public void run() {
            jsonHandler.getGroups(jsonObject);
            connectFragment.notifyDataSetChanged();
        }
    }

    /**
     * Updates location based on gps and network location services.
     */
    private class NetGPSLocationListener implements LocationListener {
        private Location lastLocation;

        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
            if (!currentID.equals("")) {
                networkController.send(jsonHandler.location(currentID, "" + location.getLatitude(), "" + location.getLongitude()));
            }
        }

        @Override
        public void onStatusChanged(String s, int status, Bundle bundle) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("Inlamning2.Controller.NetworkLocationListener", "Network location available again");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("Inlamning2.Controller.NetworkLocationListener", "Network location out of service");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("Inlamning2.Controller.NetworkLocationListener", "Network location temporarily unavailable");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d("Inlamning2.Controller.NetworkLocationListener", "Network Provider Enabled");
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d("Inlamning2.Controller.NetworkLocationListener", "Network Provider disabled");
        }

        public Location getLastLocation() {
            return lastLocation;
        }
    }
}
