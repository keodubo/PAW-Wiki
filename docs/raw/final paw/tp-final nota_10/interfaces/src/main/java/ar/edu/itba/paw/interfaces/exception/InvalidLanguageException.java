package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.CustomRuntimeException;
import ar.edu.itba.paw.interfaces.exception.utils.ExceptionUtils;

public class InvalidLanguageException extends CustomRuntimeException {

    public InvalidLanguageException() {
        super(ExceptionUtils.SC_BAD_REQUEST, "exceptions.user.invalidLanguage");
    }

}
