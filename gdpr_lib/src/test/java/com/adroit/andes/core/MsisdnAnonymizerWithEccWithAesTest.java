package com.adroit.andes.core;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.adroit.andes.core.AndesException;
import org.adroit.andes.core.Error;
import org.adroit.andes.core.Result;
import org.adroit.andes.core.crypt.AsymmetricEccWithAesCipher;
import org.adroit.andes.core.crypt.ICrypto;
import org.adroit.andes.core.pii.GoogleMsisdnAndesizer;
import org.adroit.andes.core.util.KeyPairUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class MsisdnAnonymizerWithEccWithAesTest {

	public static final String ANDES_SECURITY_KEYS_PUBLIC = "anonymization.security.keys.public";
	public static final String ANDES_SECURITY_KEYS_PRIVATE = "anonymization.security.keys.private";

    private static GoogleMsisdnAndesizer msisdnAnonymizer = null;
    private static Config andesConfig;
    private static ICrypto cryptoHandler;

    @BeforeClass
    public static void setUp() throws Exception {
        // Initialize configurations
    	andesConfig = ConfigFactory.load();
    	testGenerateKeyPair();
    	cryptoHandler = new AsymmetricEccWithAesCipher(andesConfig);
        msisdnAnonymizer = new GoogleMsisdnAndesizer(andesConfig, cryptoHandler);
    }

    private static void testGenerateKeyPair() throws Exception {
        final String publicKeyFilename = andesConfig.getString(ANDES_SECURITY_KEYS_PUBLIC);
        final String privateKeyFilename = andesConfig.getString(ANDES_SECURITY_KEYS_PRIVATE);

        KeyPairUtils.generateEcKeyPair("prime256v1", publicKeyFilename, privateKeyFilename);
    }
 
    @Test
    public void givenMsisdnWithValidPrefix_testAnonymization() throws AndesException {
        final String mccPrefix = "234";
        final String mncPrefix = "809";
        final String sn = "1234567";
        final String normalizedMsisdn = mccPrefix + "-" + mncPrefix + "-" + sn;
        final Result result = msisdnAnonymizer.anonymize(normalizedMsisdn);
        final String anonymizedMsisdn = result.getValue();

        System.out.println("Real MSISDN: " + normalizedMsisdn);
        System.out.println("Anonymized MSISDN: " + anonymizedMsisdn);

        assertTrue("Anonymization should be successful", result.getError() == Error.NONE);
        assertNotEquals("Real and anonymized MSISDNs should not the same", normalizedMsisdn, anonymizedMsisdn);
        assertTrue("Anonymization should preserve MCC", anonymizedMsisdn.startsWith(mccPrefix));

        final String expectedPrefix = mccPrefix + "-" + mncPrefix + "-";
        assertTrue("Anonymization should preserve MNC", anonymizedMsisdn.startsWith(expectedPrefix));
    }

    @Test
    public void givenMsisdnWithValidPrefix_testDeAnonymization() throws AndesException {
        final String mccPrefix = "234";
        final String mncPrefix = "809";
        final String sn = "1234567";
        final String normalizedMsisdn = mccPrefix + "-" + mncPrefix + "-" + sn;
        final Result result = msisdnAnonymizer.anonymize(normalizedMsisdn);
        final String anonymizedMsisdn = result.getValue();

        assertTrue("Anonymization should be successful", result.getError() == Error.NONE);
        assertNotEquals("Real and anonymized MSISDNs should not the same", normalizedMsisdn, anonymizedMsisdn);
        assertTrue("Anonymization should preserve MCC", anonymizedMsisdn.startsWith(mccPrefix));

        final String expectedPrefix = mccPrefix + "-" + mncPrefix + "-";
        assertTrue("Anonymization should preserve MNC", anonymizedMsisdn.startsWith(expectedPrefix));

        final Result deResult = msisdnAnonymizer.deAnonymize(anonymizedMsisdn);
        final String deanonymizedMsisdn = deResult.getValue();
        System.out.println("Real MSISDN: " + normalizedMsisdn);
        System.out.println("Anonymized MSISDN: " + anonymizedMsisdn);
        System.out.println("De-Anonymized MSISDN: " + deanonymizedMsisdn);
        assertTrue("De-Anonymized msisdn matched with actual msisdn.", deanonymizedMsisdn.equals(normalizedMsisdn));
    }
}
