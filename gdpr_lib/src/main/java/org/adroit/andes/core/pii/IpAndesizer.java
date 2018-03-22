package org.adroit.andes.core.pii;

import java.io.IOException;

import org.adroit.andes.core.AbstractAndes;
import org.adroit.andes.core.AndesException;
import org.adroit.andes.core.Error;
import org.adroit.andes.core.Result;
import org.adroit.andes.core.crypt.ICrypto;

import com.typesafe.config.Config;

public class IpAndesizer extends AbstractAndes {

	private static final int IP_LENGTH = "255.255.255.x".length();
	private final String ipAddressMasker;

	public IpAndesizer(Config config, ICrypto cyptoHandler) throws AndesException, IOException {
		super(config, cyptoHandler);
		this.piiDataValidator = new IpValidator(config);
		this.ipAddressMasker = ((IpValidator)this.piiDataValidator).getMaskingChar();
	}

	@Override
	public Result anonymize(final String ipaddr) throws AndesException {
		if(ipaddr != null) {
			String[] validIpParts = this.piiDataValidator.getValidParts(ipaddr);
			if (validIpParts != null) {
				StringBuilder finalIp = new StringBuilder(IP_LENGTH);
				finalIp.append(validIpParts[0]);
				finalIp.append(IpValidator.DELIMITOR_DOT);
				finalIp.append(this.ipAddressMasker);
				return new Result(finalIp.toString(), Error.NONE);
			}
		}
		return new Result(ipaddr, Error.ERROR_IP_UNKNOWN);
	}

	@Override
	public Result deAnonymize(String ipaddr) throws AndesException {
		return new Result(ipaddr, Error.NONE);
	}

}
