package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.db.Location;
import ar.edu.itba.paw.models.db.Pool;
import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.persistence.config.TestConfig;
import ar.edu.itba.paw.persistence.dao.PoolDaoJpa;
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
public class PoolDaoJpaTest {

    private static final int EXISTING_POOL_ID = 1;

    private static final int EXISTING_PRODUCT_ID = 1;
    private static final int EXISTING_LOCATION_ID = 1;

    private static final int NON_EXISTING_POOL_ID = 9999;

    private static final String POOLS_TABLE = "pools";

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PoolDaoJpa poolDao;

    @Autowired
    private DataSource ds;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    // ==================== TESTS PARA CREATE ====================

    @Test
    public void testCreatePool() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, POOLS_TABLE);
        Product product = em.find(Product.class, EXISTING_PRODUCT_ID);
        Location location = em.find(Location.class, EXISTING_LOCATION_ID);

        Pool pool = poolDao.create(50, 10, 150.0, product, location);
        em.flush();

        Assert.assertNotNull(pool);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, POOLS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "min_quantity = 50 AND down_payment = 10 AND price = 150.0 AND status = 'AVAILABLE'"
        ));
    }

    @Test
    public void testCreatePoolWithDifferentValues() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, POOLS_TABLE);
        Product product = em.find(Product.class, EXISTING_PRODUCT_ID);
        Location location = em.find(Location.class, EXISTING_LOCATION_ID);

        Pool pool = poolDao.create(200, 20, 500.50, product, location);
        em.flush();

        Assert.assertNotNull(pool);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, POOLS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "min_quantity = 200 AND down_payment = 20 AND price = 500.50"
        ));
    }

    @Test
    public void testCreatePoolWithZeroDownPayment() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, POOLS_TABLE);
        Product product = em.find(Product.class, EXISTING_PRODUCT_ID);
        Location location = em.find(Location.class, EXISTING_LOCATION_ID);

        Pool pool = poolDao.create(100, 0, 250.0, product, location);
        em.flush();

        Assert.assertNotNull(pool);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, POOLS_TABLE));

        int createdId = pool.getId();
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "id = " + createdId + " AND down_payment = 0 AND status = 'AVAILABLE'"
        ));
    }

    // ==================== TESTS PARA FIND BY ID ====================

    @Test
    public void testFindByIdExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "id = " + EXISTING_POOL_ID
        );
        Assert.assertEquals(1, count);

        Pool foundPool = poolDao.findById(EXISTING_POOL_ID).orElse(null);

        Assert.assertNotNull(foundPool);
        Assert.assertEquals(EXISTING_POOL_ID, foundPool.getId());

        int verifyCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "id = " + EXISTING_POOL_ID + " AND min_quantity = 100"
        );
        Assert.assertEquals(1, verifyCount);
    }

    @Test
    public void testFindByIdNotExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "id = " + NON_EXISTING_POOL_ID
        );
        Assert.assertEquals(0, count);

        Pool foundPool = poolDao.findById(NON_EXISTING_POOL_ID).orElse(null);

        Assert.assertNull(foundPool);
    }

    // ==================== TESTS PARA EDIT ====================

    @Test
    public void testEditPoolMinQuantity() {
        int newMinQuantity = 150;

        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "id = " + EXISTING_POOL_ID + " AND min_quantity = 100"
        );
        Assert.assertEquals(1, initialCount);

        poolDao.edit(EXISTING_POOL_ID, newMinQuantity);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "id = " + EXISTING_POOL_ID + " AND min_quantity = " + newMinQuantity
        );
        Assert.assertEquals(1, updatedCount);

        int oldCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "id = " + EXISTING_POOL_ID + " AND min_quantity = 100"
        );
        Assert.assertEquals(0, oldCount);
    }

    // ==================== TESTS PARA SET STATUS ====================

    @Test
    public void testSetStatusToDelivering() {
        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "id = " + EXISTING_POOL_ID + " AND status = 'AVAILABLE'"
        );
        Assert.assertEquals(1, initialCount);

        poolDao.setStatus(EXISTING_POOL_ID, Pool.Status.DELIVERING);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "id = " + EXISTING_POOL_ID + " AND status = 'DELIVERING'"
        );
        Assert.assertEquals(1, updatedCount);
    }

    @Test
    public void testSetStatusToPaused() {
        poolDao.setStatus(EXISTING_POOL_ID, Pool.Status.PAUSED);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "id = " + EXISTING_POOL_ID + " AND status = 'PAUSED'"
        );
        Assert.assertEquals(1, updatedCount);
    }

    @Test
    public void testSetStatusToCancelled() {
        poolDao.setStatus(EXISTING_POOL_ID, Pool.Status.CANCELLED);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "id = " + EXISTING_POOL_ID + " AND status = 'CANCELLED'"
        );
        Assert.assertEquals(1, updatedCount);
    }

    @Test
    public void testSetStatusToFinished() {
        poolDao.setStatus(EXISTING_POOL_ID, Pool.Status.FINISHED);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "id = " + EXISTING_POOL_ID + " AND status = 'FINISHED'"
        );
        Assert.assertEquals(1, updatedCount);
    }

    // ==================== TESTS PARA FILTER ====================

    @Test
    public void testFilterNoParams() {
        int totalPools = JdbcTestUtils.countRowsInTable(jdbcTemplate, POOLS_TABLE);

        Paginator<Pool> result = poolDao.filter(null, null, null, null, null, null, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(totalPools, result.getTotalItems());
    }

    @Test
    public void testFilterByProductId() {
        int poolsForProduct = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "product_id = " + EXISTING_PRODUCT_ID
        );
        Assert.assertTrue(poolsForProduct > 0);

        Paginator<Pool> result = poolDao.filter(EXISTING_PRODUCT_ID, null, null, null, null, null, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(poolsForProduct, result.getTotalItems());
    }

    @Test
    public void testFilterByStatus() {
        int availablePools = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "status = 'AVAILABLE'"
        );
        Assert.assertTrue(availablePools > 0);

        Paginator<Pool> result = poolDao.filter(null, null, "AVAILABLE", null, null, null, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(availablePools, result.getTotalItems());
    }

    @Test
    public void testFilterByLocationId() {
        int poolsForLocation = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "location_id = " + EXISTING_LOCATION_ID
        );
        Assert.assertTrue(poolsForLocation > 0);

        Paginator<Pool> result = poolDao.filter(null, null, null, null, null, null, EXISTING_LOCATION_ID, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(poolsForLocation, result.getTotalItems());
    }

    @Test
    public void testFilterByPriceRange() {
        int poolsInRange = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "price >= 50.0 AND price <= 250.0"
        );

        Paginator<Pool> result = poolDao.filter(null, null, null, null, 50.0, 250.0, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(poolsInRange, result.getTotalItems());
    }

    @Test
    public void testFilterByMinPrice() {
        int poolsAboveMin = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "price >= 150.0"
        );

        Paginator<Pool> result = poolDao.filter(null, null, null, null, 150.0, null, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(poolsAboveMin, result.getTotalItems());
    }

    @Test
    public void testFilterByMaxPrice() {
        int poolsBelowMax = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "price <= 250.0"
        );

        Paginator<Pool> result = poolDao.filter(null, null, null, null, null, 250.0, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(poolsBelowMax, result.getTotalItems());
    }

    @Test
    public void testFilterCombinedParams() {
        int expectedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                POOLS_TABLE,
                "product_id = " + EXISTING_PRODUCT_ID + " AND status = 'AVAILABLE' AND location_id = " + EXISTING_LOCATION_ID
        );

        Paginator<Pool> result = poolDao.filter(EXISTING_PRODUCT_ID, null, "AVAILABLE", null, null, null, EXISTING_LOCATION_ID, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(expectedCount, result.getTotalItems());
    }

    @Test
    public void testFilterOrderByIdDesc() {
        Paginator<Pool> result = poolDao.filter(null, null, null, null, null, null, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.getList().isEmpty());

        for (int i = 0; i < result.getList().size() - 1; i++) {
            Assert.assertTrue(result.getList().get(i).getId() >= result.getList().get(i + 1).getId());
        }
    }

    @Test
    public void testFilterOrderByIdAsc() {
        Paginator<Pool> result = poolDao.filter(null, null, null, null, null, null, null, null, 0, "id", false);

        Assert.assertNotNull(result);
        Assert.assertFalse("Debe haber al menos un pool", result.getList().isEmpty());

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
    public void testFilterWithInvalidStatus() {
        Paginator<Pool> result = poolDao.filter(null, null, "INVALID_STATUS", null, null, null, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getTotalItems());
    }

    @Test
    public void testFilterPagination() {
        Product product = em.find(Product.class, EXISTING_PRODUCT_ID);
        Location location = em.find(Location.class, EXISTING_LOCATION_ID);

        for (int i = 0; i < 15; i++) {
            poolDao.create(100 + i, i, 100.0 + i, product, location);
        }
        em.flush();

        int totalPools = JdbcTestUtils.countRowsInTable(jdbcTemplate, POOLS_TABLE);
        Assert.assertTrue(totalPools >= 15);

        Paginator<Pool> page0 = poolDao.filter(null, null, null, null, null, null, null, null, 0, "id", true);
        Paginator<Pool> page1 = poolDao.filter(null, null, null, null, null, null, null, null, 1, "id", true);

        Assert.assertNotNull(page0);
        Assert.assertNotNull(page1);
        Assert.assertEquals(totalPools, page0.getTotalItems());
        Assert.assertEquals(totalPools, page1.getTotalItems());
        Assert.assertTrue(page0.getList().size() <= Paginator.DEFAULT_PAGE_SIZE);
    }

}
