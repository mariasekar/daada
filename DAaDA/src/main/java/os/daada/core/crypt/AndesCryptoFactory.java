package os.daada.core.crypt;

import com.typesafe.config.Config;

import os.daada.core.AndesException;
import os.daada.core.IAndes;
import os.daada.core.crypt.ICrypto.CryptAlgorithmType;

public final class AndesCryptoFactory {

	private final static AndesCryptoFactory ANDES_CRYPTO_FACTORY = new AndesCryptoFactory();

	public static final AndesCryptoFactory getAndesCryptoInstance() {
		return ANDES_CRYPTO_FACTORY;
	}

	public ICrypto getCrypter(Config config) throws AndesException {
		ICrypto iCrypto = null;
		CryptAlgorithmType cryptAlgorithmType = config.getEnum(CryptAlgorithmType.class, IAndes.ANDES_SECURITY_CRYPTO_ALGORITHM);
		if(cryptAlgorithmType != null) {
			switch(cryptAlgorithmType) {
				case RSA :
					iCrypto = new AsymmetricRsaCipher(config);
					break;
				case ECIES :
					iCrypto = new AsymmetricEccCipher(config);
					break;
				case ECDH :
					iCrypto = new AsymmetricEccWithAesCipher(config);
					break;
				case ELGAMAL :
					iCrypto = new AsymmetricElGamalCipher(config);
					break;
			}
		}
		return iCrypto;
	}
}
