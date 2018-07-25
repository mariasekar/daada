package os.daada.core.crypt;

import java.security.InvalidKeyException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import com.typesafe.config.Config;

import os.daada.core.AndesException;
import os.daada.core.util.KeyPairUtils;

/**
 * <p>
 * The <code>AsymmetricEccCipher</code> class encrypts/decrypts (alternatively
 * encodes/decodes) data using asymmetric EC encryption.
 * </p>
 *
 * @author sekar
 *
 */
public class AsymmetricElGamalCipher extends BaseAsymmetricCipher {

    private final SecureRandom random;

    public AsymmetricElGamalCipher(final Config config) throws AndesException {
    	super(CryptAlgorithmType.ELGAMAL, config);
    	this.random = KeyPairUtils.createFixedRandom();
	}

    /**
     * Encrypt the given bytes of data using the ECC public key
     *
     * @param value
     *            The data to be encoded or encrypted
     * @return The encrypted/encoded data
     * @throws AndesException
     *             if an error occurred during the operation
     */
	public byte[] encrypt(byte[] value) throws AndesException {
		byte[] hexEncodedCipher = null;
		 try {
			 this.cipher.init(Cipher.ENCRYPT_MODE, this.publicKey, this.random);
			hexEncodedCipher = this.cipher.doFinal(value);
		 } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
	            throw new AndesException(e);
	     }
		 return hexEncodedCipher;
	}

	/**
     * Encrypt the given plain text using the Elgamal private key
     *
     * @param cipherText
     *            The data to be decoded/decrypted
     * @return The decrypted/decoded bytes of data
     * @throws AndesException
     *             if an error occurred during the operation
     */
	public byte[] decrypt(byte[] value)  throws AndesException {
		byte[] hexEncodedCipher = null;
		try {
			this.cipher.init(Cipher.DECRYPT_MODE, this.privateKey, this.random);
			hexEncodedCipher = this.cipher.doFinal(value);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new AndesException(e);
     }
		return hexEncodedCipher;
	}

}
