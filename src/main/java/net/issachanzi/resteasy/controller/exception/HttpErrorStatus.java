package net.issachanzi.resteasy.controller.exception;

/**
 * An error in an HTTP request, as represented by a 4xx or 5xx status code
 */
public abstract class HttpErrorStatus extends Exception {
    /**
     * The status code to return to the client
     *
     * <p>
     *     See
     *     <a href="https://en.wikipedia.org/wiki/List_of_HTTP_status_codes">
     *          List of HTTP status codes - Wikipedia
     *     </a>
     * </p>
     */
    protected int statusCode;

    /**
     * Constructs a {@code HttpErrorStatus} with no message
     *
     * @param statusCode The status code to return to the client
     */
    public HttpErrorStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Constructs a {@code HttpErrorStatus} with a custom message
     *
     * @param statusCode The status code to return to the client
     * @param message The custom message to use
     */
    public HttpErrorStatus(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Constructs a {@code HttpErrorStatus} with a custom message and cause
     *
     * @param statusCode The status code to return to the client
     * @param message The custom message to use
     * @param cause The exception that caused this request to fail
     */
    public HttpErrorStatus(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;

        cause.printStackTrace();
    }

    /**
     * Constructs a {@code HttpErrorStatus} with a given cause
     *
     * @param statusCode The status code to return to the client
     * @param cause The exception that caused this request to fail
     */
    public HttpErrorStatus(int statusCode, Throwable cause) {
        super(cause);
        this.statusCode = statusCode;

        cause.printStackTrace();
    }

    /**
     * Constructs a {@code HttpErrorStatus} with a custom message and cause
     *
     * @param statusCode The status code to return to the client
     * @param message The custom message to use
     * @param cause The exception that caused this request to fail
     * @param enableSuppression Whether suppression is enabled
     * @param writableStackTrace Whether the stack trace should be writable
     */
    public HttpErrorStatus(
            int statusCode,
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.statusCode = statusCode;

        cause.printStackTrace();
    }

    public int statusCode() {
        return this.statusCode;
    }
}
