package cfh.air;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ArcA implements Segment {

    /** step in degrees */
    private static final int STEP = 2;

    /** nautical mile */
    private final double radius;
    /** degrees */
    private final double startAngle;
    /** degrees */
    private final double endAngle;
    
    private final Point center;
    private final boolean clockwise;
    
    /**
     * @param radius nautical mile
     * @param startAngle degrees, 0° is north, 90° is east
     * @param endAngle degrees
     * @param center 
     * @param clockwise
     */
    public ArcA(double radius, double startAngle, double endAngle, Point center, boolean clockwise) {
        if (radius < 0) throw new IllegalArgumentException(Double.toString(radius));
        if (startAngle < 0 || startAngle > 360) throw new IllegalArgumentException(Double.toString(startAngle));
        if (endAngle < 0 || endAngle > 360) throw new IllegalArgumentException(Double.toString(endAngle));
        if (center == null) throw new IllegalArgumentException("null");
        
        this.radius = radius;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        this.center = center;
        this.clockwise = clockwise;
    }
    
    public double getRadius() {
        return radius;
    }
    
    public double getStartAngle() {
        return startAngle;
    }
    
    public double getEndAngle() {
        return endAngle;
    }
    
    public Point getCenter() {
        return center;
    }
    
    public boolean isClockwise() {
        return clockwise;
    }
    
    @Override
    public List<Point> getPoints(int step) {
        if (step < 0) throw new IllegalArgumentException("invalid step: " + step);
        
        double start = startAngle;
        double end = endAngle;
        double r = radius / MILES_PER_DEGREE;
        
        if (clockwise) {
            while (end <= start) {
                end += 360;
            }
        } else {
            while (start <= end) {
                start += 360;
            }
        }

        double delta = end - start;
        int n = (step == 0) ? 1 : (int) abs(ceil(delta / step));
        double angleStep = toRadians(delta / n);
        
        List<Point> points = new ArrayList<Point>();
        double angle = toRadians(start);
        for (int i = 0; i < n; i++) {
            points.add(center.polar(r, angle)); 
            angle += angleStep;
        }
        points.add(center.polar(r, toRadians(end))); 
        
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
                    w -= lon - x;
                    x = lon;
                } else if (lon > x + w) {
                    w = lon - x;
                }
                double lat = point.getLatitude();
                if (lat < y) {
                    h -= lat - y;
                    y = lat;
                } else if (lat > y + h) {
                    h = lat - y;
                }
            }
        }
        return new Rectangle2D.Double(x, y, w, h);
    }
    
    @Override
    public Point draw(Graphics2D gg, Point last) {
        double start = startAngle;
        double end = endAngle;
        double r = radius  / MILES_PER_DEGREE;
        
        if (clockwise) {
            if (end <= start) {
                end += 360;
            }
        } else {
            if (start <= end) {
                start += 360;
            }
        }
        System.out.printf("%s x %f  %f -> %f\n", center, r, start, end);
        
        // TODO testing
        Color org = gg.getColor();
        try {
            gg.setColor(Color.RED);
            Point l = null;
            for (Point p : getPoints(5)) {
                if (l != null) {
                    gg.draw(new Line2D.Double(l.getLongitude(), l.getLatitude(), p.getLongitude(), p.getLatitude()));
                }
                l = p;
            }
        } finally {
            gg.setColor(org);
        }
        
        if (last != null) {
            Point s = center.polar(r, start);
            gg.draw(new Line2D.Double(last.getLongitude(), last.getLatitude(), s.getLongitude(), s.getLatitude()));
        }
        gg.draw(new Arc2D.Double(center.getLongitude()-r, center.getLatitude()-r, 2*r, 2*r, start-90, end-start, Arc2D.OPEN));
        
        return center.polar(r, toRadians(endAngle));
    }
}
