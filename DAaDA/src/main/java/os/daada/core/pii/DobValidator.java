package os.daada.core.pii;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.typesafe.config.Config;

import os.daada.core.IPiiDataValidator;

public class DobValidator implements IPiiDataValidator {

	private static String CONFIG_ANDES_DEFAULT_DOB = "andes.dob.client.format";
    private final SimpleDateFormat dateformat;

    /**
     * <p>Initialize the configuration.</p>
     * @param configurationFile
     *      The configuration file containing the dob formats
     * @throws IOException
     *      if the configuration file was not found or could not be read
     * @throws NumberFormatException
     *      if the configuration file contains non-numeric entries for MCCs and MNCs
     */
    public DobValidator(final Config config) throws IOException, NumberFormatException {
    	this.dateformat = new SimpleDateFormat(config.getString(CONFIG_ANDES_DEFAULT_DOB));
    }

	@Override
	public String[] getValidParts(String dateofbirth) {
		String[] validDobParts = null;
		if (dateofbirth != null) {
			String tempDob = dateofbirth.trim();
			if ((tempDob.length() > 4)) {
				try {
					dateformat.parse(tempDob);
				} catch (ParseException e) {
					return validDobParts;
				}
				validDobParts = new String[]{tempDob.substring(0, 4)};
			}
		}
		return validDobParts;
	}
}
