package cfh;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import cfh.air.AltitudeTest;
import cfh.air.ArcATest;


@RunWith(Suite.class)
@SuiteClasses({
    AltitudeTest.class,
    ArcATest.class,
})

public class TestAll {
}