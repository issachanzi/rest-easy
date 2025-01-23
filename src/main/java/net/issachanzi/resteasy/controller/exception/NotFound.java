package net.issachanzi.resteasy.controller.exception;

public class NotFound extends HttpErrorStatus {
    private static final int STATUS_CODE = 404;

    public NotFound () {
        super (STATUS_CODE, "Not found");
    }

    public NotFound(String message) {
        super(STATUS_CODE, message);
    }

    public NotFound(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }

    public NotFound(Throwable cause) {
        super(STATUS_CODE, cause);
    }

    public NotFound(
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
