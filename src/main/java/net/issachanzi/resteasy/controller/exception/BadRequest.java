package net.issachanzi.resteasy.controller.exception;

/**
 * The 400 Bad Request HTTP error status
 */
public class BadRequest extends HttpErrorStatus {
    private static final int STATUS_CODE = 400;

    /**
     * Constructs a {@code BadRequest} with default message
     */
    public BadRequest() {
        super(STATUS_CODE, "Bad request");
    }

    /**
     * Constructs a {@code BadRequest} with a custom message
     *
     * @param message The custom message to use
     */
    public BadRequest(String message) {
        super(STATUS_CODE, message);
    }

    /**
     * Constructs a {@code BadRequest} with a custom message and cause
     *
     * @param message The custom message to use
     * @param cause The exception that caused this request to fail
     */
    public BadRequest(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }

    /**
     * Constructs a {@code BadRequest} with a given cause
     *
     * @param cause The exception that caused this request to fail
     */
    public BadRequest(Throwable cause) {
        super(STATUS_CODE, cause);
    }

    /**
     * Constructs a {@code BadRequest} with a custom message and cause
     *
     * @param message The custom message to use
     * @param cause The exception that caused this request to fail
     * @param enableSuppression Whether suppression is enabled
     * @param writableStackTrace Whether the stack trace should be writable
     */
    public BadRequest(
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
