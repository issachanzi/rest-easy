package net.issachanzi.resteasy.controller.exception;

/**
 * An error in an HTTP request
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

    public HttpErrorStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public HttpErrorStatus(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpErrorStatus(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public HttpErrorStatus(int statusCode, Throwable cause) {
        super(cause);
        this.statusCode = statusCode;
    }

    public HttpErrorStatus(
            int statusCode,
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return this.statusCode;
    }
}
