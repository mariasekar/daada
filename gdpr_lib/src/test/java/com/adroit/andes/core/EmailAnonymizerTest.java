package com.adroit.andes.core;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.adroit.andes.core.AndesException;
import org.adroit.andes.core.Error;
import org.adroit.andes.core.Result;
import org.adroit.andes.core.crypt.AsymmetricEccWithAesCipher;
import org.adroit.andes.core.crypt.ICrypto;
import org.adroit.andes.core.pii.EmailAndesizer;
import org.adroit.andes.core.util.KeyPairUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class EmailAnonymizerTest {

	public static final String ANDES_SECURITY_KEYS_PUBLIC = "anonymization.security.keys.public";
	public static final String ANDES_SECURITY_KEYS_PRIVATE = "anonymization.security.keys.private";

	private static EmailAndesizer emailAnonymizer = null;
	private static Config config;
	private static ICrypto cryptoHandler;

	@BeforeClass
	public static void setUp() throws Exception {
		// Initialize configurations
		try {
			config = ConfigFactory.load();
			testGenerateKeyPair();
			cryptoHandler = new AsymmetricEccWithAesCipher(config);
			emailAnonymizer = new EmailAndesizer(config, cryptoHandler);
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
	public void givenEmailAnonymization() throws AndesException {

		final String realEmail = "anushbula@gmail.com";
		final String expectedCharacter = "@";
		final Result result = emailAnonymizer.anonymize(realEmail);
		final String anonymizedEmail = result.getValue();

		System.out.println("Real EMAIL: " + realEmail);
		System.out.println("Anonymized EMAIL: " + anonymizedEmail);

		assertTrue("Anonymization should be successful", result.getError() == Error.NONE);
		assertNotEquals("Real and anonymized EMAILs should not the same", realEmail, anonymizedEmail);
		assertTrue("Anonymization should preserve @", anonymizedEmail.contains(expectedCharacter));
	}

	@Test
	public void givenEmailWithNullAnonymization() throws AndesException {

		final String realEmail = "";
		final Result result = emailAnonymizer.anonymize(realEmail);
		final String anonymizedEmail = result.getValue();

		System.out.println("Real EMAIL: " + realEmail);
		System.out.println("Anonymized EMAIL: " + anonymizedEmail);

		assertTrue("Anonymization should be successful", result.getError() == Error.ERROR_UNKNOWN_EMAIL);
		assertNotEquals("Real and anonymized MSISDNs should not the same", realEmail, anonymizedEmail);
		// assertEquals("anonymized email should always be
		// null",anonymizedEmail,nullvalue);
	}

	@Test
	public void givenEmailWithSpecialCharacters() throws AndesException {
		final String realEmail = "anush#beaula@gmail";
		final Result result = emailAnonymizer.anonymize(realEmail);
		final String anonymizedEmail = result.getValue();

		System.out.println("Real EMAIL: " + realEmail);
		System.out.println("Anonymized EMAIL: " + anonymizedEmail);
		assertTrue("Anonymization should be successful", result.getError() == Error.ERROR_UNKNOWN_EMAIL);
	}

}
