package os.daada.core.crypt;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.typesafe.config.Config;

import os.daada.core.AndesException;

public abstract class AbstractCrypto implements ICrypto {

	protected Cipher cipher;
	protected final Config config;
	protected final CryptAlgorithmType cryptAlgorithmType;

	static {
		if(Security.getProvider("BC") == null) {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		}
	}

	public AbstractCrypto(final CryptAlgorithmType cryptAlgorithmType, final Config config) throws AndesException {
		this.config = config;
		this.cryptAlgorithmType = cryptAlgorithmType;
		try {
			this.cipher = Cipher.getInstance(this.cryptAlgorithmType.getCipherScheme(), CRYPTO_PROVIDER_BOUNCY_CASTLE);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException error) {
			throw new AndesException("Cipher creation error.", error);
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
	public abstract byte[] encrypt(byte[] value) throws AndesException;

	/**
     * De-crypt the given bytes of data
     *
     * @param cipherText
     *            The data to be decoded/decrypted
     * @return The decrypted/decoded bytes of data
     * @throws AndesException
     *             if an error occurred during the operation
     */
	public abstract byte[] decrypt(byte[] value)  throws AndesException;

	/**
     * Encrypt the given bytes of data using the public key
     *
     * @param value
     *            The data to be encoded or encrypted
     * @return The encrypted/encoded data
     * @throws AndesException
     *             if an error occurred during the operation
     */
	public String encrypt(String value) throws AndesException {
		byte[] hexEncodedCipher = this.encrypt(value.getBytes());
		return hexEncodedCipher != null && hexEncodedCipher.length > 0 ? Hex.encodeHexString(hexEncodedCipher) : value;
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
	public String decrypt(final String value)  throws AndesException {
		byte[] hexEncodedCipher;
		try {
			hexEncodedCipher = this.decrypt(Hex.decodeHex(value.toCharArray()));
		} catch (AndesException | DecoderException e) {
			throw new AndesException("Error occurred.", e);
		}
		return hexEncodedCipher != null && hexEncodedCipher.length > 0 ? new String(hexEncodedCipher) : ""; 
	}
}









































































































































































































































































































































































































































































































