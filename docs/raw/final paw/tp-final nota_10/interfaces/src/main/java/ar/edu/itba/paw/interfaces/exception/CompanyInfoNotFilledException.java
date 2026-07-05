package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.CustomRuntimeException;
import ar.edu.itba.paw.interfaces.exception.utils.ExceptionUtils;

public class CompanyInfoNotFilledException extends CustomRuntimeException {

    public CompanyInfoNotFilledException() {
        super(ExceptionUtils.SC_BAD_REQUEST, "exceptions.company.infoNotFilled");
    }

}
