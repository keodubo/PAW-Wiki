package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.ResourceNotFoundException;

public class ReviewNotFoundException extends ResourceNotFoundException {

    public ReviewNotFoundException() {
        super("exceptions.review.notFound");
    }

}
