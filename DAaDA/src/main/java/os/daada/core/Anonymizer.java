package os.daada.core;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import os.daada.core.IAndes.PiiFieldType;
import os.daada.core.crypt.AndesCryptoFactory;
import os.daada.core.crypt.ICrypto;
import os.daada.core.pii.DobAndesizer;
import os.daada.core.pii.EmailAndesizer;
import os.daada.core.pii.GoogleMsisdnAndesizer;
import os.daada.core.pii.ImeiAndesizer;
import os.daada.core.pii.ImsiAndesizer;
import os.daada.core.pii.IpAndesizer;
import os.daada.core.pii.NameAndesizer;

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
