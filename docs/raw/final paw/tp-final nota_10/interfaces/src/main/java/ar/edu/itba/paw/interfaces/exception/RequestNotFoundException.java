package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.ResourceNotFoundException;

public class RequestNotFoundException extends ResourceNotFoundException {

    public RequestNotFoundException() {
        super("exceptions.request.notFound");
    }

}
