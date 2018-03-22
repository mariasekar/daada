package com.adroit.andes.core.util;

import org.adroit.andes.core.IAndes;
import org.adroit.andes.core.cli.AndesTableProfile;
import org.adroit.andes.core.cli.CliException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;

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
