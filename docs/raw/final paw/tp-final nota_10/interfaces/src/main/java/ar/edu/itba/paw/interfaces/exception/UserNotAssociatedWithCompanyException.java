package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.CustomRuntimeException;
import ar.edu.itba.paw.interfaces.exception.utils.ExceptionUtils;

public class UserNotAssociatedWithCompanyException extends CustomRuntimeException {

    public UserNotAssociatedWithCompanyException() {
        super(ExceptionUtils.SC_BAD_REQUEST, "exceptions.user.notAssociatedWithCompany");
    }

}

