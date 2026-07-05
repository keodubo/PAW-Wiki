package ar.edu.itba.paw.persistence.dao;

import ar.edu.itba.paw.interfaces.dao.ReviewDao;
import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.db.Review;
import ar.edu.itba.paw.models.db.User;
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
public class ReviewDaoJpa implements ReviewDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Review create(final User reviewer, final Product product, final String description, final double rating) {
        final Review review = new Review(reviewer, product, description, rating, new Timestamp(System.currentTimeMillis()));
        entityManager.persist(review);
        return review;
    }

    @Override
    public void edit(int id, String description, double rating) {
        Review review = entityManager.find(Review.class, id);
        review.setDescription(description);
        review.setRating(rating);
    }

    @Override
    public void delete(int id) {
        Review review = entityManager.find(Review.class, id);
        entityManager.remove(review);
    }

    @Override
    public Optional<Review> findById(int id) {
        return Optional.ofNullable(entityManager.find(Review.class, id));
    }

    @Override
    public Optional<Review> findByUserAndProduct(final int userId, final int productId) {
        TypedQuery<Review> query = entityManager.createQuery("from Review where reviewer.id = :user_id and product.id = :product_id", Review.class);
        query.setParameter("user_id", userId);
        query.setParameter("product_id", productId);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Paginator<Review> filter(final int productId, final Integer userId, final int page) {
        final String idsQuerySql = "SELECT id FROM reviews ";
        final String countQuerySql = "SELECT count(*) FROM reviews ";

        final StringBuilder where = new StringBuilder("WHERE product_id = :product_id ");

        if (userId != null)
            where.append("AND reviewer_id = :user_id ");

        final String orderBy = "ORDER BY created_at DESC, id DESC";
        final Query paginatorQuery = entityManager.createNativeQuery(idsQuerySql + where + orderBy);
        final Query countQuery = entityManager.createNativeQuery(countQuerySql + where);

        countQuery.setParameter("product_id", productId);
        paginatorQuery.setParameter("product_id", productId);

        if (userId != null) {
            countQuery.setParameter("user_id", userId);
            paginatorQuery.setParameter("user_id", userId);
        }

        final long count = ((Number) countQuery.getSingleResult()).longValue();
        final int pageNorm = Math.max(0, page);

        paginatorQuery.setFirstResult(pageNorm * Paginator.DEFAULT_PAGE_SIZE);
        paginatorQuery.setMaxResults(Paginator.DEFAULT_PAGE_SIZE);

        final List<Review> reviewList = getReviewList(paginatorQuery, orderBy);
        return new Paginator<>(reviewList, pageNorm, Paginator.DEFAULT_PAGE_SIZE, (int) count);
    }

    private List<Review> getReviewList(Query paginatorQuery, String orderBy) {
        @SuppressWarnings("unchecked")
        List<Review> paginatedIdsList = paginatorQuery.getResultList();

        if (paginatedIdsList.isEmpty())
            return Collections.emptyList();

        String orderedQuery = "from Review where id in :ids_list " + orderBy;
        TypedQuery<Review> query = entityManager.createQuery(orderedQuery, Review.class);
        query.setParameter("ids_list", paginatedIdsList);

        return query.getResultList();
    }

}
