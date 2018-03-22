package org.adroit.andes.core.pii;

import java.io.IOException;

import org.adroit.andes.core.AbstractAndes;
import org.adroit.andes.core.AndesException;
import org.adroit.andes.core.Error;
import org.adroit.andes.core.Result;
import org.adroit.andes.core.crypt.ICrypto;

import com.typesafe.config.Config;

public class DobAndesizer extends AbstractAndes {

	private static final Result RESULT_UNKNOWN_DOB = new Result(null, Error.ERROR_DOB_UNKNOWN);

	public DobAndesizer(Config config, ICrypto cyptoHandler) throws AndesException, IOException {
		super(config, cyptoHandler);
		this.piiDataValidator = new DobValidator(config);
	}

	@Override
	public Result anonymize(final String dateofbirth) throws AndesException {

		String[] validDobParts = this.piiDataValidator.getValidParts(dateofbirth);
		if (validDobParts != null) {
			return new Result(validDobParts[0], Error.NONE);
		} else {
			return RESULT_UNKNOWN_DOB;
		}
	}

	@Override
	public Result deAnonymize(String data) throws AndesException {
		return new Result(data, Error.NONE);
	}

}
