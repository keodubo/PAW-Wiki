package ar.edu.itba.paw.interfaces.dao;

import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Report;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Date;
import java.util.Optional;

public interface ReportDao {

    Report create(final String description, final Date createdAt, final boolean userReported, final Company company, final User user);

    Optional<Report> findById(final int id);

    Paginator<Report> filter(final int userId, final int page);

}
