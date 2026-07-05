package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.db.Pool;
import ar.edu.itba.paw.models.db.Request;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.persistence.config.TestConfig;
import ar.edu.itba.paw.persistence.dao.RequestDaoJpa;
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
public class RequestDaoJpaTest {

    private static final int EXISTING_REQUEST_ID = 1;
    private static final int EXISTING_REQUEST_ID_5 = 5;
    private static final int EXISTING_REQUEST_ID_6 = 6;

    private static final int EXISTING_USER_ID = 3;
    private static final int EXISTING_POOL_ID = 1;
    private static final int EXISTING_COMPANY_ID = 1;
    private static final int EXISTING_DOCUMENT_ID = 3;

    private static final int NON_EXISTING_REQUEST_ID = 9999;

    private static final String REQUESTS_TABLE = "requests";

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private RequestDaoJpa requestDao;

    @Autowired
    private DataSource ds;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    // ==================== TESTS PARA CREATE ====================

    @Test
    public void testCreateRequest() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REQUESTS_TABLE);
        User user = em.find(User.class, EXISTING_USER_ID);
        Pool pool = em.find(Pool.class, EXISTING_POOL_ID);

        Request request = requestDao.create(25, user, pool);
        em.flush();

        Assert.assertNotNull(request);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, REQUESTS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "quantity = 25 AND status = 'PENDING' AND user_id = " + EXISTING_USER_ID
        ));
    }

    @Test
    public void testCreateRequestDefaultStatusPending() {
        User user = em.find(User.class, EXISTING_USER_ID);
        Pool pool = em.find(Pool.class, EXISTING_POOL_ID);

        Request request = requestDao.create(15, user, pool);
        em.flush();

        Assert.assertNotNull(request);
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "quantity = 15 AND status = 'PENDING'"
        ));
    }

    @Test
    public void testCreateMultipleRequests() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REQUESTS_TABLE);
        User user = em.find(User.class, EXISTING_USER_ID);
        Pool pool = em.find(Pool.class, EXISTING_POOL_ID);

        Request request1 = requestDao.create(10, user, pool);
        Request request2 = requestDao.create(20, user, pool);
        em.flush();

        Assert.assertNotNull(request1);
        Assert.assertNotNull(request2);
        Assert.assertEquals(initialRows + 2, JdbcTestUtils.countRowsInTable(jdbcTemplate, REQUESTS_TABLE));
    }

    // ==================== TESTS PARA FIND BY ID ====================

    @Test
    public void testFindByIdExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + EXISTING_REQUEST_ID
        );
        Assert.assertEquals(1, count);

        Request foundRequest = requestDao.findById(EXISTING_REQUEST_ID).orElse(null);

        Assert.assertNotNull(foundRequest);
        Assert.assertEquals(EXISTING_REQUEST_ID, foundRequest.getId());

        int verifyCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + EXISTING_REQUEST_ID + " AND quantity = 10"
        );
        Assert.assertEquals(1, verifyCount);
    }

    @Test
    public void testFindByIdNotExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + NON_EXISTING_REQUEST_ID
        );
        Assert.assertEquals(0, count);

        Request foundRequest = requestDao.findById(NON_EXISTING_REQUEST_ID).orElse(null);

        Assert.assertNull(foundRequest);
    }

    // ==================== TESTS PARA EDIT ====================

    @Test
    public void testEditRequest() {
        int newQuantity = 30;

        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + EXISTING_REQUEST_ID_5 + " AND quantity = 10"
        );
        Assert.assertEquals(1, initialCount);

        requestDao.edit(EXISTING_REQUEST_ID_5, newQuantity, null, null);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + EXISTING_REQUEST_ID_5 + " AND quantity = " + newQuantity
        );
        Assert.assertEquals(1, updatedCount);
    }

    // ==================== TESTS PARA DELETE ====================

    @Test
    public void testDeleteRequest() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REQUESTS_TABLE);
        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + EXISTING_REQUEST_ID_6
        );
        Assert.assertEquals(1, initialCount);

        requestDao.delete(EXISTING_REQUEST_ID_6);
        em.flush();

        Assert.assertEquals(initialRows - 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, REQUESTS_TABLE));
        int deletedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + EXISTING_REQUEST_ID_6
        );
        Assert.assertEquals(0, deletedCount);
    }

    @Test
    public void testDeleteRemovesFromDatabase() {
        User user = em.find(User.class, EXISTING_USER_ID);
        Pool pool = em.find(Pool.class, EXISTING_POOL_ID);
        Request request = requestDao.create(50, user, pool);
        em.flush();

        int createdId = request.getId();
        int afterCreate = JdbcTestUtils.countRowsInTable(jdbcTemplate, REQUESTS_TABLE);

        requestDao.delete(createdId);
        em.flush();

        int afterDelete = JdbcTestUtils.countRowsInTable(jdbcTemplate, REQUESTS_TABLE);
        Assert.assertEquals(afterCreate - 1, afterDelete);

        int deletedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + createdId
        );
        Assert.assertEquals(0, deletedCount);
    }

    // ==================== TESTS PARA SET STATUS ====================

    @Test
    public void testSetStatusToAccepted() {
        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + EXISTING_REQUEST_ID_5 + " AND status = 'PENDING'"
        );
        Assert.assertEquals(1, initialCount);

        requestDao.setStatus(EXISTING_REQUEST_ID_5, Request.Status.ACCEPTED);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + EXISTING_REQUEST_ID_5 + " AND status = 'ACCEPTED'"
        );
        Assert.assertEquals(1, updatedCount);
    }

    @Test
    public void testSetStatusToRejected() {
        requestDao.setStatus(EXISTING_REQUEST_ID_5, Request.Status.REJECTED);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + EXISTING_REQUEST_ID_5 + " AND status = 'REJECTED'"
        );
        Assert.assertEquals(1, updatedCount);
    }

    @Test
    public void testSetStatusToDelivered() {
        requestDao.setStatus(EXISTING_REQUEST_ID_5, Request.Status.DELIVERED);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + EXISTING_REQUEST_ID_5 + " AND status = 'DELIVERED'"
        );
        Assert.assertEquals(1, updatedCount);
    }

    // ==================== TESTS PARA FIND FINISHED BY PRODUCT AND USER ====================

    @Test
    public void testFindFinishedByProductAndUser() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "id = " + EXISTING_REQUEST_ID + " AND status = 'DELIVERED' AND user_id = " + EXISTING_USER_ID
        );
        Assert.assertTrue(count >= 1);

        Request found = requestDao.findFinishedByProductAndUser(1, EXISTING_USER_ID).orElse(null);

        Assert.assertNotNull(found);
        Assert.assertEquals(EXISTING_USER_ID, found.getUser().getId());
    }

    @Test
    public void testFindFinishedByProductAndUserNotExists() {
        Request found = requestDao.findFinishedByProductAndUser(9999, EXISTING_USER_ID).orElse(null);

        Assert.assertNull(found);
    }

    // ==================== TESTS PARA FIND BY COMPANY AND USER ====================

    @Test
    public void testFindByCompanyAndUser() {
        Request found = requestDao.findByCompanyAndUser(EXISTING_COMPANY_ID, EXISTING_USER_ID).orElse(null);

        Assert.assertNotNull(found);
        Assert.assertEquals(EXISTING_USER_ID, found.getUser().getId());
    }

    @Test
    public void testFindByCompanyAndUserNotExists() {
        Request found = requestDao.findByCompanyAndUser(9999, EXISTING_USER_ID).orElse(null);

        Assert.assertNull(found);
    }

    // ==================== TESTS PARA FIND BY PAYMENT ====================

    @Test
    public void testFindByPaymentDownPayment() {
        int paymentId = EXISTING_DOCUMENT_ID;

        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "down_payment_document_id = " + paymentId
        );
        Assert.assertTrue(count > 0);

        Request found = requestDao.findByPayment(paymentId).orElse(null);

        Assert.assertNotNull(found);
    }

    @Test
    public void testFindByPaymentFinalPayment() {
        int paymentId = 4;

        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "final_payment_document_id = " + paymentId
        );
        Assert.assertTrue(count > 0);

        Request found = requestDao.findByPayment(paymentId).orElse(null);

        Assert.assertNotNull(found);
    }

    @Test
    public void testFindByPaymentNotExists() {
        int paymentId = 9999;

        Request found = requestDao.findByPayment(paymentId).orElse(null);

        Assert.assertNull(found);
    }

    // ==================== TESTS PARA FILTER ====================

    @Test
    public void testFilterNoParams() {
        int totalRequests = JdbcTestUtils.countRowsInTable(jdbcTemplate, REQUESTS_TABLE);

        Paginator<Request> result = requestDao.filter(null, null, null, null, null, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(totalRequests, result.getTotalItems());
    }

    @Test
    public void testFilterByUserId() {
        int requestsForUser = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "user_id = " + EXISTING_USER_ID
        );
        Assert.assertTrue(requestsForUser > 0);

        Paginator<Request> result = requestDao.filter(null, null, null, null, null, null, EXISTING_USER_ID, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(requestsForUser, result.getTotalItems());
    }

    @Test
    public void testFilterByPoolId() {
        int requestsForPool = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "pool_id = " + EXISTING_POOL_ID
        );
        Assert.assertTrue(requestsForPool > 0);

        Paginator<Request> result = requestDao.filter(null, null, null, null, null, EXISTING_POOL_ID, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(requestsForPool, result.getTotalItems());
    }

    @Test
    public void testFilterByRequestStatus() {
        int pendingRequests = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "status = 'PENDING'"
        );
        Assert.assertTrue(pendingRequests > 0);

        Paginator<Request> result = requestDao.filter(null, null, null, null, "PENDING", null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(pendingRequests, result.getTotalItems());
    }

    @Test
    public void testFilterCombinedParams() {
        int expectedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REQUESTS_TABLE,
                "user_id = " + EXISTING_USER_ID + " AND pool_id = " + EXISTING_POOL_ID
        );

        Paginator<Request> result = requestDao.filter(null, null, null, null, null, EXISTING_POOL_ID, EXISTING_USER_ID, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(expectedCount, result.getTotalItems());
    }

    @Test
    public void testFilterOrderByIdDesc() {
        Paginator<Request> result = requestDao.filter(null, null, null, null, null, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.getList().isEmpty());

        for (int i = 0; i < result.getList().size() - 1; i++) {
            Assert.assertTrue(result.getList().get(i).getId() >= result.getList().get(i + 1).getId());
        }
    }

    @Test
    public void testFilterOrderByIdAsc() {
        Paginator<Request> result = requestDao.filter(null, null, null, null, null, null, null, 0, "id", false);

        Assert.assertNotNull(result);
        Assert.assertFalse("Debe haber al menos un request", result.getList().isEmpty());

        if (result.getList().size() > 1) {
            int previousId = result.getList().getFirst().getId();
            for (int i = 1; i < result.getList().size(); i++) {
                int currentId = result.getList().get(i).getId();
                Assert.assertTrue("ID " + currentId + " debe ser >= que " + previousId + " (orden ascendente)",
                    currentId >= previousId);
                previousId = currentId;
            }
        }
    }

    @Test
    public void testFilterPagination() {
        User user = em.find(User.class, EXISTING_USER_ID);
        Pool pool = em.find(Pool.class, EXISTING_POOL_ID);

        for (int i = 0; i < 15; i++) {
            requestDao.create(5 + i, user, pool);
        }
        em.flush();

        int totalRequests = JdbcTestUtils.countRowsInTable(jdbcTemplate, REQUESTS_TABLE);
        Assert.assertTrue(totalRequests >= 15);

        Paginator<Request> page0 = requestDao.filter(null, null, null, null, null, null, null, 0, "id", true);
        Paginator<Request> page1 = requestDao.filter(null, null, null, null, null, null, null, 1, "id", true);

        Assert.assertNotNull(page0);
        Assert.assertNotNull(page1);
        Assert.assertEquals(totalRequests, page0.getTotalItems());
        Assert.assertEquals(totalRequests, page1.getTotalItems());
        Assert.assertTrue(page0.getList().size() <= Paginator.DEFAULT_PAGE_SIZE);
    }

}
