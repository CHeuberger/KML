package cfh.air;

import static java.lang.Math.*;
import static java.util.Arrays.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;


@RunWith(Theories.class)
public class PointTest {
    
    private static final double N = 0;
    private static final double E = toRadians(90);
    private static final double S = toRadians(180);
    private static final double W = toRadians(270);
    private static final double ERR = 1E-5;

    @DataPoints("lat")
    public static double[] latitudes = { 0, 1, -1, 10, -10, -80, 80, 90, -90 };

    @DataPoints("lon")
    public static double[] longitudes = { 0, 1, -1, 10, -10, 90, -90, 170, -170, 180, -180 };


    @Theory
    public void testPoint(
            @FromDataPoints("lat") double latitude,
            @FromDataPoints("lon") double longitude
    ) {
        assumeThat(latitude, both(greaterThanOrEqualTo(-90.0)).and(lessThanOrEqualTo(90.0)));
        assumeThat(longitude, both(greaterThan(-180.0)).and(lessThanOrEqualTo(180.0)));
        Point test = new Point(latitude, longitude);

        assertThat("latitude", test.getLatitude(), closeTo(latitude, ERR));
        assertThat("longitude", test.getLongitude(), closeTo(longitude, ERR));
    }

    @Test
    public void testAngleTo() {
        Point north = new Point(21, 0);
        Point south = new Point(19, 0);
        Point east = new Point(20, -1);
        Point west = new Point(20, 1);
        double angle;
        
        angle = south.angleTo(north);
        assertThat("south to north", angle, closeTo(N, ERR));
        
        angle = west.angleTo(east);
        assumeThat("west to east", angle, closeTo(E, ERR));
        
        angle = north.angleTo(south);
        assertThat("north to south", angle, closeTo(S, ERR));
        
        angle = east.angleTo(west);
        assertThat("east to west", angle, closeTo(W, ERR));
    }
    
    @Test
    public void testAngleTo_far() {
        Point fareast = new Point(0, -170);
        Point farwest = new Point(0, 170);
        double angle;
        
        angle = fareast.angleTo(farwest);
        assertThat("far east to far west", angle, closeTo(E, ERR));
        
        angle = farwest.angleTo(fareast);
        assertThat("far west to far east", angle, closeTo(W, ERR));
    }

    @Test
    public void testDistTo() {
        double dist;
        
        dist = new Point(0, 0).distTo(new Point(0, 10));
        assertThat(dist, closeTo(10, ERR));
        
        dist = new Point(0, 0).distTo(new Point(10, 0));
        assertThat(dist, closeTo(10, ERR));
        
        dist = new Point(10, 0).distTo(new Point(20, 0));
        assertThat(dist, closeTo(10, ERR));
        
        dist = new Point(10, 0).distTo(new Point(10, 0));
        assertThat(dist, closeTo(0, ERR));
        
        Point to = new Point(10, 10);
        dist = new Point(10, 0).distTo(to);
        assertThat(dist, lessThan(10.0));
        assertThat(dist, closeTo(to.distTo(new Point(10, 0)), ERR));
        
        dist = new Point(0, 170).distTo(new Point(0, -170));
        assertThat(dist, closeTo(20, ERR));
    }

    @Test
    public void testPolar() {
        Point zero = new Point(0, 0);
        double dist = 1;
        Point north = new Point(dist, 0);
        Point east = new Point(0, -dist);
        Point south = new Point(-dist, 0);
        Point west = new Point(0, dist);
        Point test;
        
        test = zero.polar(dist, N);
        assertThat("N", test, equalTo(north));
        
        test = zero.polar(dist, E);
        assertThat("E", test, equalTo(east));
        
        test = zero.polar(dist, S);
        assertThat("S", test, equalTo(south));
        
        test = zero.polar(dist, W);
        assertThat("W", test, equalTo(west));
    }
    
    @Theory
    public void testPolar_reciprocal(
            @FromDataPoints("lat") double latitude1,
            @FromDataPoints("lon") double longitude1,
            @FromDataPoints("lat") double latitude2,
            @FromDataPoints("lon") double longitude2
    ) {
        assumeThat(latitude1, both(greaterThanOrEqualTo(-90.0)).and(lessThanOrEqualTo(90.0)));
        assumeThat(longitude1, both(greaterThan(-180.0)).and(lessThanOrEqualTo(180.0)));
        assumeThat(latitude2, both(greaterThanOrEqualTo(-90.0)).and(lessThanOrEqualTo(90.0)));
        assumeThat(longitude2, both(greaterThan(-180.0)).and(lessThanOrEqualTo(180.0)));
        Point center = new Point(latitude1, longitude1);
        Point point = new Point(latitude2, longitude2);
        
        double dist = center.distTo(point);
        assumeThat(dist, lessThan(10.0));
        double angle = center.angleTo(point);
        Point test = center.polar(dist, angle);
        assertThat(center + "->" + point + " == " + dist + "<" + (int) toDegrees(angle) + "°", test, equalTo(point));
    }

    @Theory
    public void testGetPoints(
            @FromDataPoints("lat") double latitude,
            @FromDataPoints("lon") double longitude
    ) {
        assumeThat(latitude, both(greaterThanOrEqualTo(-90.0)).and(lessThanOrEqualTo(90.0)));
        assumeThat(longitude, both(greaterThan(-180.0)).and(lessThanOrEqualTo(180.0)));
        Point point = new Point(latitude, longitude);
        List<Point> expected = Collections.singletonList(point);
        List<Point> test;
        
        test = point.getPoints(0);
        assertThat("no step", test, equalTo(expected));
        
        test = point.getPoints(10);
        assertThat("step of 10", test, equalTo(expected));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetPoints_negative() {
        Point point = new Point(0, 0);
        point.getPoints(-1);
    }

    @Theory
    public void testGetBound(
            @FromDataPoints("lat") double latitude,
            @FromDataPoints("lon") double longitude
    ) {
        assumeThat(latitude, both(greaterThanOrEqualTo(-90.0)).and(lessThanOrEqualTo(90.0)));
        assumeThat(longitude, both(greaterThan(-180.0)).and(lessThanOrEqualTo(180.0)));
        Point point = new Point(latitude, longitude);
        Rectangle2D test;
        
        test = point.getBound();
        assertThat(test, equalTo(new Rectangle2D.Double(longitude, latitude, 0, 0).getBounds2D()));
    }

    @Theory
    public void testEqualsObject(
            @FromDataPoints("lat") double latitude,
            @FromDataPoints("lon") double longitude
    ) {
        assumeThat(latitude, both(greaterThanOrEqualTo(-90.0)).and(lessThanOrEqualTo(90.0)));
        assumeThat(longitude, both(greaterThan(-180.0)).and(lessThanOrEqualTo(180.0)));
        Point point = new Point(latitude, longitude);
        
        assertThat("same instance", point.equals(point), is(true));
        assertThat("new instance", point.equals(new Point(latitude, longitude)), is(true));
        
        assumeThat(latitude, is(not(0.0)));
        assertThat("latitude not equal", point.equals(new Point(0, longitude)), is(false));
        
        assumeThat(longitude, is(not(0.0)));
        assertThat("longitude not equal", point.equals(new Point(latitude, 0)), is(false));
    }
    
    @Theory
    public void testHashCode(
            @FromDataPoints("lat") double latitude,
            @FromDataPoints("lon") double longitude
    ) {
        assumeThat(latitude, both(greaterThanOrEqualTo(-90.0)).and(lessThanOrEqualTo(90.0)));
        assumeThat(longitude, both(greaterThan(-180.0)).and(lessThanOrEqualTo(180.0)));
        Point point = new Point(latitude, longitude);
        int hash = point.hashCode();
        
        assertThat("same instance", point.hashCode(), equalTo(hash));

        Point test = new Point(latitude, longitude);
        assumeTrue(test.equals(point));
        assertThat("new instance", test.hashCode(), equalTo(hash));
    }

    @Theory
    public void testToString(
            @FromDataPoints("lat") double latitude,
            @FromDataPoints("lon") double longitude
    ) {
        assumeThat(latitude, both(greaterThanOrEqualTo(-90.0)).and(lessThanOrEqualTo(90.0)));
        assumeThat(longitude, both(greaterThan(-180.0)).and(lessThanOrEqualTo(180.0)));
        String test = new Point(latitude, longitude).toString();
        String lat = String.format(Locale.ROOT, "%.4f", latitude);
        String lon = String.format(Locale.ROOT, "%.4f", longitude);

        assertThat(test, stringContainsInOrder(asList(lat, ",", lon)));
    }
}
