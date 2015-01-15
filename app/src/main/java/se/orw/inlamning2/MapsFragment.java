package se.orw.inlamning2;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapsFragment extends Fragment {
    private GoogleMap googleMap;
    private ArrayList<Marker> markerArrayList = new ArrayList<Marker>();
    private int mapType = GoogleMap.MAP_TYPE_NORMAL;
    private boolean zoomed = false;
    private View view;

    public MapsFragment() {
        // Required empty public constructor
    }

    /**
     * Sets the map type for Google Maps
     *
     * @param mapType Google Maps mapType
     */
    public void setMapType(int mapType) {
        this.mapType = mapType;
    }

    /**
     * Add marker to Google Maps
     *
     * @param lat   The markers Latitude
     * @param lng   The markers Longitude
     * @param title The Title of the marker
     */
    public void addMapMarker(double lat, double lng, String title) {
        markerArrayList.add(new Marker(lat, lng, title));
    }

    /**
     * Remove all Google Map's markers
     */
    public void clearMapMarkers() {
        markerArrayList.clear();
        googleMap.clear();
    }

    /**
     * Zoom into specified latitude and longitude with a nice animation
     *
     * @param latitude  The latitude
     * @param longitude The longitude
     */
    public void zoomToMarker(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        googleMap.animateCamera(cameraUpdate);
        zoomed = true;
    }

    /**
     * If we have zoomed already
     *
     * @return If we have zoomed already returns true else false
     */
    public boolean alreadyZoomed() {
        return zoomed;
    }

    /**
     * Sets if we have zoomed already or not
     *
     * @param zoomed The state of the zoom (true = zoomed, false = not zoomed).
     */
    public void setZoomed(boolean zoomed) {
        this.zoomed = zoomed;
    }

    /**
     * Updates the Google Maps markers
     */
    public void updateMap() {
        Marker marker;
        MarkerOptions markerOptions;
        for (Marker aMarkerArrayList : markerArrayList) {
            marker = aMarkerArrayList;
            markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(marker.getLat(), marker.getLng()));
            markerOptions.title(marker.getTitle());
            googleMap.addMarker(markerOptions);
        }
    }

    /**
     * Init the Fragment
     */
    private void init() {
        setRetainInstance(true);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        googleMap = mapFragment.getMap();
        googleMap.setMapType(mapType);
        updateMap();
    }

    /**
     * Create the actuall view
     *
     * @param inflater           -
     * @param container          -
     * @param savedInstanceState -
     * @return -
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_maps, container, false);
        }
        init();
        return view;
    }
}
