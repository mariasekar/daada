package os.daada.core.pii;

import com.typesafe.config.Config;

import os.daada.core.AbstractAndes;
import os.daada.core.AndesException;
import os.daada.core.Error;
import os.daada.core.Result;
import os.daada.core.crypt.ICrypto;

public class NameAndesizer extends AbstractAndes {

	private static final Result RESULT_UNKNOWN_NAME = new Result(null, Error.ERROR_UNKNOWN_NAME);

	public NameAndesizer(final Config config, final ICrypto cyptoHandler) throws AndesException {
		super(config, cyptoHandler);
		this.piiDataValidator = new NameValidator();
	}

	public Result anonymize(final String name) throws AndesException {

		String[] validNameParts = this.piiDataValidator.getValidParts(name);
		if (validNameParts != null)
			return new Result(validNameParts[0], Error.NONE);
		else
			return RESULT_UNKNOWN_NAME;
	}

	public Result deAnonymize(final String anonymizedName) throws AndesException {
		if (anonymizedName != null && !anonymizedName.trim().isEmpty())
			return new Result(anonymizedName, Error.NONE);
		else
			return new Result(anonymizedName, Error.ERROR_UNKNOWN_NAME);
	}

}