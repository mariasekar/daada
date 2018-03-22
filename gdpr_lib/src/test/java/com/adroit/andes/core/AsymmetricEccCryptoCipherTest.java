	package com.adroit.andes.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.adroit.andes.core.crypt.AsymmetricEccCipher;
import org.adroit.andes.core.crypt.ICrypto;
import org.adroit.andes.core.util.KeyPairUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class AsymmetricEccCryptoCipherTest {

	public static final String ANDES_SECURITY_KEYS_PUBLIC = "anonymization.security.keys.public";
	public static final String ANDES_SECURITY_KEYS_PRIVATE = "anonymization.security.keys.private";

    private static ICrypto cryptoHandler;
    private static Config config;

    @BeforeClass
    public static void setUp() {
    	try {
    		config = ConfigFactory.load();
    		testGenerateKeyPair();
        	cryptoHandler = new AsymmetricEccCipher(config);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }

    private static void testGenerateKeyPair() throws Exception {
        final String publicKeyFilename = config.getString(ANDES_SECURITY_KEYS_PUBLIC);
        final String privateKeyFilename = config.getString(ANDES_SECURITY_KEYS_PRIVATE);

        KeyPairUtils.generateEcKeyPair("secp112r1", publicKeyFilename, privateKeyFilename);
    }

    // @Test
    public void testEncryptAndDecrypt() throws Exception {
        final String realMsisdn = "9940612345";
        final String anonymizedMsisdn = cryptoHandler.encrypt(realMsisdn);
        final String deAnonymizedMsisdn = cryptoHandler.decrypt(anonymizedMsisdn);

        System.out.println("Real MSISDN: " + realMsisdn);
        System.out.println("Anonymized MSISDN: " + anonymizedMsisdn);
        System.out.println("Deanonymized MSISDN: " + deAnonymizedMsisdn);

        assertNotEquals(realMsisdn, anonymizedMsisdn);
        assertEquals(deAnonymizedMsisdn, realMsisdn);
    }

    //@Test
    public void testConsistentEncryption() throws Exception {
        final String realMsisdn = "9940612345";
        final String anonymizedMsisdn = cryptoHandler.encrypt(realMsisdn);
        System.out.println("Anonymized MSISDN: " + anonymizedMsisdn);

        for (int i = 0; i < 10; i++) {
            final String anonymizedMsisdnAgain = cryptoHandler.encrypt(realMsisdn);
            System.out.println("Re-anonymized MSISDN: " + anonymizedMsisdnAgain);
            assertEquals("Encryption must be consistent", anonymizedMsisdn, anonymizedMsisdnAgain);
        }
    }

    //@Test
    public void testConsistentDecryption() throws Exception {
        final String realMsisdn = "9940612345";
        final String anonymizedMsisdn = cryptoHandler.encrypt(realMsisdn);
        System.out.println("Anonymized MSISDN: " + anonymizedMsisdn);

        for (int i = 0; i < 10; i++) {
            final String deanonymizedMsisdn = cryptoHandler.decrypt(anonymizedMsisdn);
            System.out.println("De-anonymized MSISDN: " + deanonymizedMsisdn);
            assertEquals(realMsisdn, deanonymizedMsisdn);
        }
    }
}
