package os.daada.core.pii;

import java.io.IOException;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.ShortNumberInfo;
import com.typesafe.config.Config;

import os.daada.core.IPiiDataValidator;

/**
 * <p>
 * The <code>MsisdnPrefixConfig</code> class represents a configuration of Mobile Country Codes and
 * Mobile Network Codes.
 * </p>
 * @author sekar
 *
 */
public final class MsisdnValidator implements IPiiDataValidator {

	private static String CONFIG_ANDES_DEFAULT_CC = "andes.msisdn.client.cc";

	private final PhoneNumberUtil PHONE_UTIL = PhoneNumberUtil.getInstance();
    private final ShortNumberInfo shortInfo = ShortNumberInfo.getInstance();
    
    private final String defaultCountryCode;

    /**
     * <p>Initialize the configuration.</p>
     * @param configurationFile
     *      The configuration file containing the default country code
     * @throws IOException
     *      if the configuration file was not found or could not be read
     */
    public MsisdnValidator(final Config config) throws IOException {
    	this.defaultCountryCode = config.getString(CONFIG_ANDES_DEFAULT_CC);
    }

    public String getDefaultCountryCode() {
		return defaultCountryCode;
	}

	@Override
	public String[] getValidParts(String msisdn) {

		String[] msisdnParts = null;
    	String ccc = null;
    	String ndc = null;
    	String sn = null;
        try {
        	final PhoneNumber phoneNumber = PHONE_UTIL.parse(msisdn, this.defaultCountryCode);

        	// Getting the CCC (Country calling code) 
        	ccc = "" + phoneNumber.getCountryCode();

        	// Getting national number "NDC + SN"
        	String nationalSignificantNumber = PHONE_UTIL.getNationalSignificantNumber(phoneNumber);

        	// Validating the telephone / mobile and short codes
        	final int msisdnLength = msisdn.length();
			if(msisdnLength > 6) {
        		int nationalDestinationCodeLength = PHONE_UTIL.getLengthOfNationalDestinationCode(phoneNumber);
        		if (nationalDestinationCodeLength > 0) {

        			// Getting the NDC / MNC
        			ndc = nationalSignificantNumber.substring(0, nationalDestinationCodeLength);

        			// Extracting the SN (Subscriber Number) 
        			sn = nationalSignificantNumber.substring(nationalDestinationCodeLength);

        		}
        	} else if(msisdnLength > 4) {
        		if(this.shortInfo.isPossibleShortNumberForRegion(phoneNumber, this.defaultCountryCode)) {
        			sn = nationalSignificantNumber;
        		} else {	// Dont anonymize the very short codes.
        			sn = msisdn;
        		}
        	} else { // Dont anonymize the very short codes.
        		sn = msisdn;
        	}

		} catch (NumberParseException e) {
			msisdnParts = null;
		}
		return sn != null ? msisdnParts = new String[]{ccc, ndc, sn} : msisdnParts;
	}

}
