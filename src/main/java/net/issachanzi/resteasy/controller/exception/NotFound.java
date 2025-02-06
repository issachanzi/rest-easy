package net.issachanzi.resteasy.controller.exception;

/**
 * The 404 Not Found HTTP error status
 */
public class NotFound extends HttpErrorStatus {
    private static final int STATUS_CODE = 404;
    public static final String DEFAULT_MESSAGE = "Not found";


    /**
     * Constructs a {@code NotFound} with default message
     */
    public NotFound () {
        super (STATUS_CODE, DEFAULT_MESSAGE);
    }

    /**
     * Constructs a {@code NotFound} with a custom message
     *
     * @param message The custom message to use
     */
    public NotFound(String message) {
        super(STATUS_CODE, message);
    }

    /**
     * Constructs a {@code NotFound} with a custom message and cause
     *
     * @param message The custom message to use
     * @param cause The exception that caused this request to fail
     */
    public NotFound(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }

    /**
     * Constructs a {@code NotFound} with a given cause
     *
     * @param cause The exception that caused this request to fail
     */
    public NotFound(Throwable cause) {
        super(STATUS_CODE, cause);
    }

    /**
     * Constructs a {@code Forbidden} with a custom message and cause
     *
     * @param message The custom message to use
     * @param cause The exception that caused this request to fail
     * @param enableSuppression Whether suppression is enabled
     * @param writableStackTrace Whether the stack trace should be writable
     */
    public NotFound(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace
    ) {
        super(
                STATUS_CODE,
                message,
                cause,
                enableSuppression,
                writableStackTrace
        );
    }
}
