package cfh.air;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static java.lang.Math.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;


@RunWith(Theories.class)
public class ArcATest {

    @DataPoints("rad")
    public static double[] radii = { 1, 2, 10, 100 };
    
    @DataPoints("ang")
    public static double[] angles = { 0, 10, 60, 90, 180, 270, 360 };
    
    @DataPoints("lat")
    public static double[] latitudes = { 0, 1, -1, 10, -10, -80, 80 };

    @DataPoints("lon")
    public static double[] longitudes = { 0, 1, -1, 10, -10, 90, -90, 170, -170, 180 };
    
    @DataPoints("dir")
    public static boolean[] directions = { false, true };

    private static final double N = 0;
    private static final double E = 90;
    private static final double S = 180;
    private static final double W = 270;
    private static final double ERR = 1E-5;
    
    @Theory
    public void testArcA(
            @FromDataPoints("rad") double radius, 
            @FromDataPoints("ang") double start, 
            @FromDataPoints("ang") double end, 
            @FromDataPoints("lat") double latitude,
            @FromDataPoints("lon") double longitude,
            @FromDataPoints("dir") boolean clockwise
    ) {
        assumeThat(latitude, both(greaterThanOrEqualTo(-85.0)).and(lessThanOrEqualTo(85.0)));
        assumeThat(longitude, both(greaterThan(-180.0)).and(lessThanOrEqualTo(180.0)));
        Point center = new Point(latitude, longitude);
        ArcA test = new ArcA(radius, start, end, center, clockwise);
        
        assertThat("radius", test.getRadius(), closeTo(radius, ERR));
        assertThat("start", test.getStartAngle(), closeTo(start, ERR));
        assertThat("end", test.getEndAngle(), closeTo(end, ERR));
        assertThat("center", test.getCenter(), equalTo(center));
        assertThat("clockwise", test.isClockwise(), is(clockwise));
    }

    @Theory
    public void testGetPoints_zero(
            @FromDataPoints("dir") boolean clockwise
    ) { 
        double degrees = 1.0;
        ArcA arc;
        double startLat;
        double startLon;
        double endLat;
        double endLon;
        final Point center = new Point(0, 0);
        List<Point> test;
        Point start;
        Point end;
        
        arc = new ArcA(degrees * Segment.MILES_PER_DEGREE, N, E, center, clockwise);
        startLat = center.getLatitude() + degrees;
        startLon = center.getLongitude();
        endLat = center.getLatitude();
        endLon = center.getLongitude() - degrees;
        
        test = arc.getPoints(0);
        assertThat(test.size(), is (2));
        start = test.get(0);
        assertThat(start.getLatitude(), closeTo(startLat, ERR));
        assertThat(start.getLongitude(), closeTo(startLon, ERR));
        end = test.get(1);
        assertThat(end.getLatitude(), closeTo(endLat, ERR));
        assertThat(end.getLongitude(), closeTo(endLon, ERR));
        
        arc = new ArcA(degrees * Segment.MILES_PER_DEGREE, S, W, center, clockwise);
        startLat = center.getLatitude() - degrees;
        startLon = center.getLongitude();
        endLat = center.getLatitude();
        endLon = center.getLongitude() + degrees;
        
        test = arc.getPoints(0);
        assertThat(test.size(), is (2));
        start= test.get(0);
        assertThat(start.getLatitude(), closeTo(startLat, ERR));
        assertThat(start.getLongitude(), closeTo(startLon, ERR));
        end = test.get(1);
        assertThat(end.getLatitude(), closeTo(endLat, ERR));
        assertThat(end.getLongitude(), closeTo(endLon, ERR));
    }
    
    @Theory
    public void testGetPoints_positive(
            @FromDataPoints("dir") boolean clockwise
    ) {
        double radius = 10;  // degrees
        Point center = new Point(0, 0);
        double startAngle = E;
        double endAngle = S;
        ArcA arc = new ArcA(radius*Segment.MILES_PER_DEGREE, startAngle, endAngle, center, clockwise);
        Point start = center.polar(radius, toRadians(startAngle));
        Point end = center.polar(radius, toRadians(endAngle));
        int count = 10;
        
        List<Point> test = arc.getPoints((clockwise ? 90 : 270) / (count-1));
        assertThat("result size", test, hasSize(count));
        assertThat("first point", test.get(0), equalTo(start));
        assertThat("last point", test.get(count-1), equalTo(end));
        
        for (int i = 0; i < test.size(); i++) {
            Point point = test.get(i);
            assertThat("distance " + point, center.distTo(point), closeTo(radius, ERR));
            double angle = center.angleTo(point);
            if (i == 0) {
                assertThat("angle " + point, angle, closeTo(toRadians(startAngle), ERR));
            } else if (i == test.size()-1) {
                assertThat("angle " + point, angle, closeTo(toRadians(endAngle), ERR));
            }
        }
    }
 
    @Test(expected = IllegalArgumentException.class)
    public void testGetPoints_negative() {
        double radius = 10;
        Point center = new Point(0, 0);
        ArcA arc = new ArcA(radius, N, E, center, true);

        arc.getPoints(-2);
    }

    @Test
    public void testGetBound() {
        fail("Not yet implemented");
    }

}
