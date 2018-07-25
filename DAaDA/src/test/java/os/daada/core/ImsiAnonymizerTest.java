package os.daada.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import os.daada.core.AndesException;
import os.daada.core.Error;
import os.daada.core.Result;
import os.daada.core.crypt.AsymmetricRsaCipher;
import os.daada.core.crypt.ICrypto;
import os.daada.core.pii.ImsiAndesizer;
import os.daada.core.util.KeyPairUtils;

public class ImsiAnonymizerTest {
	public static final String ANDES_SECURITY_KEYS_PUBLIC = "anonymization.security.keys.public";
	public static final String ANDES_SECURITY_KEYS_PRIVATE = "anonymization.security.keys.private";
	private static ImsiAndesizer imsi = null;
	private static Config andesConfig;
	private static ICrypto cryptoHandler;
	
	@BeforeClass
	public static void setUp() throws Exception {
		andesConfig = ConfigFactory.load();
    	testGenerateKeyPair();
    	cryptoHandler = new AsymmetricRsaCipher(andesConfig);
		imsi = new ImsiAndesizer(andesConfig, cryptoHandler);
	}
	
	 private static void testGenerateKeyPair() throws Exception {
	    	final int keySizeInBits = 512;
	        final String publicKeyFilename = andesConfig.getString(ANDES_SECURITY_KEYS_PUBLIC);
	        final String privateKeyFilename = andesConfig.getString(ANDES_SECURITY_KEYS_PRIVATE);

	        KeyPairUtils.generateRsaKeyPair(keySizeInBits, publicKeyFilename, privateKeyFilename);

	    }
	 
	 @Test
	 public void getValidImsiTest() throws AndesException{
		 
		 final String valid_imsi = "621600074735189";
		 final Result result = imsi.anonymize(valid_imsi);
		 final String final_result = result.getValue();
		 
		 assertTrue("Real and anonymized imsi should not be the same",result.getError() == Error.NONE);
		 assertNotEquals("Real and anonymized imsi should not be the same", valid_imsi,final_result);
		 System.out.println("Real imsi:" + valid_imsi);
			System.out.println("Anonymized imsi :" + final_result);
		  }
	 @Test
		public void getInvalidImsiTest() throws AndesException{
			
			final String first_imsi = "625602303154077";
			final Result result = imsi.anonymize(first_imsi);
			final String final_result = result.getValue();
			final String expected_imei = "625602303154077";
			
			assertTrue("Replacement should not be successful", result.getError() == Error.ERROR_UNKNOWN_IMSI);
			assertEquals("Original and replaced imsi should be the same", first_imsi, expected_imei);
			assertNotEquals("Original and expected should not be the  same",first_imsi, final_result);
			System.out.println("Real imsi:" + first_imsi);
			System.out.println("Anonymized imsi :" + final_result);
		}
	
 @Test
	 public void getEmptyImsiTest() throws AndesException{
			
			final String first_imsi = "";
			final Result result = imsi.anonymize(first_imsi);
			final String final_result = result.getValue();
			
			assertTrue("Replacement should not be successful", result.getError() == Error.ERROR_UNKNOWN_IMSI);
			assertNotEquals("Original and final should not be the  same",first_imsi, final_result);
			
			System.out.println("Real imsi:" + first_imsi);
			System.out.println("Anonymized imsi :" + final_result);
		 
	 }
 
 @Test
 public void getBrokenImsiTest() throws AndesException{
		
		final String first_imsi = "3596050733549";
		final Result result = imsi.anonymize(first_imsi);
		final String final_result = result.getValue();
		final String expected_imei = "3596050733549";
		
		assertTrue("Replacement should not be successful", result.getError() == Error.ERROR_UNKNOWN_IMSI);
		assertEquals("Original and replaced imei should be the same", first_imsi, expected_imei);
		assertNotEquals("Original and expected should not be the  same",first_imsi, final_result);
		System.out.println("Real imei:" + first_imsi);
		System.out.println("Anonymized imei :" + final_result);
 }
	
}