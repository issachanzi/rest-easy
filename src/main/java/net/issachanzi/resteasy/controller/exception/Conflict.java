package net.issachanzi.resteasy.controller.exception;

/**
 * The 409 Conflict HTTP error status
 */
public class Conflict extends HttpErrorStatus {
    private static final int STATUS_CODE = 409;
    private static final String DEFAULT_MESSAGE = "Conflict";

    /**
     * Constructs a {@code Conflict} with default message
     */
    public Conflict() {
        this(DEFAULT_MESSAGE);
    }

    /**
     * Constructs a {@code Conflict} with a custom message
     *
     * @param message The custom message to use
     */
    public Conflict(String message) {
        super(STATUS_CODE, message);
    }

    /**
     * Constructs a {@code Conflict} with a custom message and cause
     *
     * @param message The custom message to use
     * @param cause The exception that caused this request to fail
     */
    public Conflict(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }

    /**
     * Constructs a {@code Conflict} with a given cause
     *
     * @param cause The exception that caused this request to fail
     */
    public Conflict(Throwable cause) {
        this(DEFAULT_MESSAGE, cause);
    }

    /**
     * Constructs a {@code Conflict} with a custom message and cause
     *
     * @param message The custom message to use
     * @param cause The exception that caused this request to fail
     * @param enableSuppression Whether suppression is enabled
     * @param writableStackTrace Whether the stack trace should be writable
     */
    public Conflict(
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
