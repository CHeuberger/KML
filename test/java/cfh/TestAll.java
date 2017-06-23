package cfh;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import cfh.air.AltitudeTest;
import cfh.air.ArcATest;
import cfh.air.ArcBSpecification;
import cfh.air.CircleTest;
import cfh.air.PointTest;


@RunWith(Suite.class)
@SuiteClasses({
    AltitudeTest.class,
    PointTest.class,
    CircleTest.class,
    ArcATest.class,
    
    ArcBSpecification.class,
})

public class TestAll {
}
