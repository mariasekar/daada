package com.adroit.andes.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(
        { AsymmetricRsaCryptoCipherTest.class, AsymmetricEccWithAesCryptoCipherTest.class,/*AsymmetricEccCryptoCipherTest.class,*/
        	MsisdnAnonymizerRsaTest.class, MsisdnAnonymizerEccTest.class, MsisdnAnonymizerWithEccWithAesTest.class, DobTest.class})

public class AllTests {

}
