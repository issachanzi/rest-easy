package net.issachanzi.resteasy.controller.exception;

/**
 * The 403 Forbidden HTTP error status
 */
public class Forbidden extends HttpErrorStatus {
    private static final int STATUS_CODE = 403;
    public static final String DEFAULT_MESSAGE = "Forbidden";

    /**
     * Constructs a {@code Forbidden} with default message
     */
    public Forbidden() {
        super(STATUS_CODE, DEFAULT_MESSAGE);
    }

    /**
     * Constructs a {@code Forbidden} with a custom message
     *
     * @param message The custom message to use
     */
    public Forbidden(String message) {
        super(STATUS_CODE, message);
    }

    /**
     * Constructs a {@code Forbidden} with a custom message and cause
     *
     * @param message The custom message to use
     * @param cause The exception that caused this request to fail
     */
    public Forbidden(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }

    /**
     * Constructs a {@code Forbidden} with a given cause
     *
     * @param cause The exception that caused this request to fail
     */
    public Forbidden(Throwable cause) {
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
    public Forbidden(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace
    ) {
        super(
                STATUS_CODE,
                message, cause,
                enableSuppression,
                writableStackTrace
        );
    }
}
