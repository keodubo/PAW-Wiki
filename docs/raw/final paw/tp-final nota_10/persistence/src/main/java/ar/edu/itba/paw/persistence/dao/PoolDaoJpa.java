package ar.edu.itba.paw.persistence.dao;

import ar.edu.itba.paw.interfaces.dao.PoolDao;
import ar.edu.itba.paw.models.db.Location;
import ar.edu.itba.paw.models.db.Pool;
import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class PoolDaoJpa implements PoolDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Pool create(int minQuantity, int downPayment, double price, Product product, Location location) {
        final Pool pool = new Pool(minQuantity, Pool.Status.AVAILABLE, downPayment, price, product, location, new Timestamp(System.currentTimeMillis()));
        entityManager.persist(pool);
        return pool;
    }

    @Override
    public void edit(int id, int minQuantity) {
        Pool pool = entityManager.find(Pool.class, id);
        pool.setMinQuantity(minQuantity);
    }

    @Override
    public void setStatus(int id, Pool.Status status) {
        Pool pool = entityManager.find(Pool.class, id);
        pool.setStatus(status);
    }

    @Override
    public void cancelAvailablePools(final int productId) {
        final Query query = entityManager.createNativeQuery(
                "UPDATE pools SET status = :pool_status_set " +
                        "WHERE product_id = :product_id " +
                        "AND status IN (:pool_status_available, :pool_status_paused)"
        );
        query.setParameter("pool_status_set", Pool.Status.CANCELLED.name());
        query.setParameter("product_id", productId);
        query.setParameter("pool_status_available", Pool.Status.AVAILABLE.name());
        query.setParameter("pool_status_paused", Pool.Status.PAUSED.name());
        query.executeUpdate();
    }

    @Override
    public Optional<Pool> findById(int id) {
        return Optional.ofNullable(entityManager.find(Pool.class, id));
    }

    @Override
    public Paginator<Pool> filter(final Integer productId, final String search, final String status, final Integer companyId, final Double priceMin, final Double priceMax, final Integer locationId, final Integer categoryId, final int page, final String order, final boolean desc) {
        String idsQuerySql = "SELECT pool.id FROM pools pool ";
        String countQuerySql = "SELECT count(*) FROM pools pool ";

        if (order.equals("requested_count")) {
            idsQuerySql = "SELECT pool.id FROM (SELECT p.*, (SELECT COALESCE(SUM(r.quantity), 0) FROM "
                    + "requests r WHERE r.pool_id = id AND (r.status = 'PENDING' OR r.status = 'ACCEPTED' OR r.status = 'DELIVERED')) "
                    + "productsRequestedCount FROM pools p) pool ";
        }

        final StringBuilder where = new StringBuilder();

        if (productId != null || search != null || companyId != null || priceMin != null || priceMax != null
                || categoryId != null || List.of("product_name", "product_rating").contains(order)) {
            String join;
            if (order.equals("product_rating")) {
                join = "JOIN (SELECT p.*, (SELECT COALESCE(AVG(re.rating), 0) FROM reviews re "
                        + "WHERE re.product_id = id) rating FROM products p) product ON pool.product_id = product.id ";
            } else {
                join = "JOIN products product ON pool.product_id = product.id ";
            }
            idsQuerySql += join;
            countQuerySql += join;
        }

        if (productId != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" pool.product_id = :product_id ");

        if (search != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" lower(product.name) LIKE lower(:search) ");

        if (status != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" pool.status = :status ");

        if (companyId != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" product.company_id = :company_id ");

        if (priceMin != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" pool.price >= :price_min ");

        if (priceMax != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" pool.price <= :price_max ");

        if (locationId != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" pool.location_id = :location_id ");

        if (categoryId != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" product.category_id = :category_id ");

        String orderBy, field;
        if (order != null && (field = getOrderByField(order)) != null) {
            orderBy = "ORDER BY " + field + (desc ? " DESC " : " ASC ") + ", id " + (desc ? "DESC" : "ASC");
        } else {
            orderBy = "ORDER BY id " + (desc ? "DESC" : "ASC");
        }

        final Query paginatorQuery = entityManager.createNativeQuery(idsQuerySql + where + orderBy);
        final Query countQuery = entityManager.createNativeQuery(countQuerySql + where);

        if (productId != null) {
            countQuery.setParameter("product_id", productId);
            paginatorQuery.setParameter("product_id", productId);
        }

        if (search != null) {
            String searchParam = "%" + sanitize(search) + "%";
            countQuery.setParameter("search", searchParam);
            paginatorQuery.setParameter("search", searchParam);
        }

        if (status != null) {
            countQuery.setParameter("status", status);
            paginatorQuery.setParameter("status", status);
        }

        if (companyId != null) {
            countQuery.setParameter("company_id", companyId);
            paginatorQuery.setParameter("company_id", companyId);
        }

        if (priceMin != null) {
            countQuery.setParameter("price_min", priceMin);
            paginatorQuery.setParameter("price_min", priceMin);
        }

        if (priceMax != null) {
            countQuery.setParameter("price_max", priceMax);
            paginatorQuery.setParameter("price_max", priceMax);
        }

        if (locationId != null) {
            countQuery.setParameter("location_id", locationId);
            paginatorQuery.setParameter("location_id", locationId);
        }

        if (categoryId != null) {
            countQuery.setParameter("category_id", categoryId);
            paginatorQuery.setParameter("category_id", categoryId);
        }

        final long count = ((Number) countQuery.getSingleResult()).longValue();
        final int pageNorm = Math.max(0, page);

        paginatorQuery.setFirstResult(pageNorm * Paginator.DEFAULT_PAGE_SIZE);
        paginatorQuery.setMaxResults(Paginator.DEFAULT_PAGE_SIZE);

        final List<Pool> poolList = getPoolList(paginatorQuery, orderBy);
        return new Paginator<>(poolList, pageNorm, Paginator.DEFAULT_PAGE_SIZE, (int) count);
    }

    private List<Pool> getPoolList(Query paginatorQuery, String orderBy) {
        @SuppressWarnings("unchecked")
        List<Integer> paginatedIdsList = paginatorQuery.getResultList();

        if (paginatedIdsList.isEmpty())
            return Collections.emptyList();

        String orderedQuery = "FROM Pool pool WHERE pool.id IN :ids_list " + orderBy;
        TypedQuery<Pool> query = entityManager.createQuery(orderedQuery, Pool.class);
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
            case "product_name" -> "product.name";
            case "price" -> "pool.price";
            case "product_rating" -> "product.rating";
            case "requested_count" -> "pool.productsRequestedCount";
            default -> null;
        };
    }

}
