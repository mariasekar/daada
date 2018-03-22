package com.adroit.andes.core.util.security;

import org.adroit.andes.core.IAndes;
import org.adroit.andes.core.cli.Cli;
import org.adroit.andes.core.cli.CliException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class DependentKeyPairTest {

	public static final String ANDES_SECURITY_KEYS_PUBLIC = "anonymization.security.keys.public";
	public static final String ANDES_SECURITY_KEYS_PRIVATE = "anonymization.security.keys.private";

	/** Fixture */
    private static Cli cli;
    private static Config andesConfig;
    
    @BeforeClass
    public static void setUp() {
    	System.setProperty("anonymization.security.keys.public", "publicfolder");
    	System.setProperty("anonymization.security.keys.private", "privatefolder");
        // Initialize configurations
        andesConfig = ConfigFactory.load();
        cli = new Cli(andesConfig);
    }

    /*
     * Dependent arguments tests
     */

    // Anonymize option without public key option

    @Test(expected = CliException.class)
    public void givenAnomymizeOption_AndNoPublicKeyOption_test_CliIsWrong() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-t customer_dim ").append(" ")
                .append("-f input/customer_dim.csv").append(" ")
                .toString();
        System.out.println(andesConfig.getString(IAndes.ANDES_SECURITY_KEYS_PUBLIC));
        cli.parse(commandLine.split("\\s"));
    }

    // Deanonymize option without private key option

    @Test(expected = CliException.class)
    public void givenDeanomymizeOption_AndNoPrivateKeyOption_test_CliThrowsException() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("-d").append(" ")
                .append("-t customer_dim ").append(" ")
                .append("-f output/customer_dim.csv").append(" ")
                .toString();
        System.out.println(andesConfig.getString(IAndes.ANDES_SECURITY_KEYS_PRIVATE));
        cli.parse(commandLine.split("\\s"));
    } 
}
