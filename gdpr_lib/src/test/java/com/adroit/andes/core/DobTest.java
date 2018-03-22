package com.adroit.andes.core;

import static org.junit.Assert.*;

import org.adroit.andes.core.AndesException;
import org.adroit.andes.core.Error;
import org.adroit.andes.core.Result;
import org.adroit.andes.core.crypt.ICrypto;
import org.adroit.andes.core.pii.DobAndesizer;
import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class DobTest {

	private static DobAndesizer dob = null;
	private static Config andesConfig;
	private static ICrypto cryptoHandler;

	@BeforeClass
	public static void setUp() throws Exception {
		// Initialize configurations
		andesConfig = ConfigFactory.load();
		dob = new DobAndesizer(andesConfig, cryptoHandler);
	}

	@Test
	public void givenDob_test() throws AndesException {

		final String full_dob = "2017-10-10 00:00:00";
		final Result final_result = dob.anonymize(full_dob);
		final String final_dob = final_result.getValue();
		final String expected_dob = "2017";

		assertTrue("Truncation should be successful", final_result.getError() == Error.NONE);
		assertNotEquals("Real and truncated DOBs should not be the same", full_dob, final_dob);
		assertEquals("Expected and final should be the same", expected_dob, final_dob);

		System.out.println("Real DOB: " + full_dob);
		System.out.println("Truncated DOB: " + final_dob);
	}

	@Test
	public void nullDob_test() throws AndesException {

		final String full_dob = "";
		final Result final_result = dob.anonymize(full_dob);
		final String final_dob = final_result.getValue();

		assertNotEquals("Truncation should be successful", final_result.getError() == Error.ERROR_DOB_UNKNOWN);
		assertNotEquals("Real and final DOBs should not be the same", full_dob, final_dob);

		System.out.println("Real DOB: " + full_dob);
		System.out.println("Final DOB: " + final_dob);

	}

	@Test
	public void brokenDobTest() throws AndesException {
		final String full_dob = "123";
		final Result final_result = dob.anonymize(full_dob);
		final String final_dob = final_result.getValue();

		assertNotEquals("Truncation should be successful", final_result.getError() == Error.ERROR_DOB_UNKNOWN);
		assertNotEquals("Real and final DOBs should not be the same", full_dob, final_dob);

		System.out.println("Real DOB: " + full_dob);
		System.out.println("Truncated DOB: " + final_dob);

	}

}