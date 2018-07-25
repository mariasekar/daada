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
import os.daada.core.pii.GoogleMsisdnAndesizer;

/**
 * <p>
 * The <code>Anonymizer</code> class is a wrapper/facade over the classes
 * that perform anonymization/de-anonymization of individual fields.
 * </p>
 * @author sekar
 *
 */
public class DeAnonymizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeAnonymizer.class);

    /** The helper to anonymize and de-anonymize MSISDNs */
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
    public DeAnonymizer(final Config anonymizerConfig)
            throws AndesException {
        try {
        	this.cryptoHandler = AndesCryptoFactory.getAndesCryptoInstance().getCrypter(anonymizerConfig);
            ANONYMIZERS_DEANONYMIZERS.put(PiiFieldType.msisdn, new GoogleMsisdnAndesizer(anonymizerConfig, this.cryptoHandler));
        } catch (Exception e) {
        	LOGGER.error("Anonymizer initialization exception.", e);
            throw new AndesException(e);
        }
    }

    /**
     * The anonymization driver
     * @param args TODO
     */
    public static void main(final String[] args) {
        //final Cli cli = new Cli();
        try {

        	final Config anonymizerConfig = ConfigFactory.load();

            //cli.parse(args);
            final DeAnonymizer anonymizer = new DeAnonymizer(anonymizerConfig);

            final String[] encMsisdns = {"2348092ab3e7d71a0155593cd0878e8bd0a7081ce16bffc3bd7222a8e647dc152c3131235fa8043877ed81da7cb7db442b73dc1faa6e4dc50b165f60ff42693daa3fb69346fe016705586401a6aa90935ff2c5a6b12c15bad5bfebf5c9cfdccfcaa3917adcdbc5408c792f50d848819e0319fca4bea9d826128970f992c7db90be5fe6",
                                         "2348091a10ae9cc4f751027eb99b863c0bbe0e591af310f44e50b5283ba8c2b4e438c90af11efe360d9f12bd8e5355076ee7cce9e9558aed25ceddf71e447c7f55ed089fd49e7e4ae2dac00fbd02c31dc65abdd512ef178a9c7395062cd1fc35784b02f7af5d943a1f0b96ec0c985ab4eeda626f2e5bf778d581b5deb6f299789f559c",
                                         "2348099fd9d441925c9e6099a5325d0c35db85b1fdf52682a458a6421b46f293c79c495344e72348bb8501e89b2565020fcf38ab5472da867451c1e60e76ddcc0720f3a00aff9f450de7f2ebbb2c02f4b3d4148febfd0d7799d4c93159b8c13dca5d8b1340541c79a84fe46c13e35aee3767ee9a26acc7c9606e0edb10e1657e071a78",
                                         "23480923a977cb776ef5d9ce0d624214828d4103527e110ce9f012ab354f7ea31a3d7f15ff0e734e550f466befcce4f3e5732c801f59201a63d33465f65c64c886b3417edf1435979eade92e0c8ef8bdcaea491ad6a14bcc2d25cbcc250fb7794d59aaa6bdd099e0910841e43aed00fc80c4fdaa58312eaa729ba6e488a63fd87e6fb3",
                                         "234809423d52fb5deb73dec7d9f72e40b61727d5ad586d446877e2999cc29e90edb5fa54b3fe6d874db33bf52dbbabcf05fd61fd5769964ec5f1e566965811e0496112ebf534101488adb404e80cbc8c9188f4ae26f52ce1b758364b947ce9a44388e5f91e4e5854fd96e8be81bb8869b6fae8c490e72146ad879b1eca113e560b3cd5",
                                         "23480952698f13af3e1c993bf339f3c5291fcfe1dddc0513dc37e6a31076fc0f0e72fa32d2f0e9695f788fd234f35dfde1cc4d9e7bbd9f001f5b695f05df8305674d0ee8a68676e831a6f2e8ce9ed927df2a1a42efa6ee48bff5b74ff64068f792ebddfed4811652d25916c80fb49d0f67d6c42fe5359df4b498cb3f270bd71ae3d00d"
                                         };
            final long totalMsisdns = encMsisdns.length;

            final LocalTime startTime = LocalTime.now();
            System.out.println("Test begin time: " + startTime.toString());

            for (int i = 0; i < totalMsisdns; i++) {
                final Result result = anonymizer.deAnonymize(PiiFieldType.msisdn, encMsisdns[i]);
                final String anonymizedMsisdn = result.getValue();
                System.out.println("Completed until: " + i + "(" + anonymizedMsisdn + ")");
            }

            final LocalTime endTime = LocalTime.now();
            System.out.println("Test end time: " + endTime.toString());
            final long elapsedTimeInSecs = Duration.between(startTime, endTime).getSeconds();
            System.out.println("Total time taken (secs): " + elapsedTimeInSecs);
            System.out.println("Total MSISDNs deanonymized: " + totalMsisdns / 1000000 + " M");

        }/* catch (CliException e) {
            cli.printUsage();
        }*/
        catch (Exception e) {
            e.printStackTrace();
        }
    }

	public Result deAnonymize(PiiFieldType alias, String data) throws AndesException {
		return ANONYMIZERS_DEANONYMIZERS.get(alias).deAnonymize(data);
	}

}
