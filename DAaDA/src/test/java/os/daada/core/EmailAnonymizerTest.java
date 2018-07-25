package os.daada.core;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import os.daada.core.AndesException;
import os.daada.core.Error;
import os.daada.core.Result;
import os.daada.core.crypt.AsymmetricEccWithAesCipher;
import os.daada.core.crypt.ICrypto;
import os.daada.core.pii.EmailAndesizer;
import os.daada.core.util.KeyPairUtils;

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
