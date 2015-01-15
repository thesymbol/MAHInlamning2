package se.orw.inlamning2;

import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by Marcus on 2014-10-13.
 *
 * Handles different JSON strings
 */
public class JSONHandler {
    private Controller controller;

    /**
     * Sets the controller for the class
     *
     * @param controller The controller object
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * Get all members of a group's positions
     *
     * @param jsonObject   The JSON object to parse
     * @param currentName  The current username
     * @param group        The group to use
     * @param mapsFragment The mapsFragment
     */
    public void getMembersLocation(JSONObject jsonObject, String currentName, String group, MapsFragment mapsFragment) {
        try {
            mapsFragment.clearMapMarkers();
            if (jsonObject.get("type").equals("locations") && jsonObject.get("group").equals(group)) {
                JSONArray jsonArray = jsonObject.getJSONArray("location");
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject locationsJSONObject = jsonArray.getJSONObject(i);
                        if (currentName.equals(locationsJSONObject.getString("member")) && !mapsFragment.alreadyZoomed()) { // if its you zoom baby zoom
                            mapsFragment.zoomToMarker(locationsJSONObject.getDouble("latitude"), locationsJSONObject.getDouble("longitude"));
                        }
                        mapsFragment.addMapMarker(locationsJSONObject.getDouble("latitude"), locationsJSONObject.getDouble("longitude"), locationsJSONObject.getString("member"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            mapsFragment.updateMap();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse the groups received from the server
     *
     * @param jsonObject The JSON object to parse
     */
    public void getGroups(JSONObject jsonObject) {
        try {
            controller.clearGroupArray();
            if (jsonObject.get("type").equals("groups")) {
                JSONArray jsonArray = jsonObject.getJSONArray("groups");
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject groupJSONObject = jsonArray.getJSONObject(i);
                        controller.addToGroupArray(groupJSONObject.get("group").toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create's a JSON query for registering a member
     *
     * @param group The group to register to
     * @param name  The username to register
     * @return The JSON query created
     */
    public String register(String group, String name) {
        return jsonBase(new String[][]{
                {"type", "register"},
                {"group", group},
                {"member", name}
        });
    }

    /**
     * Create's a JSON query for unregister from server
     *
     * @param id The id from the server to unregister
     * @return The JSON query created
     */
    public String unregister(String id) {
        return jsonBase(new String[][]{
                {"type", "unregister"},
                {"id", id}
        });
    }

    /**
     * Create's a JSON query for getting members
     *
     * @param group The group to use
     * @return The JSON query created
     */
    public String members(String group) {
        return jsonBase(new String[][]{
                {"type", "members"},
                {"group", group}
        });
    }

    /**
     * Create's a JSON query for getting groups
     *
     * @return The JSON query created
     */
    public String groups() {
        return jsonBase(new String[][]{
                {"type", "groups"}
        });
    }

    /**
     * Create's a JSON query for current location
     *
     * @param id  The id from the server to use for your location
     * @param lat The current latitude
     * @param lng The current longitude
     * @return The JSON query created
     */
    public String location(String id, String lat, String lng) {
        return jsonBase(new String[][]{
                {"type", "location"},
                {"id", id},
                {"longitude", lng},
                {"latitude", lat}
        });
    }

    /**
     * Builds a JSON query with specified names and values.
     *
     * @param nameValues String[[Name, Value], [Name2, Value2]...]...
     * @return The JSON query created
     */
    private String jsonBase(String[][] nameValues) {
        String jsonQuery = "";
        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.beginObject();
            for (String[] nameValue : nameValues) {
                jsonWriter.name(nameValue[0]).value(nameValue[1]);
            }
            jsonWriter.endObject();
            jsonQuery = stringWriter.toString();
            jsonWriter.close();
            stringWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("Inlamning2.JSONHandler", jsonQuery);
        return jsonQuery;
    }

}
