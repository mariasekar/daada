package org.adroit.andes.core.crypt;

import org.adroit.andes.core.AndesException;

import com.typesafe.config.Config;

/**
 * <p>
 * The <code>AsymmetricRsaCipher</code> class encrypts/decrypts (alternatively
 * encodes/decodes) data using asymmetric encryption.
 * </p>
 *
 * @author sekar
 *
 */
public class AsymmetricRsaCipher extends BaseAsymmetricCipher {

    public AsymmetricRsaCipher(final Config config) throws AndesException {
    	super(CryptAlgorithmType.RSA, config);
	}
}
