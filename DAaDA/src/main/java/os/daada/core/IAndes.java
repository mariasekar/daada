package os.daada.core;

public interface IAndes {

	public static final String CHARACTER_TYPE_UTF_8 = "UTF-8";
	public static final char FILE_DEFAULT_DELIMITER = (char)1;

	// Cryptography algorithm configuration
	public static final String ANDES_SECURITY_CRYPTO_ALGORITHM = "anonymization.security.algorithmtype";
	
	// Public and private key locations.
	public static final String ANDES_SECURITY_KEYS_PUBLIC = "anonymization.security.keys.public";
	public static final String ANDES_SECURITY_KEYS_PRIVATE = "anonymization.security.keys.private";

	// Source table configurations
	public static final String ANDES_SOURCE_TABLES = "anonymization.sourcetables";
	public static final String ANDES_PII_FIELDS = "piiFieldDetails";

	// Input files, output files and error files locations. 
	public static final String ANDES_INPUT_FILES_DIR = "anonymization.files.input";
	public static final String ANDES_OUTPUT_FILES_DIR = "anonymization.files.output";
	public static final String ANDES_ERROR_FILES_DIR = "anonymization.files.error";
	
	public static String ANDES_FILE_DELIMITOR = "andes.file.delimitor";

	Result anonymize(final String data) throws AndesException;
	Result deAnonymize(final String data) throws AndesException;


	public enum AndesMode {

		ANONYMIZE,
		DEANONYMIZE,
		NONE
		;
	}

	public enum PiiFieldType {
		msisdn,email,dob,imei,midname,lastname,imsi,ip
		;
	}

}
