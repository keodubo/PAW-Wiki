package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.ReportDao;
import ar.edu.itba.paw.interfaces.exception.UserNotFoundException;
import ar.edu.itba.paw.interfaces.service.ReportService;
import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Report;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final ReportDao reportDao;

    private final UserService userService;

    @Autowired
    public ReportServiceImpl(final ReportDao reportDao, final UserService userService) {
        this.reportDao = reportDao;
        this.userService = userService;
    }

    @Transactional
    @Override
    public Report create(final String description, final Company company, final int userId) {
        final User user = userService.findById(userId).orElseThrow(UserNotFoundException::new);
        final Report report = reportDao.create(description, new Date(), true, company, user);
        LOGGER.info("Report created: {} (company: {}, user: {})", report.getId(), company.getId(), userId);
        return report;
    }

    @Transactional(readOnly = true)
    @Override
    public Paginator<Report> filter(final int userId, final int page) {
        return reportDao.filter(userId, Math.max(page, 0));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Report> findById(final int id) {
        return reportDao.findById(id);
    }

}
