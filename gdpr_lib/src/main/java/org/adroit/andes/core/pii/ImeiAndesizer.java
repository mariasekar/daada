package org.adroit.andes.core.pii;


import java.util.concurrent.ConcurrentMap;

import org.adroit.andes.core.AbstractAndes;
import org.adroit.andes.core.AndesException;
import org.adroit.andes.core.Error;
import org.adroit.andes.core.IPiiDataValidator;
import org.adroit.andes.core.Result;
import org.adroit.andes.core.crypt.ICrypto;

import com.typesafe.config.Config;

public class ImeiAndesizer extends AbstractAndes {

	private static final Result RESULT_UNKOWN_IMEI = new Result(null, Error.ERROR_UNKNOWN_IMEI);

	private static final ConcurrentMap<String, String> IMEI_CACHE = createMap("imei_cache");

	public ImeiAndesizer(final Config config, final ICrypto cyptoHandler) throws AndesException {
		super(config, cyptoHandler);
		this.piiDataValidator = new ImeiValidator();
	}

	public Result anonymize(String imei) throws AndesException {
		if(imei == null) {
			return RESULT_UNKOWN_IMEI;
		}
		String existingAnonymizedImei = IMEI_CACHE.get(imei);
		if(existingAnonymizedImei == null) {
			String[] validImeiParts = this.piiDataValidator.getValidParts(imei);
			if (validImeiParts != null) {
				StringBuilder anonymizedImei = new StringBuilder(150);
				anonymizedImei.append(validImeiParts[0]);
				anonymizedImei.append(validImeiParts[1]);
				anonymizedImei.append(this.cyptoHandler.encrypt(validImeiParts[2]));
				existingAnonymizedImei = anonymizedImei.toString();
				IMEI_CACHE.put(imei, existingAnonymizedImei);
				return new Result(existingAnonymizedImei, Error.NONE);
			} else {
				return RESULT_UNKOWN_IMEI;
			}
		}
		return new Result(existingAnonymizedImei, Error.NONE);
	}

	public Result deAnonymize(String anonymizedImei) throws AndesException {
		String existingDeanonymizedImei = IMEI_CACHE.get(anonymizedImei);
		if(existingDeanonymizedImei == null) {
			if (anonymizedImei != null && anonymizedImei.isEmpty()) {
				if (anonymizedImei.indexOf(IPiiDataValidator.DELIMITER_UNDERSCORE) == 9) {
					final StringBuilder deanonymizedImei = new StringBuilder(20);
					deanonymizedImei.append(anonymizedImei.substring(0, 8));
					deanonymizedImei.append(this.cyptoHandler.decrypt(anonymizedImei.substring(9)));
					existingDeanonymizedImei = deanonymizedImei.toString();
					IMEI_CACHE.put(anonymizedImei, existingDeanonymizedImei);
					return new Result(existingDeanonymizedImei, Error.NONE);
				} else {
					return RESULT_UNKOWN_IMEI;
				}
			} else{
				return RESULT_UNKOWN_IMEI;
			}
		} else {
			return new Result(existingDeanonymizedImei, Error.NONE);
		}
	}
}

