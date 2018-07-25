package os.daada.core.pii;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

//import org.redisson.api.RedissonClient;

import com.typesafe.config.Config;

import os.daada.core.AbstractAndes;
import os.daada.core.AndesException;
import os.daada.core.Error;
import os.daada.core.IPiiDataValidator;
import os.daada.core.Result;
import os.daada.core.crypt.ICrypto;

public class ImsiAndesizer extends AbstractAndes {

	private static final Result RESULT_UNKOWN_IMSI = new Result(null, Error.ERROR_UNKNOWN_IMSI);
	private static final ConcurrentMap<String, String> IMSI_CACHE = createMap("imsi_cache");

	public ImsiAndesizer(final Config config, final ICrypto cyptoHandler)
			throws NumberFormatException, IOException, AndesException {
		super(config, cyptoHandler);
		this.piiDataValidator = new ImsiValidator(config);
	}

	@Override
	public Result anonymize(String imsi) throws AndesException {

		if(imsi == null) {
			return RESULT_UNKOWN_IMSI;
		}
		String existingAnonymizedImsi = IMSI_CACHE.get(imsi);
		if(existingAnonymizedImsi == null) {
			String[] imsiParts = this.piiDataValidator.getValidParts(imsi);
			if (imsiParts != null) {
				StringBuilder anonymizedimsi = new StringBuilder(150);
				anonymizedimsi.append(imsiParts[0]);
				anonymizedimsi.append(IPiiDataValidator.DELIMITER_UNDERSCORE);
				anonymizedimsi.append(imsiParts[1]);
				anonymizedimsi.append(IPiiDataValidator.DELIMITER_UNDERSCORE);
				anonymizedimsi.append(this.cyptoHandler.encrypt(imsiParts[2]));
				existingAnonymizedImsi = anonymizedimsi.toString();
				IMSI_CACHE.put(imsi, existingAnonymizedImsi);
				return new Result(existingAnonymizedImsi, Error.NONE);
			} else {
				return RESULT_UNKOWN_IMSI;
			}
		} 
		else {
			//return new Result(imsi, Error.NONE);
			return new Result(existingAnonymizedImsi, Error.NONE);
		}
	}

	@Override
	public Result deAnonymize(String anonymizedimsi) throws AndesException {
		String existingDeAnonymizedImsi = IMSI_CACHE.get(anonymizedimsi);
		if(existingDeAnonymizedImsi == null) {
			if (anonymizedimsi == null) {
				return RESULT_UNKOWN_IMSI;
			}
			int msinIndex = anonymizedimsi.lastIndexOf(IPiiDataValidator.DELIMITER_UNDERSCORE) + 1;
			StringBuilder deAnonymizedImsi = new StringBuilder(20);
			deAnonymizedImsi.append(anonymizedimsi.substring(0, msinIndex));
			String msin = anonymizedimsi.substring(msinIndex);
			deAnonymizedImsi.append(this.cyptoHandler.decrypt(msin));
			existingDeAnonymizedImsi = deAnonymizedImsi.toString();
			IMSI_CACHE.put(anonymizedimsi, existingDeAnonymizedImsi);
			return new Result(existingDeAnonymizedImsi, Error.NONE);	
		} else {
			return new Result(anonymizedimsi, Error.NONE);
		}
		
	}
}



