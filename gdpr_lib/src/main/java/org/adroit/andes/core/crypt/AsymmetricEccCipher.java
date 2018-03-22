package org.adroit.andes.core.crypt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.adroit.andes.core.AndesException;
import org.adroit.andes.core.IAndes;
import org.bouncycastle.jce.spec.IESParameterSpec;

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
public class AsymmetricEccCipher extends BaseAsymmetricCipher {

	 //  generate derivation and encoding vectors
    private final byte[]  d = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
    private final byte[]  e = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
    private final String initVector = "0000000000000000";
    private final IESParameterSpec ALGORITHM_PARAMETER_SPEC;

    public AsymmetricEccCipher(final Config config) throws AndesException {
    	super(CryptAlgorithmType.ECIES, config);
		try {
			ALGORITHM_PARAMETER_SPEC = new IESParameterSpec(d, e, 128, 128, this.initVector.getBytes(IAndes.CHARACTER_TYPE_UTF_8), true);
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
			 this.cipher.init(Cipher.ENCRYPT_MODE, this.publicKey, this.ALGORITHM_PARAMETER_SPEC);
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
			this.cipher.init(Cipher.DECRYPT_MODE, this.privateKey, this.ALGORITHM_PARAMETER_SPEC);
			hexEncodedCipher = this.cipher.doFinal(value);
		} catch (InvalidKeyException | IllegalBlockSizeException
	            | BadPaddingException | InvalidAlgorithmParameterException e) {
	        throw new AndesException(e);
	    }
		return hexEncodedCipher;
	}

}
