package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.CustomRuntimeException;
import ar.edu.itba.paw.interfaces.exception.utils.ExceptionUtils;

public class InvalidDocumentVisibilityException extends CustomRuntimeException {

    public InvalidDocumentVisibilityException() {
        super(ExceptionUtils.SC_BAD_REQUEST, "exceptions.document.invalidVisibility");
    }

}

