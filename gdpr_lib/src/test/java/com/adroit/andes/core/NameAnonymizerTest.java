package com.adroit.andes.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.adroit.andes.core.AndesException;
import org.adroit.andes.core.Error;
import org.adroit.andes.core.Result;
import org.adroit.andes.core.crypt.AsymmetricEccWithAesCipher;
import org.adroit.andes.core.crypt.ICrypto;
import org.adroit.andes.core.pii.NameAndesizer;
import org.adroit.andes.core.util.KeyPairUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class NameAnonymizerTest {

	public static final String ANDES_SECURITY_KEYS_PUBLIC = "anonymization.security.keys.public";
	public static final String ANDES_SECURITY_KEYS_PRIVATE = "anonymization.security.keys.private";

	private static NameAndesizer nameAnonymizer = null;
	private static Config config;
	private static ICrypto cryptoHandler;

	@BeforeClass
	public static void setUp() throws Exception {
		// Initialize configurations
		try {
			config = ConfigFactory.load();
			testGenerateKeyPair();
			cryptoHandler = new AsymmetricEccWithAesCipher(config);
			nameAnonymizer = new NameAndesizer(config, cryptoHandler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void testGenerateKeyPair() throws Exception {
		final String publicKeyFilename = config.getString(ANDES_SECURITY_KEYS_PUBLIC);
		final String privateKeyFilename = config.getString(ANDES_SECURITY_KEYS_PRIVATE);

		KeyPairUtils.generateEcKeyPair("prime256v1", publicKeyFilename, privateKeyFilename);
	}

	@Test
	public void givenNametestAnonymization() throws AndesException {

		final String realName = "anusha";

		final Result result = nameAnonymizer.anonymize(realName);
		final String anonymizedName = result.getValue();

		System.out.println("Real MSISDN: " + realName);
		System.out.println("Anonymized MSISDN: " + anonymizedName);

		assertTrue("Anonymization should be successful", result.getError() == Error.NONE);
		assertNotEquals("Real and anonymized MSISDNs should not the same", realName, anonymizedName);
		assertEquals("First letter of the word", anonymizedName, "a");
	}

	@Test
	public void givenNamewithspacetestAnonymization() throws AndesException {

		final String realName = "anu sha";

		final Result result = nameAnonymizer.anonymize(realName);
		final String anonymizedName = result.getValue();

		System.out.println("Real Name: " + realName);
		System.out.println("Anonymized NAME: " + anonymizedName);

		assertTrue("Anonymization should be successful", result.getError() == Error.NONE);
		assertNotEquals("Real and anonymized NAMEs should not the same", realName, anonymizedName);
		assertEquals("First letter of the word", anonymizedName, "a");
	}

	@Test
	public void givenNamewithNULLVALUEStestAnonymization() throws AndesException {

		final String realName = "";

		final Result result = nameAnonymizer.anonymize(realName);
		final String anonymizedName = result.getValue();

		System.out.println("Real NAME: " + realName);
		System.out.println("Anonymized NAME: " + anonymizedName);

		assertTrue("Anonymization should be successful", result.getError() == Error.ERROR_UNKNOWN_NAME);
		assertNotEquals("Real and anonymized NAMEs should not the same", realName, anonymizedName);

	}

}
