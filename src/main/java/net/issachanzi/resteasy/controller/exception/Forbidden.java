package net.issachanzi.resteasy.controller.exception;

/**
 * The 403 Forbidden HTTP error status
 */
public class Forbidden extends HttpErrorStatus {
    private static final int STATUS_CODE = 403;
    public static final String DEFAULT_MESSAGE = "Forbidden";

    public Forbidden() {
        super(STATUS_CODE, DEFAULT_MESSAGE);
    }

    public Forbidden(String message) {
        super(STATUS_CODE, message);
    }

    public Forbidden(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }

    public Forbidden(Throwable cause) {
        super(STATUS_CODE, cause);
    }

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
