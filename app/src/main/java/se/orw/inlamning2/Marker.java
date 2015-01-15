package se.orw.inlamning2;

/**
 * Created by Marcus on 2014-10-13.
 * <p/>
 * Describes a Marker Object
 */
public class Marker {
    private double lat;
    private double lng;
    private String title;

    /**
     * Marker constructor
     *
     * @param lat   Latitude of the marker
     * @param lng   Longitude of the marker
     * @param title Title of the marker
     */
    public Marker(double lat, double lng, String title) {
        this.lat = lat;
        this.lng = lng;
        this.title = title;
    }

    /**
     * Get the latitude of the marker
     *
     * @return latitude
     */
    public double getLat() {
        return lat;
    }

    /**
     * Get the longitude of the marker
     *
     * @return longitude
     */
    public double getLng() {
        return lng;
    }

    /**
     * Get title of the marker
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }
}
