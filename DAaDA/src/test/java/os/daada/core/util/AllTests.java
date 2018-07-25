package os.daada.core.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(
        { RsaKeyPairUtilsTest.class, EccKeyPairUtilsTest.class, ElgamalKeyPairUtilsTest.class })

public class AllTests {

}
