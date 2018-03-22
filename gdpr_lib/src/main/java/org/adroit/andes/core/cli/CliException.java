package org.adroit.andes.core.cli;

@SuppressWarnings("serial")
public class CliException extends Exception {

    public CliException(final String message) {
        super(message);
    }

    public CliException(final String message, Throwable cause) {
        super(message, cause);
    }

}
