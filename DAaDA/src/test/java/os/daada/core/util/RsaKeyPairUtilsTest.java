package os.daada.core.util;

import static org.junit.Assert.assertEquals;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Before;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import os.daada.core.cli.Cli;
import os.daada.core.cli.CliException;
import os.daada.core.util.KeyPairUtils;

public class RsaKeyPairUtilsTest {

	public static final String ANDES_SECURITY_KEYS_PUBLIC = "anonymization.security.keys.public";
	public static final String ANDES_SECURITY_KEYS_PRIVATE = "anonymization.security.keys.private";

	/** Fixture */
    private Cli cli;
    private Config andesConfig;

    @Before
    public void setUp() {

        // Initialize configurations
        andesConfig = ConfigFactory.load();
        cli = new Cli(andesConfig);
    }

    @Test
    public void testGenerateKeyPair() throws Exception {
        final int keySizeInBits = 512;
        final String publicKeyFilename = andesConfig.getString(ANDES_SECURITY_KEYS_PUBLIC);
        final String privateKeyFilename = andesConfig.getString(ANDES_SECURITY_KEYS_PRIVATE);

        KeyPairUtils.generateRsaKeyPair(keySizeInBits, publicKeyFilename, privateKeyFilename);
        PublicKey publicKey = KeyPairUtils.readRsaPublicKey(publicKeyFilename);
        PrivateKey privateKey = KeyPairUtils.readRsaPrivateKey(privateKeyFilename);

        assertEquals("Public key generation algorithm should be RSA", "RSA", publicKey.getAlgorithm());
        assertEquals("Private key generation algorithm should be RSA", "RSA", privateKey.getAlgorithm());
    }

    @Test
    public void givenOpenSslGeneratedKeys_test_KeysAreReadCorrectly() throws Exception {
    	final String publicKeyFilename = andesConfig.getString(ANDES_SECURITY_KEYS_PUBLIC);
        final String privateKeyFilename = andesConfig.getString(ANDES_SECURITY_KEYS_PRIVATE);

        final PublicKey publicKey = KeyPairUtils.readRsaPublicKey(publicKeyFilename);
        final PrivateKey privateKey = KeyPairUtils.readRsaPrivateKey(privateKeyFilename);

        assertEquals("Public key generation algorithm should be RSA", "RSA", publicKey.getAlgorithm());
        assertEquals("Private key generation algorithm should be RSA", "RSA", privateKey.getAlgorithm());
    }

    @Test
    public void givenAnonymizationOptionGroup_test_CliIsCorrect() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-t ODS.RATED_DATA_CDR ").append(" ")
                .append("-f input/rated_data_cdr.csv").append(" ")
                .append("-o output/rated_data_cdr.csv").append(" ")
                .append("-e error/rated_data_cdr.csv").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }
 
    @Test
    public void givenDeanonymizationOptionGroup_test_CliIsCorrect() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("-d").append(" ")
                .append("-t ODS.RATED_DATA_CDR ").append(" ")
                .append("-f output/rated_data_cdr.csv").append(" ")
                .append("-o output/rated_data_cdr.csv").append(" ")
                .append("-e error/rated_data_cdr.csv").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }

}
