package net.issachanzi.resteasy.controller.exception;

public class Conflict extends HttpErrorStatus {
    private static final int STATUS_CODE = 409;
    private static final String DEFAULT_MESSAGE = "Conflict";

    public Conflict() {
        this(DEFAULT_MESSAGE);
    }

    public Conflict(String message) {
        super(STATUS_CODE, message);
    }

    public Conflict(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }

    public Conflict(Throwable cause) {
        this(DEFAULT_MESSAGE, cause);
    }

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
