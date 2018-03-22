package com.adroit.andes.core;

import static org.junit.Assert.*;

import org.adroit.andes.core.AndesException;
import org.adroit.andes.core.Error;
import org.adroit.andes.core.Result;
import org.adroit.andes.core.crypt.AsymmetricRsaCipher;
import org.adroit.andes.core.crypt.ICrypto;
import org.adroit.andes.core.pii.ImeiAndesizer;
import org.adroit.andes.core.util.KeyPairUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ImeiAnonymizerTest {
	
	public static final String ANDES_SECURITY_KEYS_PUBLIC = "anonymization.security.keys.public";
	public static final String ANDES_SECURITY_KEYS_PRIVATE = "anonymization.security.keys.private";
	private static ImeiAndesizer imei = null;
	private static Config andesConfig;
	private static ICrypto cryptoHandler;

	@BeforeClass
	public static void setUp() throws Exception {
		andesConfig = ConfigFactory.load();
    	testGenerateKeyPair();
    	cryptoHandler = new AsymmetricRsaCipher(andesConfig);
		imei = new ImeiAndesizer(andesConfig, cryptoHandler);
	}
	
	 private static void testGenerateKeyPair() throws Exception {
	    	final int keySizeInBits = 512;
	        final String publicKeyFilename = andesConfig.getString(ANDES_SECURITY_KEYS_PUBLIC);
	        final String privateKeyFilename = andesConfig.getString(ANDES_SECURITY_KEYS_PRIVATE);

	        KeyPairUtils.generateRsaKeyPair(keySizeInBits, publicKeyFilename, privateKeyFilename);

	    }
	 @Test
	 public void getValidImeiTest() throws AndesException{
		 
		 final String valid_imei = "357215061348861";
		 final Result result = imei.anonymize(valid_imei);
		 final String final_result = result.getValue();
		 
		 assertTrue("Real and anonymized imei should not be the same",result.getError() == Error.NONE);
		 assertNotEquals("Real and anonymized imei should not be the same", valid_imei,final_result);
		 System.out.println("Real imei:" + valid_imei);
			System.out.println("Anonymized imei :" + final_result);
		  }
	
	 @Test
	public void getIvalidImeiTest() throws AndesException{
		
		final String first_imei = "012345678912";
		final Result result = imei.anonymize(first_imei);
		final String final_result = result.getValue();
		final String expected_imei = "012345678912";
		
		assertTrue("Replacement should not be successful", result.getError() == Error.ERROR_UNKNOWN_IMEI);
		assertEquals("Original and replaced imei should be the same", first_imei, expected_imei);
		assertNotEquals("Original and expected should not be the  same",first_imei, final_result);
		System.out.println("Real imei:" + first_imei);
		System.out.println("Anonymized imei :" + final_result);
	}
 @Test
	 public void getEmptyImeiTest() throws AndesException{
			
			final String first_imei = "";
			final Result result = imei.anonymize(first_imei);
			final String final_result = result.getValue();
			
			assertTrue("Replacement should not be successful", result.getError() == Error.ERROR_UNKNOWN_IMEI);
			assertNotEquals("Original and final should not be the  same",first_imei, final_result);
			System.out.println("Real imei:" + first_imei);
			System.out.println("Anonymized imei :" + final_result);
	 }
	@Test
	 public void getBrokenImeiTest() throws AndesException{
			
			final String first_imei = "3596050733549";
			final Result result = imei.anonymize(first_imei);
			final String final_result = result.getValue();
			final String expected_imei = "3596050733549";
			
			assertTrue("Replacement should not be successful", result.getError() == Error.ERROR_UNKNOWN_IMEI);
			assertEquals("Original and replaced imei should be the same", first_imei, expected_imei);
			assertNotEquals("Original and expected should not be the  same",first_imei, final_result);
			System.out.println("Real imei:" + first_imei);
			System.out.println("Anonymized imei :" + final_result);
	 }
}
