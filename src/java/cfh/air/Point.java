package cfh.air;

import static java.lang.Math.*;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class Point implements Segment {

    /** Error for comparing points. */
    private static final double ERR = 1E-5;
    
    /** degrees */
    private final double latitude;
    /** degrees */
    private final double longitude;
    
    /**
     * @param latitude degrees
     * @param longitude degrees
     */
    public Point(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) throw new IllegalArgumentException("invalid latitude: " + latitude);
        if (longitude < -180 || longitude > 180) throw new IllegalArgumentException("invalid longitude: " + longitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    /**
     * Initial bearing to another point.
     * @param point destination point
     * @return angle from this point to the given point in radians, 0 is north, PI/2 is east
     */
    public double angleTo(Point point) {
        double dx = cos(toRadians(this.latitude)) * (point.longitude - this.longitude);
        double dy = point.latitude - this.latitude;
        while (dx < -180) dx += 360;
        while (dx > 180) dx -= 360;
        double angle = atan2(-dx, dy);
        if (angle < 0) {
            angle += 2 * PI;
        }
        return angle;
    }
    
    /**
     * @param point 
     * @return <i>distance</i> between the two points in degrees
     */
    public double distTo(Point point) {
        return toDegrees(acos(sin(toRadians(latitude))*sin(toRadians(point.latitude))+
                cos(toRadians(latitude))*cos(toRadians(point.latitude))
                        *cos(toRadians(longitude)-toRadians(point.longitude))));
    }
    
    /**
     * @param dist in degrees
     * @param angle in radians
     * @return
     */
    public Point polar(double dist, double angle) {
        return new Point(latitude + dist*cos(angle), 
                longitude - dist*sin(angle)/cos(toRadians(latitude)));
    }
    
    @Override
    public List<Point> getPoints(int step) {
        if (step < 0) throw new IllegalArgumentException("invalid step: " + step);
        
        return Collections.singletonList(this);
    }
    
    @Override
    public Rectangle2D getBound() {
        return new Rectangle2D.Double(longitude, latitude, 0, 0);
    }
    
    @Override
    public Point draw(Graphics2D gg, Point last) {
        if (last != null) {
            gg.draw(new Line2D.Double(last.getLongitude(), last.getLatitude(), longitude, latitude));
        }
        return this;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        
        Point other = (Point) obj;
        return abs(other.latitude-this.latitude) < ERR && abs(other.longitude-this.longitude) < ERR;
    }

    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(latitude);
        bits ^= Double.doubleToLongBits(longitude);
        return (int)(bits ^ (bits >>> 32));
    }
    
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "P(%f,%f)", latitude, longitude);
    }
}
