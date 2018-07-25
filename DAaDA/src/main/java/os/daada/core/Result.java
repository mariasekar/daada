package os.daada.core;

/**
 * <p>
 * The <code>Result</code> class contains the result of an anonymization operation.
 * The purpose of this class is to indicate any logical errors in the anonymization operation.
 * </p>
 * @author sekar
 *
 */
public class Result {

    /** The output of the operation */
    private String value;

    /** The error that occurred during the operation */
    private Error error;

    public Result(final String value, final Error error) {
        this.value = value;
        this.error = error;
    }

    /**
     * <p>
     * Returns the value of the result. The value can be <code>null</code> if the result is in error.
     * <br/><br/>
     * The {@link Result#getError()} method <em>must</em> be called to check for an error condition
     * before calling this method to get the value of the result.
     * </p>
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the error condition that may have occurred when computing the result.
     * A successful operation is indicated by the presence of {@link Error.NONE} as the return value of this method.
     */
    public Error getError() {
        return this.error;
    }

}
