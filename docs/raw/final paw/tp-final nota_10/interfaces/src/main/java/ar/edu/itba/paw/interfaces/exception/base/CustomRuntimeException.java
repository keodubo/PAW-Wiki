package ar.edu.itba.paw.interfaces.exception.base;

public class CustomRuntimeException extends RuntimeException implements CustomBaseException {

    private final int statusCode;
    private final String message;

    public CustomRuntimeException(final int statusCode, final String message) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
