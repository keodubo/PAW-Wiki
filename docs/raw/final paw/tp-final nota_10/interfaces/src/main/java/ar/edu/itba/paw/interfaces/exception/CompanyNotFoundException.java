package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.ResourceNotFoundException;

public class CompanyNotFoundException extends ResourceNotFoundException {

    public CompanyNotFoundException() {
        super("exceptions.company.notFound");
    }

}
