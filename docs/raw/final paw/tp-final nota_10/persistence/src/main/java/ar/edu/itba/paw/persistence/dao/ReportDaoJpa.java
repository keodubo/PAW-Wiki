package ar.edu.itba.paw.persistence.dao;

import ar.edu.itba.paw.interfaces.dao.ReportDao;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Report;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class ReportDaoJpa implements ReportDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Report create(final String description, final Date createdAt, final boolean userReported, final Company company, final User user) {
        final Report report = new Report(description, createdAt, userReported, company, user);
        entityManager.persist(report);
        return report;
    }

    @Override
    public Optional<Report> findById(final int id) {
        return Optional.ofNullable(entityManager.find(Report.class, id));
    }

    @Override
    public Paginator<Report> filter(final int userId, final int page) {
        final String idsQuerySql = "SELECT id FROM reports WHERE user_id = :user_id ORDER BY created_at DESC, id DESC";
        final String countQuerySql = "SELECT count(*) FROM reports WHERE user_id = :user_id";

        final Query paginatorQuery = entityManager.createNativeQuery(idsQuerySql);
        final Query countQuery = entityManager.createNativeQuery(countQuerySql);

        countQuery.setParameter("user_id", userId);
        paginatorQuery.setParameter("user_id", userId);

        final long count = ((Number) countQuery.getSingleResult()).longValue();
        final int pageNorm = Math.max(0, page);

        paginatorQuery.setFirstResult(pageNorm * Paginator.DEFAULT_PAGE_SIZE);
        paginatorQuery.setMaxResults(Paginator.DEFAULT_PAGE_SIZE);

        final List<Report> reportList = getReportList(paginatorQuery);
        return new Paginator<>(reportList, pageNorm, Paginator.DEFAULT_PAGE_SIZE, (int) count);
    }

    private List<Report> getReportList(Query paginatorQuery) {
        @SuppressWarnings("unchecked")
        List<Integer> paginatedIdsList = paginatorQuery.getResultList();

        if (paginatedIdsList.isEmpty())
            return Collections.emptyList();

        String orderedQuery = "FROM Report WHERE id IN :ids_list ORDER BY createdAt DESC, id DESC";
        TypedQuery<Report> query = entityManager.createQuery(orderedQuery, Report.class);
        query.setParameter("ids_list", paginatedIdsList);

        return query.getResultList();
    }

}
