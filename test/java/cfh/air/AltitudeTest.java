package cfh.air;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.hamcrest.Matchers.*;
import static java.util.Arrays.*;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import cfh.air.Altitude.Type;


@RunWith(Theories.class)
public class AltitudeTest {
    
    @DataPoints("type")
    public static final Type[] types = Type.values();
    
    @DataPoints("value")
    public static final int[] values = { 0, 100, 200, 1000, 2000 };
    
    private static final double ERR = 1E-5;

    
    @Theory
    public void testAltitude(
            @FromDataPoints("type")  Type type,
            @FromDataPoints("value") int value
    ) {
        Altitude test = new Altitude(type, value);
        
        assertThat(test.getType(), is(type));
        assertThat(test.getValue(), closeTo(value, ERR));
    }

    @Theory
    public void testGetValueFeet(
            @FromDataPoints("type")  Type type,
            @FromDataPoints("value") int value
    ) {
        int test = new Altitude(type, value).getValueFeet();
        
        assertThat((double) test, closeTo(value/0.3048, 0.5 + ERR));
    }

    @Theory
    public void testToDisplayValue_nonFL(
            @FromDataPoints("type")  Type type,
            @FromDataPoints("value") int value
    ) {
        assumeThat(type, not(Type.FL));
        int feets = (int) (value/0.3048 + 0.5);
        String test = new Altitude(type, value).toDisplayValue();
        
        assertThat(test, stringContainsInOrder(asList(Integer.toString(feets), type.toString())));
        assertThat(test, stringContainsInOrder(asList(Integer.toString(value), "m")));
    }


    @Theory
    public void testToDisplayValue_FL(
            @FromDataPoints("type")  Type type,
            @FromDataPoints("value") int value
    ) {
        assumeThat(type, is(Type.FL));
        int fl = (int) (value/0.3048/100 + 0.5);
        String test = new Altitude(type, value).toDisplayValue();
        
        assertThat(test, stringContainsInOrder(asList("FL", Integer.toString(fl))));
        assertThat(test, stringContainsInOrder(asList(Integer.toString(value), "m")));
    }
}
