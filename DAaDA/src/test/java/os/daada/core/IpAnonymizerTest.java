package os.daada.core;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import os.daada.core.AndesException;
import os.daada.core.Error;
import os.daada.core.Result;
import os.daada.core.crypt.ICrypto;
import os.daada.core.pii.IpAndesizer;

public class IpAnonymizerTest {

	private static IpAndesizer ip = null;
	private static Config andesConfig;
	private static ICrypto cryptoHandler;

	@BeforeClass
	public static void setUp() throws Exception {
		andesConfig = ConfigFactory.load();
		ip = new IpAndesizer(andesConfig, cryptoHandler);
	}

	@Test
	public void getIpTest() throws AndesException {

		final String ipaddr = "10.10.10.10";
		final Result result = ip.anonymize(ipaddr);
		final String final_result = result.getValue();
		final String expected_ip = "10.10.10.&&&";

		assertTrue("Replacement should be successful", result.getError() == Error.NONE);
		assertNotEquals("Original and replaced ip should not the same", ipaddr, final_result);
		assertEquals("Replaced and expected should be the same", expected_ip, final_result);

		System.out.println("Real ip:" + ipaddr);
		System.out.println("Truncated ip :" + final_result);

	}

	@Test
	public void getEmptyIpTest() throws AndesException {

		final String ipaddr = "";
		final Result result = ip.anonymize(ipaddr);
		final String final_result = result.getValue();

		assertTrue("Replacement should be successful", result.getError() == Error.ERROR_IP_UNKNOWN);
		assertEquals("Original and replaced ip should not the same", ipaddr, final_result);

		System.out.println("Real ip:" + ipaddr);
		System.out.println("Truncated ip :" + final_result);

	}

	@Test
	public void invaIplidtest() throws AndesException {

		final String ipaddr = "1234.678.1.1";
		final Result result = ip.anonymize(ipaddr);
		final String final_result = result.getValue();
		final String expected_result = "1234.678.1.1";

		assertTrue("Replacement should not be successful", result.getError() == Error.ERROR_IP_UNKNOWN);
		assertEquals("Original and Expected ip should be the same", ipaddr, expected_result);

		System.out.println("Real ip:" + ipaddr);
		System.out.println("Truncated ip :" + final_result);

	}

}
