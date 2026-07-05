package ar.edu.itba.paw.interfaces.exception;

import ar.edu.itba.paw.interfaces.exception.base.ResourceNotFoundException;

public class ReportNotFoundException extends ResourceNotFoundException {

    public ReportNotFoundException() {
        super("exceptions.report.notFound");
    }

}

