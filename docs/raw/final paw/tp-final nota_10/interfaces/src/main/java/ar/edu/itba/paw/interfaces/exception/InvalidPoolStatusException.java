package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.CustomRuntimeException;
import ar.edu.itba.paw.interfaces.exception.utils.ExceptionUtils;

public class InvalidPoolStatusException extends CustomRuntimeException {

    public InvalidPoolStatusException() {
        super(ExceptionUtils.SC_BAD_REQUEST, "exceptions.pool.invalidStatus");
    }

}
