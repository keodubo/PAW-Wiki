package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.ResourceNotFoundException;

public class DocumentNotFoundException extends ResourceNotFoundException {

    public DocumentNotFoundException() {
        super("exceptions.document.notFound");
    }

}
