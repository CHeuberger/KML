package cfh.kml.jaxb;

import java.util.Locale;

public class Coord {

    public static final double UNUSED = Double.NaN;
    private static final boolean isUnused(double value) { return Double.isNaN(value); }
    
    public static double hms(int h, int m, double s) {
        if (m < 0 || m >= 60) throw new IllegalArgumentException("minutes: " + m);
        if (s < 0 || s >= 60.0) throw new IllegalArgumentException("seconds: " + s);
        if (h >= 0)
            return h + (m + s / 60.0) / 60.0;
        else
            return h - (m + s / 60.0) / 60.0;
    }
    
    
    
    private double latitude = UNUSED;
    private double longitude = UNUSED;
    private double altitude = UNUSED;
    
    @SuppressWarnings("unused")
    private Coord() {
    }
    
    public Coord(double latitude, double longitude) {
        checkAndSetLongitude(longitude);
        checkAndSetLatitude(latitude);
        this.altitude = UNUSED;
    }
    
    public Coord(double latitude, double longitude, double altitude) {
        checkAndSetLongitude(longitude);
        checkAndSetLatitude(latitude);
        this.altitude = altitude;
    }
    
    public Coord(String text) {
        String[] tokens = text.split(",",3);
        if (tokens.length < 2)
            throw new IllegalArgumentException("wrong format: " + text);
        try {
            checkAndSetLongitude(Double.parseDouble(tokens[0]));
            checkAndSetLatitude(Double.parseDouble(tokens[1]));
            if (tokens.length == 3) {
                this.altitude = Double.parseDouble(tokens[2]);
            } else {
                this.altitude = UNUSED;
            }
        } catch (NumberFormatException ex) {
            throw (IllegalArgumentException) new IllegalArgumentException(ex.getMessage()).initCause(ex);
        }
    }
    
    public Coord relative(double lat, double lon, double alt) {
        if (isUnused(altitude))
            return new Coord(latitude+lat, longitude+lon, alt);
        else
            return new Coord(latitude+lat, longitude+lon, altitude+alt);
    }

    public String toKmlString() {
        if (isUnused(altitude))
            return String.format(Locale.US, "%9.6f,%9.6f", this.longitude, this.latitude);
        else
            return String.format(Locale.US, "%.6f,%.6f,%.6f", this.longitude, this.latitude, this.altitude);
    }

    private void checkAndSetLongitude(double lon) {
        if (lon < -180 || lon > 180 || Double.isNaN(lon))
            throw new IllegalArgumentException("longitude: " + lon);
        this.longitude = lon;
    }

    private void checkAndSetLatitude(double lat) {
        if (lat < -90 || lat > 90 || Double.isNaN(lat))
            throw new IllegalArgumentException("latitude: " + lat);
        this.latitude = lat;
    }
}
