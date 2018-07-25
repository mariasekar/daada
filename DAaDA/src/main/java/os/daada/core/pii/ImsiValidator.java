package os.daada.core.pii;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.typesafe.config.Config;

import os.daada.core.AndesException;
import os.daada.core.IPiiDataValidator;

public class ImsiValidator implements IPiiDataValidator {

	private static String CONFIG_ANDES_IMSI_SOURCE_FILE = "andes.imsi.mcccodes";
    private final String imsiSourceFile;
    private static final Map<String, Set<String>> IMSI_LIST = new ConcurrentHashMap<String, Set<String>>();
    private static boolean isImsiLoaded = false;

    /**
     * <p>Initialize the configuration.</p>
     * @param configurationFile
     *      The configuration file containing the imsi per countrywise 
     */
    public ImsiValidator(final Config config) throws AndesException {
    	this.imsiSourceFile = config.getString(CONFIG_ANDES_IMSI_SOURCE_FILE);
    	if(!isImsiLoaded) {
    		try {
				this.loadImsis();
			} catch (IOException e) {
				throw new AndesException("Error while loading the IMSI.");
			}
    	}
    }

    public final String[] getValidParts(String imsi) {
		String[] imsiParts = null;
		if(imsi != null && imsi.length() == 15) {
			String mcc = imsi.substring(0, 3);
			Set<String> mncs = IMSI_LIST.get(mcc);
			if(mncs != null && !mncs.isEmpty()) {
				final String twoDigitMNC = imsi.substring(3, 5);
				final String threeDigitMNC = imsi.substring(3, 6);
				if (mncs.contains(twoDigitMNC)) {
					imsiParts = new String[]{mcc, twoDigitMNC, imsi.substring(5)};
				} else if (mncs.contains(threeDigitMNC)) {
					imsiParts = new String[]{mcc, threeDigitMNC, imsi.substring(6)};
				}
			}
		}
		return imsiParts;
	}
    
    public String getImsiSourceFile() {
		return this.imsiSourceFile;
	}

    private final void loadImsis() throws IOException {

    	BufferedReader in = new BufferedReader(new FileReader(this.imsiSourceFile));
		String line;
		while ((line = in.readLine()) != null) {
 			if(line.trim().length() > 0) {
 				String[] parts = line.split(",");

 				if (IMSI_LIST.get(parts[0]) == null) {

 					IMSI_LIST.put((parts[0]), new HashSet<String>());
 				}
 				IMSI_LIST.get((parts[0])).add((parts[1]));
 			}
		}
		in.close();
		isImsiLoaded = true;
    }

}