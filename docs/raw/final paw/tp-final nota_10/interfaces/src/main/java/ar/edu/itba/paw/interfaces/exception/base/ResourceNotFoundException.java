package ar.edu.itba.paw.interfaces.exception.base;

import ar.edu.itba.paw.interfaces.exception.utils.ExceptionUtils;

public class ResourceNotFoundException extends CustomRuntimeException {

    public ResourceNotFoundException(final String message) {
        super(ExceptionUtils.SC_NOT_FOUND, message);
    }

}
