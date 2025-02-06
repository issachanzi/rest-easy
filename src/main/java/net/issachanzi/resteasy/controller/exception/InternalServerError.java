package net.issachanzi.resteasy.controller.exception;

/**
 * The 500 Internal Server Error HTTP error status
 */
public class InternalServerError extends HttpErrorStatus {
    private static final int STATUS_CODE = 500;
    private static final String DEFAULT_MESSAGE = "Internal server error";

    /**
     * Constructs an {@code InternalServerError} with default message
     */
    public InternalServerError() {
        super(STATUS_CODE, DEFAULT_MESSAGE);
    }

    /**
     * Constructs an {@code InternalServerError} with a custom message
     *
     * @param message The custom message to use
     */
    public InternalServerError(String message) {
        super(STATUS_CODE, message);
    }

    /**
     * Constructs an {@code InternalServerError} with a custom message and cause
     *
     * @param message The custom message to use
     * @param cause The exception that caused this request to fail
     */
    public InternalServerError(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }

    /**
     * Constructs an {@code InternalServerError} with a given cause
     *
     * @param cause The exception that caused this request to fail
     */
    public InternalServerError(Throwable cause) {
        super(STATUS_CODE, cause);
    }

    /**
     * Constructs an {@code InternalServerError} with a custom message and cause
     *
     * @param message The custom message to use
     * @param cause The exception that caused this request to fail
     * @param enableSuppression Whether suppression is enabled
     * @param writableStackTrace Whether the stack trace should be writable
     */
    public InternalServerError(
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
