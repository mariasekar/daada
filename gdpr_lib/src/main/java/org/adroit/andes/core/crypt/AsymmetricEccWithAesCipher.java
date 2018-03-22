package org.adroit.andes.core.crypt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.adroit.andes.core.AndesException;
import org.adroit.andes.core.IAndes;
import org.adroit.andes.core.util.KeyPairUtils;

import com.typesafe.config.Config;

/**
 * <p>
 * The <code>AsymmetricEccCipher</code> class encrypts/decrypts (alternatively
 * encodes/decodes) data using asymmetric EC encryption.
 * </p>
 *
 * @author sekar
 *
 */
public class AsymmetricEccWithAesCipher extends BaseAsymmetricCipher {

	//  generate initialization vectors
    private final String initVector = "0000000000000000";

    private final IvParameterSpec ivSpec;
    private final SecretKey secretKey;

    public AsymmetricEccWithAesCipher(final Config config) throws AndesException {
    	super(CryptAlgorithmType.ECDH, config);
		try {
			ivSpec = new IvParameterSpec(this.initVector.getBytes(IAndes.CHARACTER_TYPE_UTF_8));
			this.secretKey = KeyPairUtils.generateSharedSecret(this.privateKey, this.publicKey);
		} catch (UnsupportedEncodingException e) {
			throw new AndesException("EC IES parameter spec creation error.");
		}
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
			 this.cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, this.ivSpec);
			hexEncodedCipher = this.cipher.doFinal(value);
		 } catch (InvalidKeyException | IllegalBlockSizeException
	                | BadPaddingException | InvalidAlgorithmParameterException e) {
	            throw new AndesException(e);
	     }
		 return hexEncodedCipher;
	}

	/**
     * Encrypt the given plain text using the ECC private key
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
			this.cipher.init(Cipher.DECRYPT_MODE, this.secretKey, this.ivSpec);
			hexEncodedCipher = this.cipher.doFinal(value);
		} catch (InvalidKeyException | IllegalBlockSizeException
	            | BadPaddingException | InvalidAlgorithmParameterException e) {
	        throw new AndesException(e);
	    }
		return hexEncodedCipher;
	}

}
