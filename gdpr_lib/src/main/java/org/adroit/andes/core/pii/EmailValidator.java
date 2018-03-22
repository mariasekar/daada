package org.adroit.andes.core.pii;

import org.adroit.andes.core.IPiiDataValidator;

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
