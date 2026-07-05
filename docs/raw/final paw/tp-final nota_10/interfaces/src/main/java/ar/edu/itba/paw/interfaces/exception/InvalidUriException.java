package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.CustomRuntimeException;
import ar.edu.itba.paw.interfaces.exception.utils.ExceptionUtils;

public class InvalidUriException extends CustomRuntimeException {

    public InvalidUriException() {
        super(ExceptionUtils.SC_BAD_REQUEST, "exceptions.invalidUri");
    }

}
