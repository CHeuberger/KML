package cfh.air;

import static java.lang.Math.*;

import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArcB implements Segment {

    /** step in degrees */
    private static final int STEP = 2;

    private final Point start;
    private final Point end;
    private final Point center;
    private final boolean clockwise;
    
    public ArcB(Point start, Point end, Point center, boolean clockwise) {
        if (start == null) throw new IllegalArgumentException("null");
        if (end == null) throw new IllegalArgumentException("null");
        if (center == null) throw new IllegalArgumentException("null");
        
        this.start = start;
        this.end = end;
        this.center = center;
        this.clockwise = clockwise;
    }
    
    public Point getStart() {
        return start;
    }
    
    public Point getEnd() {
        return end;
    }
    
    public Point getCenter() {
        return center;
    }
    
    public double getRadius() {
        return 60.0 * center.distTo(start);
    }
    
    public boolean isClockwise() {
        return clockwise;
    }
    
    @Override
    public List<Point> getPoints(int step) {
        if (step < 0) throw new IllegalArgumentException("invalid step: " + step);
        
        if (step == 0) 
            return Arrays.asList(start, end);
        
        double startRadius = center.distTo(start);
        double endRadius = center.distTo(end);
        double startAngle = center.angleTo(start);
        double endAngle = center.angleTo(end);
        
        if (clockwise) {
            while (endAngle < startAngle) {
                endAngle += 2 * PI;
            }
        } else {
            while (startAngle < endAngle) {
                startAngle += 2 * PI;
            }
        }

        double delta = endAngle - startAngle;
        int n = (int) abs(ceil(delta / toRadians(step)));
        double angleStep = delta / n;
        double radiusStep = (endRadius - startRadius) / n;
        
        List<Point> points = new ArrayList<Point>();
        double angle = startAngle;
        double radius = startRadius;
        points.add(start);
        for (int i = 1; i < n; i++) {
            angle += angleStep;  // starting with i = 1
            radius += radiusStep;
            points.add(center.polar(radius, angle)); 
        }
        points.add(end);
        
        return points;
    }
    
    @Override
    public Rectangle2D getBound() {
        List<Point> points = getPoints(STEP);
        double x = 0;
        double y = 0;
        double w = 0;
        double h = 0;

        if (!points.isEmpty()) {
            x = points.get(0).getLongitude();
            y = points.get(0).getLatitude();
            for (Point point : points) {
                double lon = point.getLongitude();
                if (lon < x) {
                    w += lon - x;
                    x = lon;
                } else if (lon > x + w) {
                    w = lon - x;
                }
                double lat = point.getLatitude();
                if (lat < y) {
                    h += lat - y;
                    y = lat;
                } else if (lat > y + h) {
                    h = lat - y;
                }
            }
        }
        return new Rectangle2D.Double(x, y, w, h);
    }
    
//    For any two points (p,q) and (r,s) on the circumference/path of the ellipse, with the ellipse center at the origin (0,0):
//
//        a^2 = (p^2)*(s^2) - (r^2)*(q^2) ) / (s^2 - q^2)
//        b^2 = (r^2)*(q^2) - (p^2)*(s^2) ) / (p^2 - r^2)
//    
//    p = start.x - center.x; q = start.y - center.y;
//    r = end.x   - center.y; s = end.y   - center.y;

    @Override
    public Point draw(Graphics2D gg, Point last) {
        double p = start.getLongitude() - center.getLongitude();
        double q = start.getLatitude()  - center.getLatitude();
        double r = end.getLongitude() - center.getLongitude();
        double s = end.getLatitude()  - center.getLatitude();
        
        double w = (p*p*s*s - r*r*q*q) / (s*s - q*q);
        double h = (r*r*q*q - p*p*s*s) / (p*p - r*r);
        
        double startA = atan2(q, p);
        double endA = atan2(s, r);
        
        if (last != null) {
            gg.draw(new Line2D.Double(last.getLongitude(), last.getLatitude(), start.getLongitude(), start.getLatitude()));
        }
        gg.draw(new Arc2D.Double(center.getLongitude()-w, center.getLatitude()-h, 2*w, 2*h, startA, endA-startA, Arc2D.OPEN));
        
        return end;
    }
}
