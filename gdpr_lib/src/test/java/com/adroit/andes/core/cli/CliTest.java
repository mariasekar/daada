package com.adroit.andes.core.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.adroit.andes.core.IAndes.PiiFieldType;
import org.adroit.andes.core.cli.Cli;
import org.adroit.andes.core.cli.CliException;
import org.adroit.andes.core.cli.FieldSpec;
import org.junit.Before;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class CliTest {

    /** Fixture */
    private Cli cli;
    private Config andesConfig;

    @Before
    public void setUp() {

        // Initialize configurations
        andesConfig = ConfigFactory.load();
        cli = new Cli(andesConfig);
    }

    @Test
    public void givenAllArgumentsInShortForm_test_CliIsCorrect() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-t ODS.RATED_DATA_CDR ").append(" ")
                .append("-f input/rated_data_cdr.csv").append(" ")
                .append("-o output/rated_output_data_cdr_anonymized.csv").append(" ")
                .append("-e error/rated_input_data_cdr.csv").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }

    @Test
    public void givenAllArgumentsInLongForm_test_CliIsCorrect() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("--anonymize").append(" ")
                .append("--tableprofile ODS.RATED_DATA_CDR ").append(" ")
                .append("--file input/rated_data_cdr.csv").append(" ")
                .append("--output_file output/rated_output_data_cdr_anonymized.csv").append(" ")
                .append("--error_file error/rated_input_data_cdr.csv").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }

    @Test
    public void givenMultipleArgumentsForFieldSpecOption_test_CliIsCorrect() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-f input/activation_multiple.csv").append(" ")
                .append("-s 1:msisdn 4:msisdn").append(" ")
                .append("-o output/act_output.csv").append(" ")
                .append("-e error/act_error.csv").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }

    @Test
    public void givenSingleArgumentForFieldSpecOption_test_CliIsCorrect() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-f input/activation.csv").append(" ")
                .append("-s 1:msisdn").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }

    @Test(expected = CliException.class)
    public void givenNoArgumentsForFieldSpecOption_test_CliIsWrong() throws CliException {
    	final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-f input/activation.csv").append(" ")
                .append("-s").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }

    // Processing option group tests: -a (--anonymize), -d (--deanonymize)

    @Test
    public void givenOneOptionOfProcessingOptionGroup_test_CliIsCorrect() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-f input/activation_multiple.csv").append(" ")
                .append("-s 1:msisdn 4:msisdn").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }

    @Test(expected = CliException.class)
    public void givenAllOptionOfProcessingOptionGroup_test_CliIsWrong() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-d").append(" ")
                .append("-f input/activation_multiple.csv").append(" ")
                .append("-s 1:msisdn 4:msisdn").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }

    @Test(expected = CliException.class)
    public void givenNoOptionOfProcessingOptionGroup_test_CliThrowsException() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("-f input/activation_multiple.csv").append(" ")
                .append("-s 1:msisdn 4:msisdn").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }

    // Field specification option tests

    @Test
    public void givenSingleFieldSpec_test_CliReturnsSingleValue() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-f input/activation.csv").append(" ")
                .append("-s 1:msisdn").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));

        final List<FieldSpec> fieldSpecList = cli.getPiiAttributeDetails();
        assertEquals("No. of arguments to field-spec option", 1, fieldSpecList.size());
        assertTrue("Field-spec option value 1:msisdn", fieldSpecList.contains(new FieldSpec(PiiFieldType.msisdn, 1)));
    }

    @Test
    public void givenMultipleFieldSpec_test_CliReturnsMultipleValue() throws CliException {
        final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-f input/activation_multiple.csv").append(" ")
                .append("-s 1:msisdn 4:msisdn").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));

        final List<FieldSpec> fieldSpecs = cli.getPiiAttributeDetails();
        assertEquals("No. of arguments to field-spec option", 2, fieldSpecs.size());
        assertTrue("Field-spec contains 1:MSISDN", fieldSpecs.contains(new FieldSpec(PiiFieldType.msisdn, 1)));
        assertTrue("Field-spec contains 4:MSISDN", fieldSpecs.contains(new FieldSpec(PiiFieldType.msisdn, 4)));
    }

    @Test (expected = CliException.class)
    public void givenInvalidFieldNumberInFieldSpec_test_CliIsWrong() throws Exception {
        final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-f input/activation_multiple.csv").append(" ")
                .append("-s a:msisdn").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }

    @Test (expected = CliException.class)
    public void givenInvalidFieldValueInFieldSpec_test_CliIsWrong() throws Exception {
        final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-f input/activation_multiple.csv").append(" ")
                .append("-s 1:abc").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }

    @Test (expected = CliException.class)
    public void givenImproperFieldSpec_test_CliIsWrong() throws Exception {
        final String commandLine =
                new StringBuilder()
                .append("-a").append(" ")
                .append("-f input/activation_multiple.csv").append(" ")
                .append("-s 1,msisdn").append(" ")
                .toString();
        cli.parse(commandLine.split("\\s"));
    }
}
