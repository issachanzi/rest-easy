package net.issachanzi.resteasy.controller.exception;

/**
 * The 400 Bad Request HTTP error status
 */
public class BadRequest extends HttpErrorStatus {
    private static final int STATUS_CODE = 400;

    public BadRequest() {
        super(STATUS_CODE, "Bad request");
    }

    public BadRequest(String message) {
        super(STATUS_CODE, message);
    }

    public BadRequest(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }

    public BadRequest(Throwable cause) {
        super(STATUS_CODE, cause);
    }

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
