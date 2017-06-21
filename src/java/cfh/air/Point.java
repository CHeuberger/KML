package cfh.air;

import static java.lang.Math.*;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;


public class Point implements Segment {

    /** degrees */
    private final double latitude;
    /** degrees */
    private final double longitude;
    
    /**
     * @param latitude degrees
     * @param longitude degrees
     */
    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public double angleTo(Point point) {
        double angle = atan2(point.longitude - longitude, point.latitude - latitude);
        if (angle < 0) {
            angle += 2 * PI;
        }
        return angle;
    }
    
    public double distTo(Point point) {
        return toDegrees(acos(sin(toRadians(latitude))*sin(toRadians(point.latitude))+
                cos(toRadians(latitude))*cos(toRadians(point.latitude))
                        *cos(toRadians(longitude)-toRadians(point.longitude))));
    }
    
//    double distTo(Point point) {
//        return Point2D.distance(longitude, latitude, point.longitude, point.latitude);
//    }
    
    public Point polar(double radius, double angle) {
        return new Point(latitude + radius*cos(angle), 
                longitude + radius*sin(angle)/cos(toRadians(latitude)));
    }
    
    @Override
    public List<Point> getPoints(int step) {
        assert step >= 0 : step;
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
        if (obj == this)
            return true;
        if (!(obj instanceof Point))
            return false;
        
        Point p = (Point) obj;
        return p.latitude == latitude && p.longitude == longitude;
    }

    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(latitude);
        bits ^= Double.doubleToLongBits(longitude);
        return (int)(bits ^ (bits >>> 32));
    }
    
    @Override
    public String toString() {
        return String.format("P(%f,%f)", latitude, longitude);
    }
}
