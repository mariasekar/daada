package os.daada.core.pii;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import com.typesafe.config.Config;

import os.daada.core.AbstractAndes;
import os.daada.core.AndesException;
import os.daada.core.Error;
import os.daada.core.IPiiDataValidator;
import os.daada.core.Result;
import os.daada.core.crypt.ICrypto;

/**
* <p>
     * Anonymizes and deanonymizes an MSISDN in a prefix-preserving manner.<br>
     * </p>
     * An MSISDN has the following structure:<br>
     * MSISDN = CC + NDC + SN or <br>
     * MSISDN = CC + NPA + SN <br>
     * where <br>
     * CC is the Country Code <br>
     * NDC is the National Destination Code <br>
     * NPA is the Number Planning Area SN is the Subscriber Number.
     * </p>
     * <p>
     * The prefix consisting of CC + (NPA or NDC) is preserved and only the SN
     * is anonymized.
     * </p>
 *
 * @author sekar
 *
 */
public class GoogleMsisdnAndesizer extends AbstractAndes {

    private static final Result MSISDN_RESULT_UNKNOWN_MCC = new Result(null, Error.ERROR_MSISDN_UNKNOWN_MCC);
	private static final Result MSIDN_RESULT_UNKNON_MNC = new Result(null, Error.ERROR_MSISDN_UNKNOWN_MNC);

	private static final ConcurrentMap<String, String> MSIDN_CACHE = createMap("msisdn_cache");

	/**
     * Constructs a MsisdnAnonymizer
     * @throws AndesException 
     */
    public GoogleMsisdnAndesizer(final Config config, final ICrypto cyptoHandler)
            throws NumberFormatException, IOException, AndesException {
    	super(config, cyptoHandler);
        this.piiDataValidator = new MsisdnValidator(config);
    }

    /**
     * <p>
     * Anonymizes an MSISDN in a prefix-preserving manner.<br>
     * </p>
     * An MSISDN has the following structure:<br>
     * MSISDN = CC + NDC + SN or <br>
     * MSISDN = CC + NPA + SN <br>
     * where <br>
     * CC is the Country Code <br>
     * NDC is the National Destination Code <br>
     * NPA is the Number Planning Area SN is the Subscriber Number.
     * </p>
     * <p>
     * The prefix consisting of CC + (NPA or NDC) is preserved and only the SN
     * is anonymized.
     * </p>
     *
     * @param msisdn
     *            The MSISDN to anonymize
     * @return The anonymized MSISDN with the prefix unchanged
     * @throws AndesException
     *             if an error occurred during the operation
     */
    public Result anonymize(final String msisdn) throws AndesException {

    	if(msisdn != null) {
    		String[] msisdnParts = this.piiDataValidator.getValidParts(msisdn);
    		if(msisdnParts != null) {
    			String existingAnonymizedMsisdn = MSIDN_CACHE.get(msisdn);
    			if(existingAnonymizedMsisdn == null) {
    				String ccc = msisdnParts[0];
    				String ndc = msisdnParts[1];
    				String sn = msisdnParts[2];
    				msisdnParts = null;
    				StringBuilder anonymizedMsisdn = new StringBuilder(150);
    				if(ccc != null && ndc != null && sn != null) {
    					anonymizedMsisdn.append(ccc);
    					anonymizedMsisdn.append(IPiiDataValidator.DELIMITER_UNDERSCORE);
    					anonymizedMsisdn.append(ndc);
    					anonymizedMsisdn.append(IPiiDataValidator.DELIMITER_UNDERSCORE);
    					anonymizedMsisdn.append(this.cyptoHandler.encrypt(sn));
    					existingAnonymizedMsisdn = anonymizedMsisdn.toString();
    					MSIDN_CACHE.put(msisdn, existingAnonymizedMsisdn);
    					return new Result(existingAnonymizedMsisdn, Error.NONE);
    				} else if(ccc != null && sn != null) {
    					anonymizedMsisdn.append(ccc);
    					anonymizedMsisdn.append(IPiiDataValidator.DELIMITER_UNDERSCORE);
    					anonymizedMsisdn.append(sn);
    					existingAnonymizedMsisdn = anonymizedMsisdn.toString();
    					MSIDN_CACHE.put(msisdn, existingAnonymizedMsisdn);
    					return new Result(existingAnonymizedMsisdn, Error.NONE);
    				}
    			} else {
    				return new Result(existingAnonymizedMsisdn, Error.NONE);
    			}
    		}
    	}
		return MSISDN_RESULT_UNKNOWN_MCC;
    }

    /**
     * An MSISDN has the following structure:<br>
     * MSISDN = CC + NDC + SN or <br>
     * MSISDN = CC + NPA + SN <br>
     * where <br>
     * CC is the Country Code <br>
     * NDC is the National Destination Code <br>
     * NPA is the Number Planning Area SN is the Subscriber Number.
     * </p>
     * <p>
     * The prefix consisting of CC + (NPA or NDC) is preserved and only the SN
     * is anonymized.
     * </p>
     *
     * @param anonymizedMsisdn
     *            The MSISDN to deanonymize
     * @return The deanonymized MSISDN
     * @throws AndesException
     *             if an error occurred during the operation
     */
	public Result deAnonymize(final String anonymizedMsisdn) throws AndesException {
		if(anonymizedMsisdn == null) {
			return MSISDN_RESULT_UNKNOWN_MCC;
		}
		String existingDeAnonymizedMsisdn = MSIDN_CACHE.get(anonymizedMsisdn);
		if(existingDeAnonymizedMsisdn == null) {
			int snIndex = anonymizedMsisdn.lastIndexOf(IPiiDataValidator.DELIMITER_UNDERSCORE) + 1;
			StringBuilder deAnonymizedMsisdn = new StringBuilder(20);
			deAnonymizedMsisdn.append(anonymizedMsisdn.substring(0, snIndex));
			String sn = anonymizedMsisdn.substring(snIndex);

			// The MCC and MNC of the MSISDN is matched, de-anonymize the SN part
			if (sn != null) {
				if(sn.length() >= 7) {
					deAnonymizedMsisdn.append(this.cyptoHandler.decrypt(sn));
					existingDeAnonymizedMsisdn = deAnonymizedMsisdn.toString();
					MSIDN_CACHE.put(anonymizedMsisdn, existingDeAnonymizedMsisdn);
					return new Result(existingDeAnonymizedMsisdn, Error.NONE);
				} else {
					deAnonymizedMsisdn.append(sn);
					existingDeAnonymizedMsisdn = deAnonymizedMsisdn.toString();
					MSIDN_CACHE.put(anonymizedMsisdn, existingDeAnonymizedMsisdn);
					return new Result(existingDeAnonymizedMsisdn, Error.NONE);
				}
			} else {
				return MSIDN_RESULT_UNKNON_MNC;        	
			}
		} else {
			return new Result(existingDeAnonymizedMsisdn, Error.NONE);
		}
    }

}
