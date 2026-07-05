package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.ResourceNotFoundException;

public class LocationNotFoundException extends ResourceNotFoundException {

    public LocationNotFoundException() {
        super("exceptions.location.notFound");
    }

}
