package org.adroit.andes.core.cli;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;

import org.adroit.andes.core.Anonymizer;
import org.adroit.andes.core.DeAnonymizer;
import org.adroit.andes.core.Error;
import org.adroit.andes.core.IAndes;
import org.adroit.andes.core.Result;
import org.adroit.andes.core.IAndes.AndesMode;
import org.adroit.andes.core.IAndes.PiiFieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

/**
 * Main class for invoking ANDES functionality through the command line
 *
 * @author sekar
 *
 */
public class Driver {

	private static final Logger LOGGER = LoggerFactory.getLogger(Driver.class);

	private static String CONFIG_ANDES_DEFAULT_ERROR_VALIDATOR = "andes.client.errorValidator";
	public static void main(final String[] args) {
		
		Cli cli = null;

		try {

			// Initialize configurations
			Config andesConfig = ConfigFactory.load();
			cli = new Cli(andesConfig);
			cli.parse(args);

			// Initialize the anonymizer
			LOGGER.info("Initializing the anonymizer");

			// Perform the operation
			final AndesMode operation = cli.getOperation();
			if (AndesMode.ANONYMIZE == operation) {

				final Anonymizer anonymizer = new Anonymizer(andesConfig);

				final String inputFile = cli.getInputFile();
				final String outputDir = cli.getOutptputFile();
				final String errorDir = cli.getErrorFile();
				final Driver driver = new Driver();
				List<FieldSpec> piiFields = cli.getPiiAttributeDetails();

				driver.processFile(piiFields, inputFile, outputDir, errorDir, anonymizer, andesConfig);

			} else if (AndesMode.DEANONYMIZE == operation) {

				final DeAnonymizer anonymizer = new DeAnonymizer(andesConfig);

				final String inputFile = cli.getInputFile();
				final String outputDir = cli.getOutptputFile();
				final String errorDir = cli.getErrorFile();
				final Driver driver = new Driver();
				List<FieldSpec> piiFields = cli.getPiiAttributeDetails();

				driver.processFile(piiFields, inputFile, outputDir, errorDir, anonymizer, andesConfig);

			} else {
				LOGGER.error("Invalid command line: No operation specified.");
				System.exit(1);
			}
		} catch (CliException e) {
			LOGGER.error("Invalid command line");
			cli.printUsage();
			System.exit(1);
		} catch (Exception e) {
			LOGGER.error("Error", e);
			System.exit(1);
		}
	}

	/**
	 * Anonymize the files within the folder.
	 * 
	 * @param piiFields
	 * @param inputFileList
	 * @param outputDirName
	 * @param errorDirName
	 * @param anonymizer
	 * @throws Exception
	 */
	/*
	 * public void processFiles(final List<FieldSpec> piiFields, final
	 * List<Path> inputFileList, final String outputDirName, final String
	 * errorDirName, final Anonymizer anonymizer, final Config config) throws
	 * Exception {
	 * 
	 * long timeStart = System.currentTimeMillis(); for (Path inputFile :
	 * inputFileList) { final String inputFileName = inputFile.toString(); //
	 * String inputFileNameAlone = inputFile.getFileName().toString(); final
	 * String outputFileName = outputDirName; final String errorFileName =
	 * errorDirName;
	 * 
	 * processFile(piiFields, inputFileName, outputFileName, errorFileName,
	 * anonymizer, config); } System.out.println("Running cost time " +
	 * (System.currentTimeMillis() - timeStart) + "ms"); }
	 */

	/*
	 * public void processFiles(final List<FieldSpec> piiFields, final
	 * List<Path> inputFileList, final String outputDirName, final String
	 * errorDirName, final DeAnonymizer deanonymizer, final Config config)
	 * throws Exception {
	 * 
	 * long timeStart = System.currentTimeMillis(); for (Path inputFile :
	 * inputFileList) { final String inputFileName = inputFile.toString(); //
	 * String inputFileNameAlone = inputFile.getFileName().toString(); final
	 * String outputFileName = outputDirName; final String errorFileName =
	 * errorDirName;
	 * 
	 * processFile(piiFields, inputFileName, outputFileName, errorFileName,
	 * deanonymizer, andesConfig); } System.out.println("Running cost time " +
	 * (System.currentTimeMillis() - timeStart) + "ms"); }
	 */

	public void processFile(final List<FieldSpec> piiFields, final String inputFile, final String outputFile,
			final String errorFile, final Anonymizer anonymizer, final Config config) throws Exception {
		long timeStart = System.currentTimeMillis();

		LOGGER.info("Processing input file {} to output file {}", inputFile, outputFile);

		CsvParser parser = null;
		CsvWriter outPrinter = null;
		CsvWriter errPrinter = null;
		boolean isErroredRecord = false;
		final boolean errorVal = config.getBoolean(CONFIG_ANDES_DEFAULT_ERROR_VALIDATOR);
		try {

			char[] delimitors = new char[] {IAndes.FILE_DEFAULT_DELIMITER};
			String dataDelimitor = config.getString(IAndes.ANDES_FILE_DELIMITOR);
			if(dataDelimitor != null && dataDelimitor.trim().length() == 0) {
				dataDelimitor = new String(delimitors);
			}
			// The settings object provides many configuration options
			CsvParserSettings parserSettings = new CsvParserSettings();

			// You can configure the parser to automatically detect what line
			// separator sequence is in the input
			parserSettings.setLineSeparatorDetectionEnabled(true);

			// Let's consider the first parsed row as the headers of each column
			// in the file.
			parserSettings.setHeaderExtractionEnabled(false);

			parserSettings.getFormat().setDelimiter(IAndes.FILE_DEFAULT_DELIMITER);
			parser = new CsvParser(parserSettings);

			CsvWriterSettings csvsettings = new CsvWriterSettings();
			csvsettings.getFormat().setDelimiter(IAndes.FILE_DEFAULT_DELIMITER);

			outPrinter = new CsvWriter(new FileWriter(outputFile), csvsettings);

			CsvWriterSettings errorCsvsettings = new CsvWriterSettings();
			// errorCsvsettings.setHeaderWritingEnabled(true);
			errPrinter = new CsvWriter(new FileWriter(errorFile), errorCsvsettings);

			parser.beginParsing(new InputStreamReader(new FileInputStream(inputFile), IAndes.CHARACTER_TYPE_UTF_8));
			// outPrinter.writeHeaders(parser.getRecordMetadata().headers());

			Record record = null;
			int recordNum = 0;
			while ((record = parser.parseNextRecord()) != null) {
				recordNum++;
				String[] values = record.getValues();
				for (FieldSpec fieldSpec : piiFields) {
					PiiFieldType piiFieldType = fieldSpec.getPiiField();
					for (int fieldIndex : fieldSpec.getColumnIndices()) {
						final Result anonymizedResult = anonymizer.anonymize(piiFieldType, values[fieldIndex]);
						final Error error = anonymizedResult.getError();
						if (error == Error.NONE) {
							values[fieldIndex] = anonymizedResult.getValue();
						} else {

							isErroredRecord = true;
							errPrinter.commentRow("Error in record #" + recordNum + " : " + error);
							errPrinter.writeRow(values); // signal that the
															// entire record is
															// in error
						}
					}
				}
				if (isErroredRecord) {
					if (errorVal == false) {
						outPrinter.writeRow(values);

					}
				} else {
					outPrinter.writeRow(values);
				}
				isErroredRecord = false;
			}
		} finally {
			if (outPrinter != null) {
				outPrinter.close();
			}
			if (errPrinter != null) {
				errPrinter.close();
			}
		}
		System.out.println("Running cost time " + (System.currentTimeMillis() - timeStart) + "ms");
	}

	public void processFile(final List<FieldSpec> piiFields, final String inputFile, final String outputFile,
			final String errorFile, final DeAnonymizer anonymizer, final Config config) throws Exception {

		long timeStart = System.currentTimeMillis();

		LOGGER.info("Processing input file {} to output file {}", inputFile, outputFile);

		CsvParser parser = null;
		CsvWriter outPrinter = null;
		CsvWriter errPrinter = null;
		final boolean errorVal = config.getBoolean(CONFIG_ANDES_DEFAULT_ERROR_VALIDATOR);

		try {

			// The settings object provides many configuration options
			CsvParserSettings parserSettings = new CsvParserSettings();

			// You can configure the parser to automatically detect what line
			// separator sequence is in the input
			parserSettings.setLineSeparatorDetectionEnabled(true);

			// Let's consider the first parsed row as the headers of each column
			// in the file.
			parserSettings.setHeaderExtractionEnabled(false);

			parserSettings.getFormat().setDelimiter(IAndes.FILE_DEFAULT_DELIMITER);
			parser = new CsvParser(parserSettings);

			CsvWriterSettings csvsettings = new CsvWriterSettings();
			csvsettings.getFormat().setDelimiter(IAndes.FILE_DEFAULT_DELIMITER);
			// csvsettings.setHeaderWritingEnabled(true);

			outPrinter = new CsvWriter(new FileWriter(outputFile), csvsettings);

			CsvWriterSettings errorCsvsettings = new CsvWriterSettings();
			// errorCsvsettings.setHeaderWritingEnabled(true);
			errPrinter = new CsvWriter(new FileWriter(errorFile), errorCsvsettings);

			parser.beginParsing(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
			// outPrinter.writeHeaders(parser.getRecordMetadata().headers());

			Record record = null;
			int recordNum = 0;
			while ((record = parser.parseNextRecord()) != null) {
				recordNum++;
				String[] values = record.getValues();

				for (FieldSpec fieldSpec : piiFields) {
					PiiFieldType piiFieldType = fieldSpec.getPiiField();
					for (int fieldIndex : fieldSpec.getColumnIndices()) {
						final Result deanonymizedResult = anonymizer.deAnonymize(piiFieldType, values[fieldIndex]);
						final Error error = deanonymizedResult.getError();
						if (error == Error.NONE) {
							values[fieldIndex] = deanonymizedResult.getValue();
						} else {
							if (errorVal == false) {
								errPrinter.commentRow("Error in record #" + recordNum + " : " + error);
								errPrinter.writeRow(values); // signal that the
								values[fieldIndex] = deanonymizedResult.getValue();
							}
							// // signal that the entire record is // in error
							else {
								errPrinter.commentRow("Error in record #" + recordNum + " : " + error);
								errPrinter.writeRow(values);
							}

						}
					}
				}
				outPrinter.writeRow(values);
			}
		} finally {
			if (outPrinter != null) {
				outPrinter.close();
			}
			if (errPrinter != null) {
				errPrinter.close();
			}
		}
		System.out.println("Running cost time " + (System.currentTimeMillis() - timeStart) + "ms");
	}
}