package os.daada.core.pii;

import os.daada.core.IPiiDataValidator;

public class EmailValidator implements IPiiDataValidator {

    final org.apache.commons.validator.routines.EmailValidator emailValidator = org.apache.commons.validator.routines.EmailValidator.getInstance();

	@Override
	public String[] getValidParts(String email) {
		String[] validEmailParts = null;
		if (email != null && !email.isEmpty() && emailValidator.isValid(email)) {
			validEmailParts = email.split(DELIMITOR_ATSIGN);
		}
		return validEmailParts;
	}
}
