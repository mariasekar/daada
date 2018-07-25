package os.daada.core.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.math.NumberUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;

import os.daada.core.IAndes;
import os.daada.core.IAndes.AndesMode;
import os.daada.core.IAndes.PiiFieldType;

/**
 * <p>
 * The <code>Cli</code> class handles the parsing of the command-line for the
 * anonymizer and extraction of the options and arguments.
 * </p>
 * 
 * @author sekar
 *
 */
public class Cli {

	/** The options supported by the CLI */
	private final Options options = new Options();

	/** Global configuration of ANDES */
	private final Config andesConfig;

	/** The command line arguments obtained after parsing */
	private CommandLine commandLine;

	private final List<FieldSpec> fieldSpecList = new ArrayList<>();
	private final String publicKeyFile;
	private final String privateKeyFile;

	/**
	 * Constructs a new CLI with the given arguments
	 */
	public Cli(final Config andesConfig) {
		this.andesConfig = andesConfig;
		this.publicKeyFile = this.andesConfig.getString(IAndes.ANDES_SECURITY_KEYS_PUBLIC);
		this.privateKeyFile = this.andesConfig.getString(IAndes.ANDES_SECURITY_KEYS_PRIVATE);
		this.initializeOptions();
	}

	/**
	 * Parses the command-line arguments
	 * 
	 * @param arguments
	 *            the command-line arguments for the CLI
	 * @throws CliException
	 *             if any the given command-line arguments are not legal
	 */
	public void parse(final String[] arguments) throws CliException {
		try {
			final CommandLineParser parser = new DefaultParser();
			final CommandLine commandLine = parser.parse(options, arguments);

			if (commandLine.hasOption(CliOptions.HELP.getShortName())) {
				printUsage();
			} else {
				checkOptions(commandLine);
				this.commandLine = commandLine;
				this.setPiiFieldSpecs();
			}
		} catch (ParseException e) {
			throw new CliException("Invalid command line: " + e.getMessage(), e);
		}
	}

	/**
	 * Prints the CLI usage
	 */
	public void printUsage() {
		final PrintWriter writer = new PrintWriter(System.out);
		final HelpFormatter UsageFormatter = new HelpFormatter();
		final int maxColumns = 100;

		final String commandLine = "java -jar andes-core.jar";
		final String header = "\nAnonymization Driver\n\n";
		final String footer = "\nPlease report any errors.\n";
		UsageFormatter.printHelp(maxColumns, commandLine, header, options, footer, true);
		writer.flush();
	}

	/**
	 * Returns the operation to be performed by the CLI
	 * 
	 * @return <code>Operation</code>
	 */
	public AndesMode getOperation() {
		if (commandLine.hasOption(CliOptions.ANONYMIZE.getShortName())) {
			return AndesMode.ANONYMIZE;
		} else if (commandLine.hasOption(CliOptions.DEANONYMIZE.getShortName())) {
			return AndesMode.DEANONYMIZE;
		} else {
			return AndesMode.NONE;
		}
	}

	/**
	 * Returns the list of input files to be processed
	 * 
	 * @return List of <code>Path</code>s
	 * @throws CliException
	 *             if the input directory or input file does not exist
	 */
	public List<Path> getInputFiles() throws CliException {
		try {
			final List<Path> inputFileList = new ArrayList<>();
			if (this.commandLine.hasOption(CliOptions.INPUT_FILE.getShortName())) {
				Path inputFilePath = Paths.get(this.commandLine.getOptionValue(CliOptions.INPUT_FILE.getShortName()));
				inputFileList.add(inputFilePath);
			} else if (this.andesConfig.hasPath(IAndes.ANDES_INPUT_FILES_DIR)) {
				final String inputDir = this.andesConfig.getString(IAndes.ANDES_INPUT_FILES_DIR);
				Files.list(Paths.get(inputDir)).forEach(inputFileList::add);
			}
			return inputFileList;
		} catch (IOException e) {
			throw new CliException("Error", e);
		}
	}

	/**
	 * Get the input file details
	 * @return
	 */
	public String getInputFile() {
		String inputFile = null;
		if (commandLine.hasOption(CliOptions.INPUT_FILE.getShortName())) {
			inputFile = commandLine.getOptionValue(CliOptions.INPUT_FILE.getShortName());
		}
		return inputFile;
	}

	/**
	 * Get the output file details
	 * @return
	 */
	public String getOutptputFile() {

		String outputFile = null;
		if (commandLine.hasOption(CliOptions.OUTPUT_FILE.getShortName())) {
			outputFile = commandLine.getOptionValue(CliOptions.OUTPUT_FILE.getShortName());
		}
		return outputFile;
	}

	/**
	 * Get the error file details
	 * @return
	 * @throws CliException
	 */
	public String getErrorFile() {

		String errorFile = null;
		if (commandLine.hasOption(CliOptions.ERROR_FILE.getShortName())) {
			errorFile = commandLine.getOptionValue(CliOptions.ERROR_FILE.getShortName());
		}
		return errorFile;

	}

	/**
	 * Returns the public key file
	 */
	public String getPublicKeyFile() {
		return this.publicKeyFile;
	}

	/**
	 * Returns the private key file
	 */
	public String getPrivateKeyFile() {
		return this.privateKeyFile;
	}

	/**
	 * Returns the list of field specs
	 */
	public List<FieldSpec> getPiiAttributeDetails() {
		return this.fieldSpecList;
	}

	private void setPiiFieldSpecs() {
		if (this.commandLine.hasOption(CliOptions.FIELD_SPEC.getShortName())) {
			final String[] fieldSpecs = this.commandLine.getOptionValues(CliOptions.FIELD_SPEC.getShortName());
			for (String fieldSpec : fieldSpecs) {
				final String[] parts = fieldSpec.split(":");
				final Integer fieldPosition = Integer.parseInt(parts[0]);
				final PiiFieldType fieldType = PiiFieldType.valueOf(parts[1].toLowerCase());
				this.fieldSpecList.add(new FieldSpec(fieldType, fieldPosition));
			}
		} else if (this.commandLine.hasOption(CliOptions.TABLE_PROFILE.getShortName())) {
			String tableProfilePath = IAndes.ANDES_SOURCE_TABLES + "."
					+ this.commandLine.getOptionValue(CliOptions.TABLE_PROFILE.getShortName());
			if (this.andesConfig.hasPath(tableProfilePath)) {
				AndesTableProfile andesTableProfile = ConfigBeanFactory
						.create(this.andesConfig.getConfig(tableProfilePath), AndesTableProfile.class);
				this.fieldSpecList.addAll(andesTableProfile.getPiiFieldDetails());
			}
		}
	}

	private void initializeOptions() {

		final Option anonymizeProcOption = Option.builder(CliOptions.ANONYMIZE.getShortName())
				.longOpt(CliOptions.ANONYMIZE.getLongName()).desc(CliOptions.ANONYMIZE.getDescription()).required(false)
				.hasArg(false).build();

		final Option deanonymizeProcOption = Option.builder(CliOptions.DEANONYMIZE.getShortName())
				.longOpt(CliOptions.DEANONYMIZE.getLongName()).desc(CliOptions.DEANONYMIZE.getDescription())
				.required(false).hasArg(false).build();

		final OptionGroup processingOptionGroup = new OptionGroup();
		processingOptionGroup.setRequired(true);
		processingOptionGroup.addOption(anonymizeProcOption);
		processingOptionGroup.addOption(deanonymizeProcOption);
		options.addOptionGroup(processingOptionGroup);

		final Option fieldSpecOption = Option.builder(CliOptions.FIELD_SPEC.getShortName())
				.longOpt(CliOptions.FIELD_SPEC.getLongName()).desc(CliOptions.FIELD_SPEC.getDescription())
				.required(false).hasArgs().numberOfArgs(Option.UNLIMITED_VALUES).build();
		options.addOption(fieldSpecOption);

		final Option tableProfileOption = Option.builder(CliOptions.TABLE_PROFILE.getShortName())
				.longOpt(CliOptions.TABLE_PROFILE.getLongName()).desc(CliOptions.TABLE_PROFILE.getDescription())
				.required(false).hasArg(true).build();
		options.addOption(tableProfileOption);

		final Option inputFileOption = Option.builder(CliOptions.INPUT_FILE.getShortName())
				.longOpt(CliOptions.INPUT_FILE.getLongName()).desc(CliOptions.INPUT_FILE.getDescription())
				.required(false).hasArg(true).build();
		options.addOption(inputFileOption);

		final Option outputFileOption = Option.builder(CliOptions.OUTPUT_FILE.getShortName())
				.longOpt(CliOptions.OUTPUT_FILE.getLongName()).desc(CliOptions.OUTPUT_FILE.getDescription())
				.required(false).hasArg(true).build();
		options.addOption(outputFileOption);

		final Option errorFileOption = Option.builder(CliOptions.ERROR_FILE.getShortName())
				.longOpt(CliOptions.ERROR_FILE.getLongName()).desc(CliOptions.ERROR_FILE.getDescription())
				.required(false).hasArg(true).build();
		options.addOption(errorFileOption);

		final Option helpOption = Option.builder(CliOptions.HELP.getShortName()).longOpt(CliOptions.HELP.getLongName())
				.desc(CliOptions.HELP.getDescription()).required(false).hasArg(false).build();
		options.addOption(helpOption);
	}

	private void checkOptions(final CommandLine cmdLine) throws CliException {

		// User should provide anonymization or deanonymization option in
		// command line.
		if (!(cmdLine.hasOption(CliOptions.ANONYMIZE.getShortName())
				|| cmdLine.hasOption(CliOptions.DEANONYMIZE.getShortName()))) {
			throw new CliException("Please specify either anonymize / de-anonymize action to be performed.");
		}

		// Anonymization should be performed on the given table data.
		if (cmdLine.hasOption(CliOptions.ANONYMIZE.getShortName())
				&& !(cmdLine.hasOption(CliOptions.TABLE_PROFILE.getShortName())
						|| cmdLine.hasOption(CliOptions.FIELD_SPEC.getShortName()))) {
			throw new CliException("Please specify the table profile on which anonymization should be performed.");
		}

		// De-anonymization should be performed on the given table data.
		if (cmdLine.hasOption(CliOptions.DEANONYMIZE.getShortName())
				&& !(cmdLine.hasOption(CliOptions.TABLE_PROFILE.getShortName())
						|| cmdLine.hasOption(CliOptions.FIELD_SPEC.getShortName()))) {
			throw new CliException("Please specify the table profile on which anonymization should be performed.");
		}

		// Validating the field spec details availability and its types
		if (cmdLine.hasOption(CliOptions.FIELD_SPEC.getShortName())) {
			this.checkFieldSpecValues(cmdLine.getOptionValues(CliOptions.FIELD_SPEC.getShortName()));
		} else if (cmdLine.hasOption(CliOptions.TABLE_PROFILE.getShortName())) {
			String tableProfilePath = IAndes.ANDES_SOURCE_TABLES + "."
					+ cmdLine.getOptionValue(CliOptions.TABLE_PROFILE.getShortName());
			try {
				if ((andesConfig.hasPath(tableProfilePath))) {
					ConfigBeanFactory.create(andesConfig.getConfig(tableProfilePath), AndesTableProfile.class);
				} else {
					throw new CliException("Please check the table profile which anonymization should be performed.");
				}
			} catch (Exception e) {
				throw new CliException(
						"Please check the table profile & field specification details on which anonymization should be performed.",
						e);
			}
		} else if(cmdLine.hasOption(CliOptions.INPUT_FILE.getShortName())
				&& (cmdLine.hasOption(CliOptions.OUTPUT_FILE.getShortName()))
				&& (cmdLine.hasOption(CliOptions.ERROR_FILE.getShortName()))) {
			
		} else {
			throw new CliException("Please specify the table profile on which anonymization should be performed.");
		}
		this.checkPrivatePublicKeyFiles(cmdLine);
	}

	private void checkPrivatePublicKeyFiles(final CommandLine commandLine) throws CliException {

		File pubKeyFile = new File(this.getPublicKeyFile());
		File priKeyFile = new File(this.getPrivateKeyFile());
		if (commandLine.hasOption(CliOptions.ANONYMIZE.getShortName()) && !(pubKeyFile.exists())) {
			throw new CliException("Please provide the public key file path properly.");
		}
		if (commandLine.hasOption(CliOptions.DEANONYMIZE.getShortName()) && !(priKeyFile.exists())) {
			throw new CliException("Please provide the private key file path properly.");
		}
	}

	private void checkFieldSpecValues(final String[] fieldSpecArgs) throws CliException {
		if (fieldSpecArgs != null && fieldSpecArgs.length > 0) {
			for (String s : fieldSpecArgs) {
				final String[] parts = s.trim().split(":");
				if (parts.length != 2) {
					throw new CliException("Field spec format should be <field num>:<field type>: " + s);
				}

				final String fieldNumber = parts[0];
				if (!NumberUtils.isDigits(fieldNumber)) {
					throw new CliException("Field number is not a number: " + s);
				}

				final String fieldValue = parts[1];
				boolean isValid = false;
				for (PiiFieldType type : PiiFieldType.values()) {
					if (type.toString().equalsIgnoreCase(fieldValue)) {
						isValid = true;
						break;
					}
				}
				if (!isValid) {
					throw new CliException("Invalid field value: " + s);
				}
			}
		} else {
			throw new CliException("Missing field spec details.");
		}
	}

}
