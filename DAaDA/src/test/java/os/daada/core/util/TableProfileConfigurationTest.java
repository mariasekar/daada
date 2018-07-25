package os.daada.core.util;

import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;

import os.daada.core.IAndes;
import os.daada.core.cli.AndesTableProfile;
import os.daada.core.cli.CliException;

public class TableProfileConfigurationTest {

	private static Config andesConfig;

	@BeforeClass
    public static void setUp() throws Exception {
		// Initialize configurations
        andesConfig = ConfigFactory.load();
    }

    @Test
    public void givenValidConfig_testConfigurationParsing() throws Exception {
    	String tableProfilePath = IAndes.ANDES_SOURCE_TABLES + ".ODS.RATED_DATA_CDR";
		ConfigBeanFactory.create(andesConfig.getConfig(tableProfilePath), AndesTableProfile.class);
    }

    @Test(expected = CliException.class)
    public void givenInValidConfig_testConfigurationParsing() throws Exception {
    	String tableProfilePath = IAndes.ANDES_SOURCE_TABLES + ".rated_data";
    	try {
			if(andesConfig.hasPath(tableProfilePath)) {
    			ConfigBeanFactory.create(andesConfig.getConfig(tableProfilePath), AndesTableProfile.class);
    		} else {
    			throw new CliException(
                        "Please check the table profile which anonymization should be performed.");
    		}
		} catch(Exception e) {
			throw new CliException(
                    "Please check the table profile & field specification details on which anonymization should be performed.", e);
		}
    }

}
