package ar.edu.itba.paw.persistence.dao;

import ar.edu.itba.paw.interfaces.dao.RequestDao;
import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.Pool;
import ar.edu.itba.paw.models.db.Request;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class RequestDaoJpa implements RequestDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Request create(final int quantity, final User user, final Pool pool) {
        final Request request = new Request(quantity, Request.Status.PENDING, user, pool);
        entityManager.persist(request);
        return request;
    }

    @Override
    public void edit(final int id, final int quantity, final Document downPayment, final Document finalPayment) {
        Request request = entityManager.find(Request.class, id);
        request.setQuantity(quantity);
        request.setDownPayment(downPayment);
        request.setFinalPayment(finalPayment);
    }

    @Override
    public void delete(int id) {
        Request request = entityManager.find(Request.class, id);
        entityManager.remove(request);
    }

    @Override
    public void setStatus(int id, Request.Status status) {
        Request request = entityManager.find(Request.class, id);
        request.setStatus(status);
    }

    @Override
    public void rejectPendingRequests(final int productId) {
        final Query query = entityManager.createNativeQuery(
                "UPDATE requests SET status = :request_status_set " +
                        "WHERE pool_id IN (SELECT id FROM pools WHERE product_id = :product_id) " +
                        "AND status = :request_status"
        );
        query.setParameter("request_status_set", Request.Status.REJECTED.name());
        query.setParameter("product_id", productId);
        query.setParameter("request_status", Request.Status.PENDING.name());
        query.executeUpdate();
    }

    @Override
    public Optional<Request> findById(int id) {
        return Optional.ofNullable(entityManager.find(Request.class, id));
    }

    @Override
    public Optional<Request> findFinishedByProductAndUser(int productId, int userId) {
        TypedQuery<Request> query = entityManager.createQuery("from Request where pool.status = 'FINISHED' and status = 'DELIVERED' and pool.product.id = :product_id and user.id = :user_id", Request.class);
        query.setParameter("product_id", productId);
        query.setParameter("user_id", userId);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<Request> findByCompanyAndUser(int companyId, int userId) {
        TypedQuery<Request> query = entityManager.createQuery("from Request where pool.product.company.id = :company_id and user.id = :user_id", Request.class);
        query.setParameter("company_id", companyId);
        query.setParameter("user_id", userId);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<Request> findByPayment(int paymentId) {
        TypedQuery<Request> query = entityManager.createQuery("from Request where downPayment.id = :down_payment_id or finalPayment.id = :final_payment_id", Request.class);
        query.setParameter("down_payment_id", paymentId);
        query.setParameter("final_payment_id", paymentId);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Paginator<Request> filter(final String search, final Integer companyId, final Integer productId, final String poolStatus, final String requestStatus, final Integer poolId, final Integer userId, final int page, final String order, final boolean desc) {
        String idsQuerySql = "SELECT request.id FROM requests request ";
        String countQuerySql = "SELECT count(*) FROM requests request ";

        final StringBuilder where = new StringBuilder();

        if (search != null || companyId != null || productId != null || poolStatus != null
                || List.of("price", "pool_status").contains(order)) {
            String join = "JOIN pools pool ON request.pool_id = pool.id ";
            if (search != null || companyId != null)
                join += "JOIN products product ON pool.product_id = product.id ";
            idsQuerySql += join;
            countQuerySql += join;
        }

        if (search != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" lower(product.name) LIKE lower(:search) ");

        if (companyId != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" product.company_id = :company_id ");

        if (productId != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" pool.product_id = :product_id ");

        if (poolStatus != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" pool.status = :pool_status ");

        if (requestStatus != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" request.status = :request_status ");

        if (poolId != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" request.pool_id = :pool_id ");

        if (userId != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" request.user_id = :user_id ");

        String orderBy, field;
        if (order != null && (field = getOrderByField(order)) != null) {
            orderBy = "ORDER BY " + field + (desc ? " DESC " : " ASC ") + ", id " + (desc ? "DESC" : "ASC");
        } else {
            orderBy = "ORDER BY id " + (desc ? "DESC" : "ASC");
        }

        final Query paginatorQuery = entityManager.createNativeQuery(idsQuerySql + where + orderBy);
        final Query countQuery = entityManager.createNativeQuery(countQuerySql + where);

        if (search != null) {
            String searchParam = "%" + sanitize(search) + "%";
            countQuery.setParameter("search", searchParam);
            paginatorQuery.setParameter("search", searchParam);
        }

        if (companyId != null) {
            countQuery.setParameter("company_id", companyId);
            paginatorQuery.setParameter("company_id", companyId);
        }

        if (productId != null) {
            countQuery.setParameter("product_id", productId);
            paginatorQuery.setParameter("product_id", productId);
        }

        if (poolStatus != null) {
            countQuery.setParameter("pool_status", poolStatus);
            paginatorQuery.setParameter("pool_status", poolStatus);
        }

        if (requestStatus != null) {
            countQuery.setParameter("request_status", requestStatus);
            paginatorQuery.setParameter("request_status", requestStatus);
        }

        if (poolId != null) {
            countQuery.setParameter("pool_id", poolId);
            paginatorQuery.setParameter("pool_id", poolId);
        }

        if (userId != null) {
            countQuery.setParameter("user_id", userId);
            paginatorQuery.setParameter("user_id", userId);
        }

        final long count = ((Number) countQuery.getSingleResult()).longValue();
        final int pageNorm = Math.max(0, page);

        paginatorQuery.setFirstResult(pageNorm * Paginator.DEFAULT_PAGE_SIZE);
        paginatorQuery.setMaxResults(Paginator.DEFAULT_PAGE_SIZE);

        final List<Request> requestList = getRequestList(paginatorQuery, orderBy);
        return new Paginator<>(requestList, pageNorm, Paginator.DEFAULT_PAGE_SIZE, (int) count);
    }

    private List<Request> getRequestList(Query paginatorQuery, String orderBy) {
        @SuppressWarnings("unchecked")
        List<Integer> paginatedIdsList = paginatorQuery.getResultList();

        if (paginatedIdsList.isEmpty())
            return Collections.emptyList();

        String orderedQuery = "FROM Request request WHERE request.id IN :ids_list " + orderBy;
        TypedQuery<Request> query = entityManager.createQuery(orderedQuery, Request.class);
        query.setParameter("ids_list", paginatedIdsList);

        return query.getResultList();
    }

    private String sanitize(String str) {
        return str.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("'", "''")
                .replace("\"", "\"\"");
    }

    private String getOrderByField(final String order) {
        return switch (order) {
            case "price" -> "pool.price";
            case "pool_status" -> "pool.status";
            case "request_status" -> "request.status";
            default -> null;
        };
    }

}
