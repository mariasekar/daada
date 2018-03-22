package org.adroit.andes.core;

@SuppressWarnings("serial")
public class AndesException extends Exception {

    public AndesException(final String message, Throwable cause) {
        super(message, cause);
    }

    public AndesException(final Throwable cause) {
        super(cause);
    }

    public AndesException(final String message) {
        super(message);
    }

}
