package cfh.air;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
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

    @DataPoints("radius")
    public static double[] radii = { 1, 2, 10, 100 };
    
    @DataPoints("angle")
    public static double[] angles = { 0, 10, 60, 90, 180, 270, 359, 360 };
    
    @DataPoints("coord")
    public static double[] coords = { 0, 0.1, -0.1, 0.2, -0.2, 1, -1 };
    
    @DataPoints("dir")
    public static boolean[] directions = { false, true };
    
    private static final double ERR = 1E-5;

    
    @Theory
    public void testArcA(
            @FromDataPoints("radius") double radius, 
            @FromDataPoints("angle")  double start, 
            @FromDataPoints("angle")  double end, 
            @FromDataPoints("coord")  double latitude,
            @FromDataPoints("coord")  double longitude,
            @FromDataPoints("dir")    boolean clockwise
            ) {
        assumeThat(null, anything());
        Point center = new Point(latitude, longitude);
        ArcA test = new ArcA(radius, start, end, center, clockwise);
        
        assertThat(test.getCenter(), equalTo(center));
        assertThat(test.getRadius(), is(radius));
        assertThat(test.isClockwise(), is(clockwise));
    }

    @Test
    public void testGetPoints_zero() {
        ArcA arc;
        double startLat;
        double startLon;
        double endLat;
        double endLon;
        final Point center = new Point(0, 0);
        List<Point> test;
        Point start;
        Point end;
        
        arc = new ArcA(Segment.MILES_PER_DEGREE, 0, 90, center, true);
        startLat = center.getLatitude() + 1;
        startLon = center.getLongitude();
        endLat = center.getLatitude();
        endLon = center.getLongitude() + 1;
        
        test = arc.getPoints(0);
        assertThat(test.size(), is (2));
        start = test.get(0);
        assertThat(start.getLatitude(), closeTo(startLat, ERR));
        assertThat(start.getLongitude(), closeTo(startLon, ERR));
        end = test.get(1);
        assertThat(end.getLatitude(), closeTo(endLat, ERR));
        assertThat(end.getLongitude(), closeTo(endLon, ERR));
        
        arc = new ArcA(Segment.MILES_PER_DEGREE, 180, 270, center, false);
        startLat = center.getLatitude() - 1;
        startLon = center.getLongitude();
        endLat = center.getLatitude();
        endLon = center.getLongitude() - 1;
        
        test = arc.getPoints(0);
        assertThat(test.size(), is (2));
        start= test.get(0);
        assertThat(start.getLatitude(), closeTo(startLat, ERR));
        assertThat(start.getLongitude(), closeTo(startLon, ERR));
        end = test.get(1);
        assertThat(end.getLatitude(), closeTo(endLat, ERR));
        assertThat(end.getLongitude(), closeTo(endLon, ERR));
    }

    @Test
    public void testGetBound() {
        fail("Not yet implemented");
    }

}
