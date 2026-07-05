package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.CustomRuntimeException;
import ar.edu.itba.paw.interfaces.exception.utils.ExceptionUtils;

public class InvalidProductRetirementException extends CustomRuntimeException {

    public InvalidProductRetirementException() {
        super(ExceptionUtils.SC_CONFLICT, "exceptions.product.invalidRetirement");
    }

}
