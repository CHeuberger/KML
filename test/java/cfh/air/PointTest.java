package cfh.air;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;


@RunWith(Theories.class)
public class PointTest {

    @DataPoints("coord")
    public static double[] coords = { 0, 0.1, -0.1, 0.2, -0.2, 1, -1 };
    
    private static final double ERR = 1E-5;


    @Theory
    public void testPoint(
            @FromDataPoints("coord") double latitude,
            @FromDataPoints("coord") double longitude
    ) {
        Point test = new Point(latitude, longitude);

        assertThat(test.getLatitude(), closeTo(latitude, ERR));
        assertThat(test.getLongitude(), closeTo(longitude, ERR));
    }

    @Test
    public void testAngleTo() {
        Point north = new Point(21, 0);
        Point south = new Point(19, 0);
        double angle;
        
        angle = south.angleTo(north);
        assertThat(angle, closeTo(0, ERR));
        fail("Not yet implemented");
    }

    @Test
    public void testDistTo() {
        fail("Not yet implemented");
    }

    @Test
    public void testPolar() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetPoints() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetBound() {
        fail("Not yet implemented");
    }

    @Test
    public void testEqualsObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testToString() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testHashCode() {
        fail("Not yet implemented");
    }
}
