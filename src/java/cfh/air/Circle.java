package cfh.air;

import static java.lang.Math.*;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Circle implements Segment {

    /** nautical mile. */
    private final double radius;
    private final Point center;
    private final boolean clockwise;
    
    /**
     * @param radius nautical mile
     * @param center
     * @param clockwise
     */
    public Circle(double radius, Point center, boolean clockwise) {
        if (radius < 0) throw new IllegalArgumentException(Double.toString(radius));
        if (center == null) throw new IllegalArgumentException("center undefined");
        
        this.radius = radius;
        this.center = center;
        this.clockwise = clockwise;
    }
    
    public Point getCenter() {
        return center;
    }
    
    public double getRadius() {
        return radius;
    }
    
    public boolean isClockwise() {
        return clockwise;
    }

    @Override
    public List<Point> getPoints(int step) {
        if (step < 0) throw new IllegalArgumentException("invalid step: " + step);
        
        if (step == 0) {
            return Collections.singletonList(center);
        }
        
        double r = radius / MILES_PER_DEGREE;
        
        int n = 360 / step;
        double angleStep = (clockwise ? 2 : -2) * PI / n;
        
        List<Point> points = new ArrayList<Point>();
        double angle = 0;
        for (int i = 0; i < n; i++) {
            points.add(center.polar(r, angle)); 
            angle += angleStep;
        }
        points.add(center.polar(r, 0)); 
        
        return points;
    }
    
    @Override
    public Rectangle2D getBound() {
        double r = radius/ MILES_PER_DEGREE;
        double x = center.getLongitude() - r;
        double y = center.getLatitude()  - r;
        return new Rectangle2D.Double(x, y, 2*r, 2*r);
    }
    
    @Override
    public Point draw(Graphics2D gg, Point last) {
        double r = this.radius / MILES_PER_DEGREE;
        if (last != null) {
            gg.draw(new Line2D.Double(last.getLongitude(), last.getLatitude(), center.getLongitude(), center.getLatitude()));
        }
        gg.draw(new Ellipse2D.Double(center.getLongitude()-r, center.getLatitude()-r, 2*r, 2*r));
        return center;
    }
}
