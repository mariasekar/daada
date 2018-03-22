package org.adroit.andes.core.pii;

import org.adroit.andes.core.IPiiDataValidator;

public class NameValidator implements IPiiDataValidator {

	public final String[] getValidParts(String name) {
		String[] nameValidParts = null;
		if(name != null && !name.trim().isEmpty()) {
			nameValidParts = new String[]{name.substring(0, 1)};
		}
		return nameValidParts;
	}

}