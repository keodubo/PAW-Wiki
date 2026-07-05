package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.CustomRuntimeException;
import ar.edu.itba.paw.interfaces.exception.utils.ExceptionUtils;

public class InvalidBlockLevelException extends CustomRuntimeException {

    public InvalidBlockLevelException() {
        super(ExceptionUtils.SC_BAD_REQUEST, "exceptions.user.invalidBlockLevel");
    }

}
