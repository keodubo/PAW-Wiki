package ar.edu.itba.paw.interfaces.service;

import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Report;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Optional;

public interface ReportService {

    Report create(final String description, final Company company, final int userId);

    Paginator<Report> filter(final int userId, final int page);

    Optional<Report> findById(final int id);

}
