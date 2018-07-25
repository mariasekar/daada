package os.daada.core.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import os.daada.core.AndesException;
import os.daada.core.Anonymizer;
import os.daada.core.DeAnonymizer;
import os.daada.core.Error;
import os.daada.core.IAndes;
import os.daada.core.Result;
import os.daada.core.IAndes.AndesMode;
import os.daada.core.IAndes.PiiFieldType;

/**
 * Main class for invoking ANDES functionality through the command line
 *
 * @author sekar
 *
 */
public class ParallelDriver {

	private static String CONFIG_ANDES_DEFAULT_ERROR_VALIDATOR = "andes.client.errorValidator";
	private static final Logger LOGGER = LoggerFactory.getLogger(ParallelDriver.class);

	private final static int totalProcessors = Runtime.getRuntime().availableProcessors();
	private final static int totalThreads = totalProcessors;
	private static CountDownLatch latch = new CountDownLatch(totalThreads);
	private final static ConcurrentLinkedQueue<Queue<String[]>> sharedDataQueue = new ConcurrentLinkedQueue<>();
	private final static ConcurrentLinkedQueue<Queue<String[]>> sharedErrorQueue = new ConcurrentLinkedQueue<>();
	private static final int batchSize = 200;

	public static void main(final String[] args) {

		Cli cli = null;
		try {
			// Initialize configurations
			Config andesConfig = ConfigFactory.load();

			char[] delimitors = new char[] {IAndes.FILE_DEFAULT_DELIMITER};
			String dataDelimitor = andesConfig.getString(IAndes.ANDES_FILE_DELIMITOR);
			if(dataDelimitor != null && dataDelimitor.trim().length() == 0) {
				dataDelimitor = new String(delimitors);
			}
			cli = new Cli(andesConfig);
			cli.parse(args);

			// Initialize the anonymizer
			LOGGER.info("Initializing the anonymizer");

			// Perform the operation
			final AndesMode operation = cli.getOperation();

			final String inputFile = cli.getInputFile();
			final String outputDir = cli.getOutptputFile();
			final String errorDir = cli.getErrorFile();
			final ParallelDriver driver = new ParallelDriver();
			List<FieldSpec> piiFields = cli.getPiiAttributeDetails();

			driver.processFile(piiFields, inputFile, outputDir, errorDir, andesConfig, operation, dataDelimitor);
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
	 * @param errorDirNameRp
	 * @param anonymizer
	 * @throws Exception
	 */
	/*
	 * public void processFiles(final List<FieldSpec> piiFields, final
	 * List<Path> inputFileList, final String outputDirName, final String
	 * errorDirName, final Config config, AndesMode operation) throws Exception,
	 * CliException, IOException {
	 * 
	 * for (Path inputFile : inputFileList) { final String inputFileName =
	 * inputFile.toString(); String inputFileNameAlone =
	 * inputFile.getFileName().toString(); String outputFileName = null; String
	 * errorFileName = null; if (operation == AndesMode.ANONYMIZE) {
	 * 
	 * outputFileName = outputDirName; errorFileName = errorDirName; } else if
	 * (operation == AndesMode.DEANONYMIZE) { outputFileName = outputDirName;
	 * errorFileName = errorDirName; } else if (operation == AndesMode.NONE) {
	 * return; } processFile(piiFields, inputFileName, outputFileName,
	 * errorFileName, config, operation); } }
	 */

	public void processFile(final List<FieldSpec> piiFields, final String inputFile, final String outputFile,
			final String errorFile, final Config config, AndesMode operation, String fileDelimitor) throws Exception {

		LOGGER.info("Processing input file {} to output file {}", inputFile, outputFile);

		long timeStart = System.currentTimeMillis();
		try {
			final DataProvider dataProvider = new DataProvider(inputFile, fileDelimitor);
			for (int i = 0; i < totalThreads; i++) {
				if (operation == AndesMode.ANONYMIZE) {
					new Thread(new AnonymizationProcessor(config, dataProvider, piiFields)).start();
				} else if (operation == AndesMode.DEANONYMIZE) {
					new Thread(new DeAnonymizationProcessor(config, dataProvider, piiFields)).start();
				}
			}
			Thread dataConsumer = new Thread(new DataConsumer(outputFile, sharedDataQueue, fileDelimitor));
			dataConsumer.start();
			Thread errorDataConsumer = new Thread(new DataConsumer(errorFile, sharedErrorQueue, fileDelimitor));
			errorDataConsumer.start();
			latch.await();
		}
		finally {

		}
		LOGGER.info("Running cost time {}", (System.currentTimeMillis() - timeStart) + "ms");
	}

	private static final class DataProvider {

		private BufferedReader bufferedReader;
		private final String fileDelimitor;

		public DataProvider(String fileName, String fileDelimitor) {
			try {
				this.bufferedReader = new BufferedReader(new FileReader(fileName), 1024 * 1024 * 10);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			}
			this.fileDelimitor = fileDelimitor;
		}

		public synchronized Queue<String[]> getNextBatch() {
			String record = null;
			int recordCount = 0;
			final Queue<String[]> batchedRows = new ConcurrentLinkedQueue<>();
			try {
				while((record = this.bufferedReader.readLine()) != null) {
					++recordCount;
					String[] values = record.split(this.fileDelimitor);
					batchedRows.add(values);
					if (recordCount % batchSize == 0) {
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return batchedRows;
		}
	}

	private static final class AnonymizationProcessor implements Runnable {

		private final Anonymizer anonymizer;
		private final List<FieldSpec> piiFields;
		private final DataProvider dataProvider;
		private final boolean errorVal;
		private Queue<String[]> processedData = new ConcurrentLinkedQueue<>();
		private final AtomicInteger recordCount = new AtomicInteger(0);

		public AnonymizationProcessor(final Config config, final DataProvider dataProvider,
				final List<FieldSpec> piiFields) throws AndesException, IOException, NumberFormatException {
			this.anonymizer = new Anonymizer(config);
			this.dataProvider = dataProvider;
			this.piiFields = piiFields;
			this.errorVal = config.getBoolean(CONFIG_ANDES_DEFAULT_ERROR_VALIDATOR);
		}

		public void run() {
			while (true) {
				final Queue<String[]> dataRecords = this.dataProvider.getNextBatch();
				if (!dataRecords.isEmpty()) {
					dataRecords.parallelStream().forEach(this::processRecord);
					// Flushing remaining processing data into queue
					if (!processedData.isEmpty()) {
						sharedDataQueue.offer(processedData);
					}
				} else {
					LOGGER.info("Processed all the records. Hence exiting.");
					break;
				}
			}
			latch.countDown();
		}

		private void processRecord(String[] record) {
			recordCount.incrementAndGet();
			boolean isErroredRecord = false;
			Queue<String[]> erroredData = new LinkedList<>();
			for (FieldSpec fieldSpec : this.piiFields) {
				PiiFieldType piiFieldType = fieldSpec.getPiiField();
				for (int fieldIndex : fieldSpec.getColumnIndices()) {
					Result anonymizedResult;
					try {
						anonymizedResult = this.anonymizer.anonymize(piiFieldType, record[fieldIndex]);
						final Error error = anonymizedResult.getError();
						if (error == Error.NONE) {
							record[fieldIndex] = anonymizedResult.getValue();
						} else {
							isErroredRecord = true;
							erroredData.add(new String[] { error.name(), "" + fieldIndex });
							//erroredData.add(record);
						}
					} catch (AndesException e) {
						LOGGER.error("Error while anonymizing the msisdn.", e);
						System.exit(1);
					}
				}
			}
			if (!isErroredRecord) {
				processedData.add(record);
			} else {
				erroredData.add(record);
				if (!this.errorVal) {
					sharedErrorQueue.offer(erroredData);
					processedData.add(record);
				} else {
					sharedErrorQueue.offer(erroredData);
				}
			}
			isErroredRecord = false;
			record = null;
			if (recordCount.get() % 10 == 0) {
				sharedDataQueue.offer(processedData);
				synchronized (processedData) {
					processedData = new ConcurrentLinkedQueue<>();
				}
			}
		}
	}

	private static final class DeAnonymizationProcessor implements Runnable {

		private final DeAnonymizer deAnonymizer;
		private final List<FieldSpec> piiFields;
		private final DataProvider dataProvider;
		private final boolean errorVal;
		private Queue<String[]> processedData = new ConcurrentLinkedQueue<>();
		private final AtomicInteger recordCount = new AtomicInteger(0);

		public DeAnonymizationProcessor(final Config config, final DataProvider dataProvider,
				final List<FieldSpec> piiFields) throws AndesException {
			this.deAnonymizer = new DeAnonymizer(config);
			this.dataProvider = dataProvider;
			this.piiFields = piiFields;
			this.errorVal = config.getBoolean(CONFIG_ANDES_DEFAULT_ERROR_VALIDATOR);
		}

		public void run() {
			while (true) {
				final Queue<String[]> dataRecords = this.dataProvider.getNextBatch();
				if (!dataRecords.isEmpty()) {
					dataRecords.parallelStream().forEach(this::processRecord);
					// Flushing remaining processing data into queue
					if (!processedData.isEmpty()) {
						sharedDataQueue.offer(processedData);
					}
				} else {
					LOGGER.info("Processed all the records. Hence exiting.");
					break;
				}
			}
			latch.countDown();
		}

		private void processRecord(String[] record) {
			recordCount.incrementAndGet();
			boolean isErroredRecord = false;
			Queue<String[]> erroredData = new LinkedList<>();
			for (FieldSpec fieldSpec : this.piiFields) {
				PiiFieldType piiFieldType = fieldSpec.getPiiField();
				for (int fieldIndex : fieldSpec.getColumnIndices()) {
					Result anonymizedResult;
					try {
						anonymizedResult = this.deAnonymizer.deAnonymize(piiFieldType, record[fieldIndex]);
						final Error error = anonymizedResult.getError();
						if (error == Error.NONE) {
							record[fieldIndex] = anonymizedResult.getValue();
						} else {
							isErroredRecord = true;
							erroredData.add(new String[] { error.name(), "" + fieldIndex });
							//erroredData.add(record);
						}
					} catch (AndesException e) {
						LOGGER.error("Error while anonymizing the msisdn.", e);
						System.exit(1);
					}
				}
			}
			if (!isErroredRecord) {
				processedData.add(record);
			} else {
				erroredData.add(record);
				if (!this.errorVal) {
					sharedErrorQueue.offer(erroredData);
					processedData.add(record);
				} else {
					sharedErrorQueue.offer(erroredData);
				}
			}
			isErroredRecord = false;
			record = null;
			if (recordCount.get() % 10 == 0) {
				sharedDataQueue.offer(processedData);
				synchronized (processedData) {
					processedData = new ConcurrentLinkedQueue<>();
				}
			}
		}
	}

	private static final class DataConsumer implements Runnable {

		private final BufferedWriter writer;
		private final ConcurrentLinkedQueue<Queue<String[]>> dataQueue;
		private final String fileDelimitor;

		public DataConsumer(String fileName, ConcurrentLinkedQueue<Queue<String[]>> dataQueue, String fileDelimitor) throws IOException {
			this.writer = new BufferedWriter(new FileWriter(fileName), 1024 * 1024 * 10);
			this.dataQueue = dataQueue;
			this.fileDelimitor = fileDelimitor;
		}

		public void run() {
			while (true) {
				this.saveRecords();
				if (latch.getCount() == 0) {
					this.saveRecords();
					break;
				}
			}
			try {
				this.writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			LOGGER.info("Writer got all records to write.");
		}

		private final void saveRecords() {
			Queue<String[]> records = null;
			while ((records = this.dataQueue.poll()) != null) {
				String[] record = null;
				while ((record = records.poll()) != null) {
					boolean delFlag = false;
					for(String data : record) {
						try {
							if(delFlag) {
								this.writer.write(this.fileDelimitor);
							}
							this.writer.write(data);
							delFlag = true;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					try {
						this.writer.write('\n');
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}

