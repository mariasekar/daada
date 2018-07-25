package os.daada.core.pii;

import com.typesafe.config.Config;

import os.daada.core.AbstractAndes;
import os.daada.core.AndesException;
import os.daada.core.Error;
import os.daada.core.IPiiDataValidator;
import os.daada.core.Result;
import os.daada.core.crypt.ICrypto;

public class EmailAndesizer extends AbstractAndes {

	private static final Result RESULT_UNKNOWN_EMAIL = new Result(null, Error.ERROR_UNKNOWN_EMAIL);

	public EmailAndesizer(final Config config, final ICrypto cyptoHandler) throws AndesException {
		super(config, cyptoHandler);
		this.piiDataValidator = new EmailValidator();
	}

	public Result anonymize(final String email) throws AndesException {
		String[] validEmailParts = this.piiDataValidator.getValidParts(email);
		if (validEmailParts != null) {
			final StringBuilder anonymizedEmail = new StringBuilder(this.cyptoHandler.encrypt(validEmailParts[0]));
			anonymizedEmail.append(IPiiDataValidator.DELIMITOR_ATSIGN);
			anonymizedEmail.append(validEmailParts[1]);
			validEmailParts = null;
			return new Result(anonymizedEmail.toString(), Error.NONE);
		}
		return RESULT_UNKNOWN_EMAIL;
	}

	public Result deAnonymize(final String anonymizedEmail) throws AndesException {
		if (anonymizedEmail != null && !anonymizedEmail.isEmpty() && anonymizedEmail.contains(IPiiDataValidator.DELIMITOR_ATSIGN)) {
			final StringBuilder deAnonymizedEmail = new StringBuilder(
					this.cyptoHandler.decrypt(anonymizedEmail.substring(0, anonymizedEmail.indexOf(IPiiDataValidator.DELIMITOR_ATSIGN))));
			deAnonymizedEmail.append(anonymizedEmail.substring(anonymizedEmail.indexOf(IPiiDataValidator.DELIMITOR_ATSIGN)));
			return new Result(deAnonymizedEmail.toString(), Error.NONE);
		}
		return RESULT_UNKNOWN_EMAIL;
	}

}
