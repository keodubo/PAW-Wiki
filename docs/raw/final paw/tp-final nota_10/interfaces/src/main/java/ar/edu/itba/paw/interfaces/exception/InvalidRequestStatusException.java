package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.CustomRuntimeException;
import ar.edu.itba.paw.interfaces.exception.utils.ExceptionUtils;

public class InvalidRequestStatusException extends CustomRuntimeException {

    public InvalidRequestStatusException() {
        super(ExceptionUtils.SC_BAD_REQUEST, "exceptions.request.invalidStatus");
    }

}
