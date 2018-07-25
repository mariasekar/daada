package os.daada.core;

public interface IPiiDataValidator {

	static final String DELIMITER_UNDERSCORE = "-";
	static final String DELIMITOR_ATSIGN = "@";
	static final String DELIMITOR_DOT = ".";

	String[] getValidParts(String piiData);
	default boolean isValidPiiData(String piiData) {
		String[] validPiiParts = this.getValidParts(piiData);
		return validPiiParts != null && validPiiParts.length > 0;
	}
}
