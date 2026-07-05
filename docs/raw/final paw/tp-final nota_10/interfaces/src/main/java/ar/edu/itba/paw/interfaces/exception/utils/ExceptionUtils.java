package ar.edu.itba.paw.interfaces.exception.utils;

public final class ExceptionUtils {

    public static final int SC_NOT_FOUND = 404;
    public static final int SC_NOT_VERIFIED = 403;
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_CONFLICT = 409;

    private ExceptionUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
