package org.adroit.andes.core.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;

import org.adroit.andes.core.AndesException;
import org.adroit.andes.core.crypt.ICrypto.CryptAlgorithmType;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * <p>
 * The <code>KeyPairUtils</code> class contains utility methods to generate,
 * read and write key-pairs for asymmetric encryption.
 * </p>
 *
 * @author sekar
 *
 */
public final class KeyPairUtils {

	static {
		if(Security.getProvider("BC") == null) {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		}
	}

	// RSA Cryptography related stuffs
	private static final String ANDES_KEY_TYPE_RSA_PUBLIC_KEY = "RSA PUBLIC KEY";
	private static final String ANDES_KEY_TYPE_RSA_PRIVATE_KEY = "RSA PRIVATE KEY";

	// Elliptic curve (EC) Cryptography related stuffs
	private static final String ANDES_KEY_TYPE_EC_PUBLIC_KEY = "EC Public Key";
	private static final String ANDES_KEY_TYPE_EC_PRIVATE_KEY = "EC PRIVATE KEY";
	private static final String ANDES_KEY_TYPE_EC_PARAMETERS = "EC PARAMETERS";
	
	private static final String ANDES_KEY_TYPE_CERTIFICATE = "CERTIFICATE";
	private static final String ANDES_KEY_TYPE_PUBLIC_KEY = "PUBLIC KEY";
	private static final String ANDES_KEY_TYPE_PRIVATE_KEY = "PRIVATE KEY";

	private static final Logger LOGGER = LoggerFactory.getLogger(KeyPairUtils.class);

    /** Utility classes should not have a public or default constructor. */
    private KeyPairUtils() {
    }

    /**
     * Generate an RSA key-pair
     *
     * @param keySizeInBits
     *            The key-size (in bits) e.g. 512, 1024
     * @param publicKeyFilename
     *            The file, including complete path name, in which the public
     *            key is to be stored
     * @param privateKeyFilename
     *            The file, including complete path name, in which the private
     *            key is to be stored
     * @throws Exception
     *             if the key-pair could not be generated
     */
    public static void generateRsaKeyPair(final int keySizeInBits, final String publicKeyFilename,
            final String privateKeyFilename) throws Exception {

        final KeyPairGenerator kpGen = KeyPairGenerator.getInstance(CryptAlgorithmType.RSA.getKeyPairAlgorithm());
        kpGen.initialize(keySizeInBits);
        final KeyPair keyPair = kpGen.generateKeyPair();

        writePemFile(keyPair.getPublic(), ANDES_KEY_TYPE_RSA_PUBLIC_KEY, publicKeyFilename);
        writePemFile(keyPair.getPrivate(), ANDES_KEY_TYPE_RSA_PRIVATE_KEY, privateKeyFilename);
    }

    /**
     * Read an RSA public key from file
     *
     * @param publicKeyFilename
     *            The file, including complete path name, from which the public
     *            key is to be read
     * @return The RSA public key
     * @throws Exception
     *             if the file could not be read
     */
    public static PublicKey readRsaPublicKey(final String publicKeyFilename) throws Exception {

        if (! Files.exists(Paths.get(publicKeyFilename))) {
            throw new FileNotFoundException(publicKeyFilename);
        }
        return readPublicKey(CryptAlgorithmType.RSA.getKeyPairAlgorithm(), publicKeyFilename);
    }

    /**
     * Read an RSA private key from file
     *
     * @param privateKeyFilename
     *            The file, including complete path name, from which the private
     *            key is to be read
     * @return The RSA private key
     * @throws Exception
     *             if the file could not be read
     */
    public static PrivateKey readRsaPrivateKey(final String privateKeyFilename) throws Exception {

        if (! Files.exists(Paths.get(privateKeyFilename))) {
            throw new FileNotFoundException(privateKeyFilename);
        }
        return readPrivateKey(CryptAlgorithmType.RSA.getKeyPairAlgorithm(), privateKeyFilename);
    }

    /**
     * Read an EC public key from file
     *
     * @param publicKeyFilename
     *            The file, including complete path name, from which the public
     *            key is to be read
     * @return The EC public key
     * @throws Exception
     *             if the file could not be read
     */
    public static PublicKey readEcPublicKey(final String publicKeyFilename) throws Exception {

        if (! Files.exists(Paths.get(publicKeyFilename))) {
            throw new FileNotFoundException(publicKeyFilename);
        }
        return readPublicKey(CryptAlgorithmType.ECIES.getKeyPairAlgorithm(), publicKeyFilename);
    }

    /**
     * Read an EC private key from file
     *
     * @param privateKeyFilename
     *            The file, including complete path name, from which the private
     *            key is to be read
     * @return The EC private key
     * @throws Exception
     *             if the file could not be read
     */
    public static PrivateKey readEcPrivateKey(final String privateKeyFilename) throws Exception {

        if (! Files.exists(Paths.get(privateKeyFilename))) {
            throw new FileNotFoundException(privateKeyFilename);
        }
        return readPrivateKey(CryptAlgorithmType.ECIES.getKeyPairAlgorithm(), privateKeyFilename);
    }

    /**
     * Read an EC public key from file
     *
     * @param publicKeyFilename
     *            The file, including complete path name, from which the public
     *            key is to be read
     * @return The EC public key
     * @throws Exception
     *             if the file could not be read
     */
    public static PublicKey readElgamalPublicKey(final String publicKeyFilename) throws Exception {

        if (! Files.exists(Paths.get(publicKeyFilename))) {
            throw new FileNotFoundException(publicKeyFilename);
        }
        return readPublicKey(CryptAlgorithmType.ELGAMAL.getKeyPairAlgorithm(), publicKeyFilename);
    }

    /**
     * Read an EC private key from file
     *
     * @param privateKeyFilename
     *            The file, including complete path name, from which the private
     *            key is to be read
     * @return The EC private key
     * @throws Exception
     *             if the file could not be read
     */
    public static PrivateKey readElgamalPrivateKey(final String privateKeyFilename) throws Exception {

        if (! Files.exists(Paths.get(privateKeyFilename))) {
            throw new FileNotFoundException(privateKeyFilename);
        }
        return readPrivateKey(CryptAlgorithmType.ELGAMAL.getKeyPairAlgorithm(), privateKeyFilename);
    }

    private static void writePemFile(final PublicKey publicKey, final String typeString, final String pemFileName) throws IOException {
        final PemObject o = new PemObject(typeString, publicKey.getEncoded());
        writePemFile(o, pemFileName);
    }

    private static void writePemFile(final PrivateKey privateKey, String typeString, final String pemFileName) throws Exception {
        final PemObject o = new PemObject(typeString, privateKey.getEncoded());
        writePemFile(o, pemFileName);
    }

    private static PublicKey readPublicKey(final String cryptAlgorithmType, final String publickeyFilename) throws AndesException {
        try {
            final PemObject o = readPemFile(publickeyFilename);
            final String keyType = o.getType();
            LOGGER.debug("Key type in file {}: {}", publickeyFilename, keyType);

            PublicKey publicKey = null;
            if (ANDES_KEY_TYPE_PUBLIC_KEY.equals(keyType) || ANDES_KEY_TYPE_RSA_PUBLIC_KEY.equals(keyType)
            		|| ANDES_KEY_TYPE_EC_PUBLIC_KEY.equals(keyType) || ANDES_KEY_TYPE_EC_PARAMETERS.equals(keyType)) {
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(o.getContent());
                publicKey = KeyFactory.getInstance(cryptAlgorithmType, "BC").generatePublic(publicKeySpec);
            } else if(ANDES_KEY_TYPE_CERTIFICATE.equals(keyType)) {
            	CertificateFactory x509CertificateObject = new CertificateFactory();
            	Certificate certificate = x509CertificateObject.engineGenerateCertificate(new ByteArrayInputStream(o.getContent()));
            	publicKey = certificate.getPublicKey();
            } else {
                throw new AndesException("Unrecognized key type: " + keyType + " in file: " + publickeyFilename);
            }
            return publicKey;
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | CertificateException | NoSuchProviderException e) {
            throw new AndesException(e);
        }
    }

    private static PrivateKey readPrivateKey(final String cryptAlgorithmType, final String privateKeyFilename) throws AndesException {
        try {
            final PemObject o = readPemFile(privateKeyFilename);
            final String keyType = o.getType();
            LOGGER.debug("Key type in file {}: {}", privateKeyFilename, keyType);

            PrivateKey privateKey = null;
            if (ANDES_KEY_TYPE_PRIVATE_KEY.equals(o.getType()) || ANDES_KEY_TYPE_RSA_PRIVATE_KEY.equals(keyType)
            		|| ANDES_KEY_TYPE_EC_PRIVATE_KEY.equals(keyType)) {
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(o.getContent());
                privateKey = KeyFactory.getInstance(cryptAlgorithmType, "BC").generatePrivate(privateKeySpec);
            } else {
                throw new AndesException("Unrecognized key type: " + keyType + " in file: " + privateKeyFilename);
            }
            return privateKey;
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new AndesException(e);
        }
    }

    private static PemObject readPemFile(final String pemFilename) throws IOException {
        PemReader pemReader = null;
        try {
            pemReader = new PemReader(new FileReader(pemFilename));
            final PemObject o = pemReader.readPemObject();
            if (o == null) {
                // PEM file is corrupted or is not in a format not recognized
                throw new IOException("Could not read PEM file (possibly corrupted): " + pemFilename);
            }
            LOGGER.debug("Contents of PEM file {}: {}", pemFilename, o.getContent());
            return o;
        } finally {
            if (pemReader != null) {
                pemReader.close();
            }
        }
    }

    private static void writePemFile(final PemObject o, final String pemFileName) throws IOException {
        final File outFile = new File(pemFileName);
        outFile.createNewFile();
        final PemWriter writer = new PemWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
        try {
            writer.writeObject(o);
        } finally {
            writer.close();
        }

    }

    /**
     * Generate an EC key-pair
     * 
     * RSA key size (bits)	ECC key size (bits)
			1024				160
			2048				224
			3072				256
			7680				384
			15360				521
     * 
     *
     * @param publicKeyFilename
     *            The file, including complete path name, in which the public
     *            key is to be stored
     * @param privateKeyFilename
     *            The file, including complete path name, in which the private
     *            key is to be stored
     * @throws Exception
     *             if the key-pair could not be generated
     */
    public static void generateEcKeyPair(final String keySizeParameter, final String publicKeyFilename,
            final String privateKeyFilename) throws Exception {

        final KeyPairGenerator kpGen = KeyPairGenerator.getInstance(CryptAlgorithmType.ECIES.getKeyPairAlgorithm(), "BC");
     // initializing parameter specs secp256r1/prime192v1
        //ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(keySizeParameter);
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(keySizeParameter);

        //kpGen.initialize(ecSpec);
        kpGen.initialize(ecGenParameterSpec);
        final KeyPair keyPair = kpGen.generateKeyPair();

        writePemFile(keyPair.getPublic(), ANDES_KEY_TYPE_PUBLIC_KEY, publicKeyFilename);
        writePemFile(keyPair.getPrivate(), ANDES_KEY_TYPE_PRIVATE_KEY, privateKeyFilename);
    }

    /**
     * Generate an Elgamal key-pair
     * 
     * RSA key size (bits)	ECC key size (bits)
			1024				160
			2048				224
			3072				256
			7680				384
			15360				521
     * 
     *
     * @param publicKeyFilename
     *            The file, including complete path name, in which the public
     *            key is to be stored
     * @param privateKeyFilename
     *            The file, including complete path name, in which the private
     *            key is to be stored
     * @throws Exception
     *             if the key-pair could not be generated
     */
    public static void generateElgamalKeyPair(final int keySizeInBits, final String publicKeyFilename,
            final String privateKeyFilename) throws Exception {

        final KeyPairGenerator kpGen = KeyPairGenerator.getInstance(CryptAlgorithmType.ELGAMAL.getKeyPairAlgorithm(), "BC");

        kpGen.initialize(keySizeInBits);
        final KeyPair keyPair = kpGen.generateKeyPair();

        writePemFile(keyPair.getPublic(), ANDES_KEY_TYPE_EC_PUBLIC_KEY, publicKeyFilename);
        writePemFile(keyPair.getPrivate(), ANDES_KEY_TYPE_EC_PRIVATE_KEY, privateKeyFilename);
    }

    public static SecretKey generateSharedSecret(PrivateKey privateKey,
            PublicKey publicKey) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "BC");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);

            SecretKey key = keyAgreement.generateSecret("AES");
            return key;
        } catch (InvalidKeyException | NoSuchAlgorithmException
                | NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    private static final class FixedRand extends SecureRandom {

		private static final long serialVersionUID = 1L;
		MessageDigest sha;
        byte[] state;

        FixedRand() {
            try {
                this.sha = MessageDigest.getInstance("SHA-1");
                this.state = sha.digest();
            }
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("can't find SHA-1!");
            }
        }

	    public void nextBytes(byte[] bytes){
	        int	off = 0;
	        sha.update(state);
	        while (off < bytes.length) {
	            state = sha.digest();
	            if (bytes.length - off > state.length) {
	                System.arraycopy(state, 0, bytes, off, state.length);
	            }
	            else {
	                System.arraycopy(state, 0, bytes, off, bytes.length - off);
	            }
	            off += state.length;
	            sha.update(state);
	        }
	    }
    }

    /**
     * Return a SecureRandom which produces the same value.
     * <b>This is for testing only!</b>
     * @return a fixed random
     */
    public static SecureRandom createFixedRandom()
    {
        return new FixedRand();
    }

    private static String	digits = "0123456789abcdef";

    /**
     * Return length many bytes of the passed in byte array as a hex string.
     *
     * @param data the bytes to be converted.
     * @param length the number of bytes in the data block to be converted.
     * @return a hex representation of length bytes of data.
     */
    public static String toHex(byte[] data, int length)
    {
        StringBuffer	buf = new StringBuffer();

        for (int i = 0; i != length; i++)
        {
            int	v = data[i] & 0xff;

            buf.append(digits.charAt(v >> 4));
            buf.append(digits.charAt(v & 0xf));
        }

        return buf.toString();
    }

    /**
     * Return the passed in byte array as a hex string.
     *
     * @param data the bytes to be converted.
     * @return a hex representation of data.
     */
    public static String toHex(byte[] data)
    {
        return toHex(data, data.length);
    }
}
