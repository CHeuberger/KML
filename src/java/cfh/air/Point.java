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
    private static final double ERR = 1E-6;
    
    /** degrees */
    private final double latitude;
    /** degrees */
    private final double longitude;
    
    /**
     * @param latitude degrees
     * @param longitude degrees
     */
    public Point(double latitude, double longitude) {
//        while (latitude < -270) latitude += 360;
//        while (latitude > 270) latitude -= 360;
//        if (latitude < -90) {
//            latitude = -180 - latitude;
//            longitude += 180;
//        } else if (latitude > 90) {
//            latitude -= 180;
//            longitude += 180;
//        }
//        while (longitude < -180) longitude += 360;
//        while (longitude > 180) longitude -= 360;
        
        if (latitude < -90-ERR || latitude > 90+ERR) throw new IllegalArgumentException("latitude: " + latitude);
        
        if (latitude < -90) latitude = -90;
        if (latitude > 90 ) latitude =  90;
        while (longitude <= -180) longitude += 360;
        while (longitude > 180) longitude -= 360;

        assert -90 <= latitude && latitude <= 90 : "latitude " + latitude;
        assert -180 < longitude && longitude <= 180 : "longitude " + longitude;
        
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
     * @return radians angle from this point to the given point, 0 is north, PI/2 is east
     */
    public double angleTo(Point point) {
        double dx = cos(toRadians(this.latitude)) * (point.longitude - this.longitude);
        double dy = point.latitude - this.latitude;
        while (dx < -180) dx += 360;
        while (dx > 180) dx -= 360;
        double angle = atan2(dx, dy);
        if (angle < 0) {
            angle += 2 * PI;
        }
        return angle;
    }
    
    /**
     * @param point 
     * @return degrees <i>distance</i> between the two points
     */
    public double distTo(Point point) {
//        return toDegrees(acos(sin(toRadians(latitude))*sin(toRadians(point.latitude))+
//                cos(toRadians(latitude))*cos(toRadians(point.latitude))
//                        *cos(toRadians(longitude)-toRadians(point.longitude))));
        double dx = cos(toRadians(this.latitude)) * (point.longitude - this.longitude);
        double dy = point.latitude - this.latitude;
        while (dx < -180) dx += 360;
        while (dx > 180) dx -= 360;
        return sqrt(dx*dx + dy*dy);
    }
    
    /**
     * @param dist in degrees
     * @param angle in radians
     * @return
     */
    public Point polar(double dist, double angle) {
        return new Point(latitude + dist*cos(angle), 
                longitude + dist*sin(angle)/cos(toRadians(latitude)));
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
        double lon = other.longitude-this.longitude;
        while (lon < -180) lon += 360;
        while (lon > 180) lon -= 360;
        return abs(other.latitude-this.latitude) < ERR && abs(lon) < ERR;
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
