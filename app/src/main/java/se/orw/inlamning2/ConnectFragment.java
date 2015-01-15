package se.orw.inlamning2;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectFragment extends Fragment {
    private EditText etGroup;
    private EditText etUsername;
    private ArrayAdapter<String> adapter;
    private Controller controller;
    private View view;

    public ConnectFragment() {
        // Required empty public constructor
    }

    /**
     * Sets the controller for the class
     *
     * @param controller The controller object
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * Set the adapter for the spinner list
     *
     * @param adapter The adapter
     */
    public void setAdapter(ArrayAdapter<String> adapter) {
        this.adapter = adapter;
    }

    /**
     * Notify the Fragment to update the adapters data
     */
    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    /**
     * Set the text of the group EditText
     *
     * @param group The group name
     */
    public void setEtGroup(String group) {
        etGroup.setText(group);
    }

    /**
     * Init the Fragment
     */
    private void init() {
        setRetainInstance(true);
        etGroup = (EditText) view.findViewById(R.id.etGroup);
        etUsername = (EditText) view.findViewById(R.id.etUsername);

        adapter.notifyDataSetChanged();

        Spinner spinnerGroup = (Spinner) view.findViewById(R.id.spinnerGroup);
        spinnerGroup.setAdapter(adapter);
        spinnerGroup.setOnItemSelectedListener(new SpinnerListListener());

        Button btnGetGroups = (Button) view.findViewById(R.id.btnGetGroups);
        btnGetGroups.setOnClickListener(new GetGroupsListener());

        Button btnConnect = (Button) view.findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new ConnectListener());

        Button btnDisconnect = (Button) view.findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(new DisconnectListener());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_connect, container, false);
        init();
        return view;
    }

    /**
     * Handles the dropdown list selection
     */
    private class SpinnerListListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
            controller.spinnerSelect(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    /**
     * Handles the get groups button
     */
    private class GetGroupsListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            controller.updateGroups();
        }
    }

    /**
     * Handles the connect button
     */
    private class ConnectListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            controller.networkConnect();
            controller.connectAs(etGroup.getText().toString(), etUsername.getText().toString());
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);//hide keyboard when you press connect
            controller.switchToMap();
        }
    }

    /**
     * Handles the disconnect button
     */
    private class DisconnectListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            controller.disconnect();
        }
    }
}
