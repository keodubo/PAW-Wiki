package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.ResourceNotFoundException;

public class PoolNotFoundException extends ResourceNotFoundException {

    public PoolNotFoundException() {
        super("exceptions.pool.notFound");
    }

}

