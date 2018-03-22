package org.adroit.andes.core.crypt;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.adroit.andes.core.AndesException;
import org.adroit.andes.core.IAndes;
import org.adroit.andes.core.util.KeyPairUtils;

import com.typesafe.config.Config;

/**
 * <p>
 * The <code>BaseAsymmetricCipher</code> class encrypts/decrypts (alternatively
 * encodes/decodes) data using asymmetric encryption.
 * </p>
 *
 * @author sekar
 *
 */
public class BaseAsymmetricCipher extends AbstractCrypto {

	protected PublicKey publicKey = null;
    protected PrivateKey privateKey = null;

    public BaseAsymmetricCipher(CryptAlgorithmType cryptAlgorithmType, final Config config) throws AndesException {
    	super(cryptAlgorithmType, config);
		this.loadKeyPairFiles();
	}

    /**
     * Sets the public & Private key files
     * @throws AndesException
     *             If an error occurred while reading the key from the specified
     *             files
     */
    private final void loadKeyPairFiles() throws AndesException {
    	final String privateKeyFilename = this.config.getString(IAndes.ANDES_SECURITY_KEYS_PRIVATE);
    	final String publicKeyFilename = this.config.getString(IAndes.ANDES_SECURITY_KEYS_PUBLIC);
    	try {
	    	switch(this.cryptAlgorithmType) {
	    		case RSA:
	    			this.privateKey = KeyPairUtils.readRsaPrivateKey(privateKeyFilename);
	    			this.publicKey = KeyPairUtils.readRsaPublicKey(publicKeyFilename);
	    			break;
	    		case ECIES:
	    		case ECDH:
	    			this.privateKey = KeyPairUtils.readEcPrivateKey(privateKeyFilename);
	    			this.publicKey = KeyPairUtils.readEcPublicKey(publicKeyFilename);
	    			break;
	    		case ELGAMAL:
	    			this.privateKey = KeyPairUtils.readElgamalPrivateKey(privateKeyFilename);
	    			this.publicKey = KeyPairUtils.readElgamalPublicKey(publicKeyFilename);
	    			break;
	    	}
    	} catch (Exception e) {
            throw new AndesException(e);
        }
    }
    
    /**
     * Encrypt the given bytes of data using the public key
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
			 synchronized (cipher) {
				 this.cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
				 hexEncodedCipher = this.cipher.doFinal(value);
			}
		 } catch (InvalidKeyException | IllegalBlockSizeException
	                | BadPaddingException e) {
	            throw new AndesException(e);
	     }
		 return hexEncodedCipher;
	}

	/**
     * Encrypt the given plain text using the private key
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
			synchronized (cipher) {
				this.cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
				hexEncodedCipher = this.cipher.doFinal(value);
			}
		} catch (InvalidKeyException | IllegalBlockSizeException
	            | BadPaddingException e) {
	        throw new AndesException(e);
	    }
		return hexEncodedCipher;
	}

}
