package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.db.Review;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.persistence.config.TestConfig;
import ar.edu.itba.paw.persistence.dao.ReviewDaoJpa;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

@Transactional
@Rollback
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class ReviewDaoJpaTest {

    private static final int EXISTING_REVIEW_ID = 1;
    private static final String EXISTING_REVIEW_DESCRIPTION = "description1";
    private static final double EXISTING_REVIEW_RATING = 5.0;

    private static final int EXISTING_REVIEW_ID_2 = 2;

    private static final int EXISTING_USER_ID = 3;
    private static final int EXISTING_PRODUCT_ID = 3;

    private static final int NON_EXISTING_REVIEW_ID = 9999;

    private static final String REVIEWS_TABLE = "reviews";

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ReviewDaoJpa reviewDao;

    @Autowired
    private DataSource ds;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    // ==================== TESTS PARA CREATE ====================

    @Test
    public void testCreateReview() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE);
        User reviewer = em.find(User.class, EXISTING_USER_ID);
        Product product = em.find(Product.class, EXISTING_PRODUCT_ID);

        Review review = reviewDao.create(reviewer, product, "Great product!", 5.0);
        em.flush();

        Assert.assertNotNull(review);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "description = 'Great product!' AND rating = 5.0"
        ));
    }

    @Test
    public void testCreateReviewWithDifferentRating() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE);
        User reviewer = em.find(User.class, EXISTING_USER_ID);
        Product product = em.find(Product.class, EXISTING_PRODUCT_ID);

        Review review = reviewDao.create(reviewer, product, "Average product", 3.5);
        em.flush();

        Assert.assertNotNull(review);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "rating = 3.5"
        ));
    }

    @Test
    public void testCreateReviewWithMinRating() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE);
        User reviewer = em.find(User.class, EXISTING_USER_ID);
        Product product = em.find(Product.class, EXISTING_PRODUCT_ID);

        Review review = reviewDao.create(reviewer, product, "Bad product", 1.0);
        em.flush();

        Assert.assertNotNull(review);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "rating = 1.0"
        ));
    }

    @Test
    public void testCreateReviewWithLongDescription() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE);
        User reviewer = em.find(User.class, EXISTING_USER_ID);
        Product product = em.find(Product.class, EXISTING_PRODUCT_ID);
        String longDescription = "A".repeat(1000);

        Review review = reviewDao.create(reviewer, product, longDescription, 4.0);
        em.flush();

        Assert.assertNotNull(review);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE));
    }

    // ==================== TESTS PARA FIND BY ID ====================

    @Test
    public void testFindByIdExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "id = " + EXISTING_REVIEW_ID
        );
        Assert.assertEquals(1, count);

        Review foundReview = reviewDao.findById(EXISTING_REVIEW_ID).orElse(null);

        Assert.assertNotNull(foundReview);
        Assert.assertEquals(EXISTING_REVIEW_ID, foundReview.getId());

        int verifyCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "id = " + EXISTING_REVIEW_ID + " AND description = '" + EXISTING_REVIEW_DESCRIPTION + "'"
        );
        Assert.assertEquals(1, verifyCount);
    }

    @Test
    public void testFindByIdNotExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "id = " + NON_EXISTING_REVIEW_ID
        );
        Assert.assertEquals(0, count);

        Review foundReview = reviewDao.findById(NON_EXISTING_REVIEW_ID).orElse(null);

        Assert.assertNull(foundReview);
    }

    @Test
    public void testFindByIdReturnsCorrectData() {
        Review review = reviewDao.findById(EXISTING_REVIEW_ID).orElse(null);

        Assert.assertNotNull(review);
        Assert.assertEquals(EXISTING_REVIEW_ID, review.getId());
        Assert.assertEquals(EXISTING_REVIEW_DESCRIPTION, review.getDescription());
        Assert.assertEquals(EXISTING_REVIEW_RATING, review.getRating(), 0.01);
    }

    // ==================== TESTS PARA EDIT ====================

    @Test
    public void testEditReview() {
        String newDescription = "Updated review description";
        double newRating = 3.0;

        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "id = " + EXISTING_REVIEW_ID + " AND rating = " + EXISTING_REVIEW_RATING
        );
        Assert.assertEquals(1, initialCount);

        reviewDao.edit(EXISTING_REVIEW_ID, newDescription, newRating);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "id = " + EXISTING_REVIEW_ID + " AND description = '" + newDescription + "' AND rating = " + newRating
        );
        Assert.assertEquals(1, updatedCount);

        int oldCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "id = " + EXISTING_REVIEW_ID + " AND description = '" + EXISTING_REVIEW_DESCRIPTION + "'"
        );
        Assert.assertEquals(0, oldCount);
    }

    @Test
    public void testEditReviewRatingOnly() {
        double newRating = 2.5;

        reviewDao.edit(EXISTING_REVIEW_ID, EXISTING_REVIEW_DESCRIPTION, newRating);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "id = " + EXISTING_REVIEW_ID + " AND rating = " + newRating
        );
        Assert.assertEquals(1, updatedCount);
    }

    // ==================== TESTS PARA DELETE ====================

    @Test
    public void testDeleteReview() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE);
        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "id = " + EXISTING_REVIEW_ID_2
        );
        Assert.assertEquals(1, initialCount);

        reviewDao.delete(EXISTING_REVIEW_ID_2);
        em.flush();

        Assert.assertEquals(initialRows - 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE));
        int deletedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "id = " + EXISTING_REVIEW_ID_2
        );
        Assert.assertEquals(0, deletedCount);
    }

    @Test
    public void testDeleteRemovesFromDatabase() {
        User reviewer = em.find(User.class, EXISTING_USER_ID);
        Product product = em.find(Product.class, EXISTING_PRODUCT_ID);
        Review review = reviewDao.create(reviewer, product, "To delete", 4.0);
        em.flush();

        int createdId = review.getId();
        int afterCreate = JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE);

        reviewDao.delete(createdId);
        em.flush();

        int afterDelete = JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE);
        Assert.assertEquals(afterCreate - 1, afterDelete);

        int deletedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "id = " + createdId
        );
        Assert.assertEquals(0, deletedCount);
    }

    // ==================== TESTS PARA FIND BY USER AND PRODUCT ====================

    @Test
    public void testFindByUserAndProductExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "reviewer_id = " + EXISTING_USER_ID + " AND product_id = " + EXISTING_PRODUCT_ID
        );
        Assert.assertTrue(count > 0);

        Review found = reviewDao.findByUserAndProduct(EXISTING_USER_ID, EXISTING_PRODUCT_ID).orElse(null);

        Assert.assertNotNull(found);
        Assert.assertEquals(EXISTING_USER_ID, found.getReviewer().getId());
        Assert.assertEquals(EXISTING_PRODUCT_ID, found.getProduct().getId());
    }

    @Test
    public void testFindByUserAndProductNotExists() {
        int userId = 1;
        int productId = 1;

        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "reviewer_id = " + userId + " AND product_id = " + productId
        );
        Assert.assertEquals(0, count);

        Review found = reviewDao.findByUserAndProduct(userId, productId).orElse(null);

        Assert.assertNull(found);
    }

    // ==================== TESTS PARA FILTER ====================

    @Test
    public void testFilterByProductId() {
        int reviewsForProduct = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "product_id = " + EXISTING_PRODUCT_ID
        );
        Assert.assertTrue(reviewsForProduct > 0);

        Paginator<Review> result = reviewDao.filter(EXISTING_PRODUCT_ID, null, 0);

        Assert.assertNotNull(result);
        Assert.assertEquals(reviewsForProduct, result.getTotalItems());
        Assert.assertTrue(result.getList().stream()
                .allMatch(r -> r.getProduct().getId() == EXISTING_PRODUCT_ID));
    }

    @Test
    public void testFilterByProductIdAndUserId() {
        int reviewsForProductAndUser = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "product_id = " + EXISTING_PRODUCT_ID + " AND reviewer_id = " + EXISTING_USER_ID
        );
        Assert.assertTrue(reviewsForProductAndUser > 0);

        Paginator<Review> result = reviewDao.filter(EXISTING_PRODUCT_ID, EXISTING_USER_ID, 0);

        Assert.assertNotNull(result);
        Assert.assertEquals(reviewsForProductAndUser, result.getTotalItems());
        Assert.assertTrue(result.getList().stream()
                .allMatch(r -> r.getReviewer().getId() == EXISTING_USER_ID));
    }

    @Test
    public void testFilterByProductWithNoReviews() {
        int productWithNoReviews = 1;
        int reviewsCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "product_id = " + productWithNoReviews
        );
        Assert.assertEquals(0, reviewsCount);

        Paginator<Review> result = reviewDao.filter(productWithNoReviews, null, 0);

        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getTotalItems());
        Assert.assertTrue(result.getList().isEmpty());
    }

    @Test
    public void testFilterOrderedByCreatedAtDesc() {
        Paginator<Review> result = reviewDao.filter(EXISTING_PRODUCT_ID, null, 0);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.getList().isEmpty());

        for (int i = 0; i < result.getList().size() - 1; i++) {
            Review current = result.getList().get(i);
            Review next = result.getList().get(i + 1);
            Assert.assertTrue(current.getCreatedAt().compareTo(next.getCreatedAt()) >= 0);
        }
    }

    @Test
    public void testFilterPagination() {
        User reviewer = em.find(User.class, EXISTING_USER_ID);
        Product product = em.find(Product.class, EXISTING_PRODUCT_ID);

        for (int i = 0; i < 15; i++) {
            reviewDao.create(reviewer, product, "Pagination test review " + i, 4.0);
        }
        em.flush();

        int totalReviews = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "product_id = " + EXISTING_PRODUCT_ID
        );
        Assert.assertTrue(totalReviews >= 15);

        Paginator<Review> page0 = reviewDao.filter(EXISTING_PRODUCT_ID, null, 0);
        Paginator<Review> page1 = reviewDao.filter(EXISTING_PRODUCT_ID, null, 1);

        Assert.assertNotNull(page0);
        Assert.assertNotNull(page1);
        Assert.assertEquals(totalReviews, page0.getTotalItems());
        Assert.assertEquals(totalReviews, page1.getTotalItems());
        Assert.assertTrue(page0.getList().size() <= Paginator.DEFAULT_PAGE_SIZE);
    }

    // ==================== TEST DE INTEGRIDAD ====================

    @Test
    public void testReviewFieldsAreNotNull() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE);
        User reviewer = em.find(User.class, EXISTING_USER_ID);
        Product product = em.find(Product.class, EXISTING_PRODUCT_ID);

        Review review = reviewDao.create(reviewer, product, "Not null test", 5.0);
        em.flush();

        Assert.assertNotNull(review);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, REVIEWS_TABLE));

        int countNotNull = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "description = 'Not null test' AND description IS NOT NULL AND rating IS NOT NULL AND created_at IS NOT NULL"
        );
        Assert.assertEquals(1, countNotNull);
    }

    @Test
    public void testReviewRatingRange() {
        User reviewer = em.find(User.class, EXISTING_USER_ID);
        Product product = em.find(Product.class, EXISTING_PRODUCT_ID);

        Review review1 = reviewDao.create(reviewer, product, "Min rating", 1.0);
        Review review5 = reviewDao.create(reviewer, product, "Max rating", 5.0);
        em.flush();

        Assert.assertNotNull(review1);
        Assert.assertNotNull(review5);

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "description = 'Min rating' AND rating = 1.0"
        ));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REVIEWS_TABLE,
                "description = 'Max rating' AND rating = 5.0"
        ));
    }

}
