package os.daada.core.pii;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.typesafe.config.Config;

import os.daada.core.IPiiDataValidator;

public class IpValidator implements IPiiDataValidator {

	private static String CONFIG_ANDES_DEFAULT_IP = "andes.ip.client.replace";
    private final String maskingChar;

	private final String IPADDRESS_PATTERN = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";
	private final Pattern pattern;
    
    /**
     * <p>Initialize the configuration.</p>
     * @param configurationFile
     *      The configuration file containing the ip address masking character
     * @throws IOException
     *      if the configuration file was not found or could not be read
     * @throws NumberFormatException
     *      if the configuration file contains non-numeric entries for MCCs and MNCs
     */
    public IpValidator(final Config config) throws IOException, NumberFormatException {
    	this.pattern = Pattern.compile(IPADDRESS_PATTERN);
    	this.maskingChar = config.getString(CONFIG_ANDES_DEFAULT_IP);
    }

    public String getMaskingChar() {
		return this.maskingChar;
	}

	@Override
	public String[] getValidParts(String ipaddr) {
		String[] validIpParts = null;
		if (ipaddr != null) {
			Matcher matcher = pattern.matcher(ipaddr);
			if (matcher.matches() == true) {
				validIpParts = new String[] {ipaddr.substring(0, ipaddr.lastIndexOf(DELIMITOR_DOT))};
			}
		}
		return validIpParts;
	}

}