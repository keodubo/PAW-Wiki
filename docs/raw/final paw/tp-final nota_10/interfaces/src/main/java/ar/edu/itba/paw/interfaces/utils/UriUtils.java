package ar.edu.itba.paw.interfaces.utils;

import ar.edu.itba.paw.interfaces.exception.InvalidUriException;

public final class UriUtils {

    private UriUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static int extractIdFromUri(final String uri) {
        if (uri == null || uri.isEmpty()) {
            throw new InvalidUriException();
        }
        String[] parts = uri.split("/");
        String lastPart = parts[parts.length - 1];
        try {
            return Integer.parseInt(lastPart);
        } catch (NumberFormatException e) {
            throw new InvalidUriException();
        }
    }

}
