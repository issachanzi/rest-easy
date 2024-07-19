package net.issachanzi.resteasy.controller.exception;

/**
 * The 500 Internal Server Error HTTP error status
 */
public class InternalServerError extends HttpErrorStatus {
    private static final int STATUS_CODE = 500;

    public InternalServerError() {
        super(STATUS_CODE);
    }

    public InternalServerError(String message) {
        super(STATUS_CODE, message);
    }

    public InternalServerError(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }

    public InternalServerError(Throwable cause) {
        super(STATUS_CODE, cause);
    }

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
