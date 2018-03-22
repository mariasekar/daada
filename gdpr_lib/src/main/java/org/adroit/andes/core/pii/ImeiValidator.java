package org.adroit.andes.core.pii;

import org.adroit.andes.core.IPiiDataValidator;
import org.apache.commons.validator.routines.checkdigit.CheckDigit;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;

public class ImeiValidator implements IPiiDataValidator {

	final CheckDigit imeicheck = new LuhnCheckDigit();

	@Override
	public String[] getValidParts(String imei) {
		String[] validImeiParts = null;
		if (imei != null) {
			String tempImei = imei.trim(); 
			int imeiLength = tempImei.length();
			if (imeiLength > 14 && imeicheck.isValid(tempImei)) {
				validImeiParts = new String[] {tempImei.substring(0, 8), DELIMITER_UNDERSCORE, tempImei.substring(8)};
			} else if (imeiLength == 14) {
				validImeiParts = new String[] {tempImei.substring(0, 8), DELIMITER_UNDERSCORE, tempImei.substring(8)};
			}
		}
		return validImeiParts;
	}
}
