package os.daada.core.pii;

import java.io.IOException;

import com.typesafe.config.Config;

import os.daada.core.AbstractAndes;
import os.daada.core.AndesException;
import os.daada.core.Error;
import os.daada.core.Result;
import os.daada.core.crypt.ICrypto;

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
