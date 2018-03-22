package org.adroit.andes.core;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.adroit.andes.core.IAndes.PiiFieldType;
import org.adroit.andes.core.crypt.AndesCryptoFactory;
import org.adroit.andes.core.crypt.ICrypto;
import org.adroit.andes.core.pii.DobAndesizer;
import org.adroit.andes.core.pii.EmailAndesizer;
import org.adroit.andes.core.pii.GoogleMsisdnAndesizer;
import org.adroit.andes.core.pii.ImeiAndesizer;
import org.adroit.andes.core.pii.ImsiAndesizer;
import org.adroit.andes.core.pii.IpAndesizer;
import org.adroit.andes.core.pii.NameAndesizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * <p>
 * The <code>Anonymizer</code> class is a wrapper/facade over the classes
 * that perform anonymization/de-anonymization of individual fields.
 * </p>
 * @author sekar
 *
 */
public class Anonymizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Anonymizer.class);

    /** The helper to anonymize MSISDNs */
    private final Map<PiiFieldType, IAndes> ANONYMIZERS_DEANONYMIZERS = new ConcurrentHashMap<>();

    private final ICrypto cryptoHandler;

    /**
     * Initialize the anonymizer
     * @param cipher
     *      the asymmetric cipher to use for the operations
     * @param msisdnPrefixConfig
     *      the configuration of the MSISDN prefixes to anonymize MSISDNs in a prefix-preserving manner
     * @throws AndesException
     *      if an error occurred during the initialization
     */
    public Anonymizer(final Config anonymizerConfig)
            throws AndesException {
        try {
        	this.cryptoHandler = AndesCryptoFactory.getAndesCryptoInstance().getCrypter(anonymizerConfig);
            ANONYMIZERS_DEANONYMIZERS.put(PiiFieldType.msisdn, new GoogleMsisdnAndesizer(anonymizerConfig, this.cryptoHandler));
            ANONYMIZERS_DEANONYMIZERS.put(PiiFieldType.dob, new DobAndesizer(anonymizerConfig, this.cryptoHandler));
            ANONYMIZERS_DEANONYMIZERS.put(PiiFieldType.ip, new IpAndesizer(anonymizerConfig, this.cryptoHandler));
            ANONYMIZERS_DEANONYMIZERS.put(PiiFieldType.midname, new NameAndesizer(anonymizerConfig, this.cryptoHandler));
            ANONYMIZERS_DEANONYMIZERS.put(PiiFieldType.lastname, new NameAndesizer(anonymizerConfig, this.cryptoHandler));
            ANONYMIZERS_DEANONYMIZERS.put(PiiFieldType.email, new EmailAndesizer(anonymizerConfig, this.cryptoHandler));
            ANONYMIZERS_DEANONYMIZERS.put(PiiFieldType.imei, new ImeiAndesizer(anonymizerConfig, this.cryptoHandler));
            ANONYMIZERS_DEANONYMIZERS.put(PiiFieldType.imsi, new ImsiAndesizer(anonymizerConfig, this.cryptoHandler));

        } catch (Exception e) {
        	LOGGER.error("Anonymizer initialization exception.", e);
            throw new AndesException(e);
        }
    }

	public Result anonymize(PiiFieldType piiFieldType, String data) throws AndesException {
		return ANONYMIZERS_DEANONYMIZERS.get(piiFieldType).anonymize(data);
	}

}
