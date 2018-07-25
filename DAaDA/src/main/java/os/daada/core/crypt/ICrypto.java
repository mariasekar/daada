package os.daada.core.crypt;

import os.daada.core.AndesException;

public interface ICrypto {

	 enum CryptAlgorithmType {

		    /**
		     * The cipher scheme to use "NoPadding" is a must to ensure consistent
		     * encryption, though in theory this weakens the encryption strength
		     */
	    	RSA("RSA", "RSA", "RSA/ECB/NoPadding"),
	    	ECIES("EC", "EC", "ECIESwithAES-CBC"),
	    	ECDH("ECDH", "ECDH", "AES/CBC/PKCS7PADDING"),
	    	ELGAMAL("ElGamal", "ElGamal", "ElGamal/None/NoPadding")
	    	;
	    	private final String cryptAlgorithm;
	    	private final String cipherScheme;
	    	private final String keyPairAlgorithm;

	    	private CryptAlgorithmType(String cryptAlgorithm, String keyPairAlgorithm, String cipherScheme) {
	    		this.cryptAlgorithm = cryptAlgorithm;
	    		this.keyPairAlgorithm = keyPairAlgorithm;
	    		this.cipherScheme = cipherScheme;
	    	}

			public String getCipherScheme() {
				return cipherScheme;
			}

			public String getCryptAlgorithm() {
				return cryptAlgorithm;
			}

			public String getKeyPairAlgorithm() {
				return keyPairAlgorithm;
			}
	    }

	//public static final String CRYPTO_PROVIDER_SUN = "SUN";
	static final String CRYPTO_PROVIDER_BOUNCY_CASTLE = "BC";

	/**
     * Encrypt the given plain text based on the associated cryptography algorithm
     *
     * @param value
     *            The data to be encoded or encrypted
     * @return The encrypted/encoded data
     * @throws AndesException
     *             if an error occurred during the operation
     */
	String encrypt(String value) throws AndesException;

	/**
     * De-crypt the given data using the associated associated cryptography algorithm
     *
     * @param cipherText
     *            The data to be decoded/decrypted
     * @return The decrypted/decoded text
     * @throws AndesException
     *             if an error occurred during the operation
     */
	String decrypt(final String value)  throws AndesException;

}
