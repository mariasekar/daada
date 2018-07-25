package os.daada.core.cli;

/**
 * This class defines all the options supported by the CLI
 * @author sekar
 *
 */
public enum CliOptions {

    ANONYMIZE("a",
            "anonymize",
            "Anonymize the given dataset."),
    DEANONYMIZE("d",
            "deanonymize",
            "De-Anonymize the given dataset."),
    FIELD_SPEC("s",
            "field-spec",
            "Specification of the fields to be processed in the dataset. "
                    + "Each field spec is of the form \"<field num>:<field type\". "
                    + "E.g. \"1:msisdn\", \"2:imei\"."),
    HELP("h",
            "help",
            "Show help."),

    INPUT_FILE("f",
            "file",
            "input file to be processed."),
      
    TABLE_PROFILE("t",
            "tableprofile",
            "Table profile for the specific table."),
    OUTPUT_FILE("o",
    		"output_file",
    		"location of output_file."),  
    ERROR_FILE("e",
    		"error_file",
    		"location of error_file."), 

    ;

    private final String shortName;
    private final String longName;
    private final String description;

    private CliOptions(final String shortName, final String longName, final String description) {
        this.shortName = shortName;
        this.longName = longName;
        this.description = description;
    }

    public String getShortName() {
        return this.shortName;
    }

    public String getLongName() {
        return this.longName;
    }

    public String getDescription() {
        return this.description;
    }

}
