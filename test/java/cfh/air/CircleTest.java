package cfh.air;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class CircleTest {
    
    @DataPoints("rad")
    public static double[] radii = { 1, 2, 10, 100 };

    @DataPoints("lat")
    public static double[] latitudes = { 0, 1, -1, 10, -10, -80, 80 };

    @DataPoints("lon")
    public static double[] longitudes = { 0, 1, -1, 10, -10, 90, -90, 170, -170, 180 };
    
    @DataPoints("dir")
    public static boolean[] directions = { false, true };

    private static final double ERR = 1E-5;

    
    @Theory
    public void testCircle(
            @FromDataPoints("rad") double radius,
            @FromDataPoints("lat") double latitude,
            @FromDataPoints("lon") double longitude,
            @FromDataPoints("dir") boolean clockwise
    ) {
        assumeThat(latitude, both(greaterThanOrEqualTo(-85.0)).and(lessThanOrEqualTo(85.0)));
        assumeThat(longitude, both(greaterThan(-180.0)).and(lessThanOrEqualTo(180.0)));
        Point center = new Point(latitude, longitude);
        Circle test = new Circle(radius, center, clockwise);
        
        assertThat("radius", test.getRadius(), closeTo(radius, ERR));
        assertThat("center", test.getCenter(), equalTo(center));
        assertThat("clockwise", test.isClockwise(), is(clockwise));
    }

    @Theory
    public void testGetPoints_zero(
            @FromDataPoints("lat") double latitude,
            @FromDataPoints("lon") double longitude
    ) {
        assumeThat(latitude, both(greaterThanOrEqualTo(-85.0)).and(lessThanOrEqualTo(85.0)));
        assumeThat(longitude, both(greaterThan(-180.0)).and(lessThanOrEqualTo(180.0)));
        Point center = new Point(latitude, longitude);
        Circle circle = new Circle(10, center, true);
        
        List<Point> test = circle.getPoints(0);
        assertThat(test, equalTo(Collections.singletonList(center)));
    }
    
    @Test
    public void testGetPoints_positive() {
        double radius = 10;  // degrees
        Point center = new Point(0, 0);
        Point start = center.polar(radius, 0);
        Circle circle = new Circle(radius*Segment.MILES_PER_DEGREE, center, true);
        int count = 10;
        
        List<Point> test = circle.getPoints(360 / (count-1));
        assertThat("result size", test, hasSize(count));
        assertThat("first point", test.get(0), equalTo(start));
        assertThat("last point", test.get(count-1), equalTo(start));
        
        double last = -1;
        for (int i = 0; i < test.size(); i++) {
            Point point = test.get(i);
            assertThat("distance " + point, center.distTo(point), closeTo(radius, ERR));
            double angle = center.angleTo(point);
            if (i == 0 || i == test.size()-1) {
                assertThat("angle " + point, angle, closeTo(0, ERR));
            } else {
                assertThat("angle " + point, angle, greaterThan(last));
            }
            last = angle;
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetPoints_negative() {
        double radius = 10;
        Point center = new Point(0, 0);
        Circle circle = new Circle(radius, center, true);

        circle.getPoints(-1);
    }

    @Theory
    public void testGetBound(
            @FromDataPoints("rad") double radius,
            @FromDataPoints("lat") double latitude,
            @FromDataPoints("lon") double longitude,
            @FromDataPoints("dir") boolean clockwise
    ) {
        assumeThat(latitude, both(greaterThanOrEqualTo(-85.0)).and(lessThanOrEqualTo(85.0)));
        assumeThat(longitude, both(greaterThan(-180.0)).and(lessThanOrEqualTo(180.0)));
        Point center = new Point(latitude, longitude);
        Circle circle = new Circle(radius, center, clockwise);
        List<Point> points = circle.getPoints(10);
        Rectangle2D test = circle.getBound();
        
        for (Point point : points) {
            assertTrue(test + "contains " + point, test.contains(longitude, latitude));
        }
    }

}
