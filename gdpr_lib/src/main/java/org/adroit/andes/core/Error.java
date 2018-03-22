package org.adroit.andes.core;

/**
 * <p>
 * The <code>Error</code> enum contains all possible error conditions that can be encountered during an operation.
 * The basic idea is that these error conditions should not be thrown as exceptions because handling an exception
 * and getting back to the normal flow of control in the caller is very cumbersome.
 * <br/><br/>
 * When any method of <code>Anonymizer</code> is called, the caller should be able to handle the error,
 * typically logging the error and writing the offending record/data to an error file, and continue.
 * </p>
 *
 * @author sekar
 *
 */
public enum Error {
    ERROR_MSISDN_UNKNOWN_MCC,
    ERROR_MSISDN_UNKNOWN_MNC,
    NONE,
    ERROR_DOB_UNKNOWN,
    ERROR_IP_UNKNOWN,
    ERROR_UNKNOWN_NAME,
    ERROR_UNKNOWN_EMAIL,
    ERROR_UNKNOWN_IMEI,
    ERROR_UNKNOWN_IMSI
}
