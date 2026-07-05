package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.db.Category;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.persistence.config.TestConfig;
import ar.edu.itba.paw.persistence.dao.ProductDaoJpa;
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
public class ProductDaoJpaTest {

    private static final int EXISTING_PRODUCT_ID = 1;
    private static final String EXISTING_PRODUCT_NAME = "product1";

    private static final int EXISTING_PRODUCT_ID_2 = 3;

    private static final int EXISTING_COMPANY_ID = 1;
    private static final int EXISTING_CATEGORY_ID = 1;
    private static final int EXISTING_DOCUMENT_ID = 1;

    private static final int NON_EXISTING_PRODUCT_ID = 9999;

    private static final String PRODUCTS_TABLE = "products";

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ProductDaoJpa productDao;

    @Autowired
    private DataSource ds;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    // ==================== TESTS PARA CREATE ====================

    @Test
    public void testCreateProduct() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, PRODUCTS_TABLE);
        Document document = em.find(Document.class, EXISTING_DOCUMENT_ID);
        Company company = em.find(Company.class, EXISTING_COMPANY_ID);
        Category category = em.find(Category.class, EXISTING_CATEGORY_ID);

        Product product = productDao.create("TestProduct", "Test Description", 99.99, document, company, category);
        em.flush();

        Assert.assertNotNull(product);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, PRODUCTS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "name = 'TestProduct' AND price = 99.99 AND active = true"
        ));
    }

    @Test
    public void testCreateProductWithDifferentPrice() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, PRODUCTS_TABLE);
        Document document = em.find(Document.class, EXISTING_DOCUMENT_ID);
        Company company = em.find(Company.class, EXISTING_COMPANY_ID);
        Category category = em.find(Category.class, EXISTING_CATEGORY_ID);

        Product product = productDao.create("ExpensiveProduct", "Expensive item", 999.50, document, company, category);
        em.flush();

        Assert.assertNotNull(product);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, PRODUCTS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "name = 'ExpensiveProduct' AND price = 999.50"
        ));
    }

    @Test
    public void testCreateProductDefaultActive() {
        Document document = em.find(Document.class, EXISTING_DOCUMENT_ID);
        Company company = em.find(Company.class, EXISTING_COMPANY_ID);
        Category category = em.find(Category.class, EXISTING_CATEGORY_ID);

        Product product = productDao.create("ActiveProduct", "Should be active by default", 50.0, document, company, category);
        em.flush();

        Assert.assertNotNull(product);
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "name = 'ActiveProduct' AND active = true"
        ));
    }

    // ==================== TESTS PARA FIND BY ID ====================

    @Test
    public void testFindByIdExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "id = " + EXISTING_PRODUCT_ID
        );
        Assert.assertEquals(1, count);

        Product foundProduct = productDao.findById(EXISTING_PRODUCT_ID).orElse(null);

        Assert.assertNotNull(foundProduct);
        Assert.assertEquals(EXISTING_PRODUCT_ID, foundProduct.getId());

        int verifyCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "id = " + EXISTING_PRODUCT_ID + " AND name = '" + EXISTING_PRODUCT_NAME + "'"
        );
        Assert.assertEquals(1, verifyCount);
    }

    @Test
    public void testFindByIdNotExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "id = " + NON_EXISTING_PRODUCT_ID
        );
        Assert.assertEquals(0, count);

        Product foundProduct = productDao.findById(NON_EXISTING_PRODUCT_ID).orElse(null);

        Assert.assertNull(foundProduct);
    }

    // ==================== TESTS PARA EDIT ====================

    @Test
    public void testEditProduct() {
        String newName = "UpdatedProduct";
        String newDescription = "Updated description";
        double newPrice = 199.99;
        Document newDocument = em.find(Document.class, 2);
        Category newCategory = em.find(Category.class, 2);

        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "id = " + EXISTING_PRODUCT_ID + " AND name = '" + EXISTING_PRODUCT_NAME + "'"
        );
        Assert.assertEquals(1, initialCount);

        productDao.edit(EXISTING_PRODUCT_ID, newName, newDescription, newPrice, newDocument, newCategory);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "id = " + EXISTING_PRODUCT_ID + " AND name = '" + newName + "' AND price = " + newPrice + " AND category_id = 2"
        );
        Assert.assertEquals(1, updatedCount);

        int oldCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "id = " + EXISTING_PRODUCT_ID + " AND name = '" + EXISTING_PRODUCT_NAME + "'"
        );
        Assert.assertEquals(0, oldCount);
    }

    // ==================== TESTS PARA RETIRE ====================

    @Test
    public void testRetireProduct() {
        int initialActive = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "id = " + EXISTING_PRODUCT_ID + " AND active = true"
        );
        Assert.assertEquals(1, initialActive);

        productDao.retire(EXISTING_PRODUCT_ID);
        em.flush();

        int activeCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "id = " + EXISTING_PRODUCT_ID + " AND active = true"
        );
        Assert.assertEquals(0, activeCount);

        int inactiveCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "id = " + EXISTING_PRODUCT_ID + " AND active = false"
        );
        Assert.assertEquals(1, inactiveCount);
    }

    @Test
    public void testRetireDoesNotRemoveFromDatabase() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, PRODUCTS_TABLE);

        productDao.retire(EXISTING_PRODUCT_ID);
        em.flush();

        int finalRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, PRODUCTS_TABLE);
        Assert.assertEquals(initialRows, finalRows);
    }

    // ==================== TESTS PARA FILTER ====================

    @Test
    public void testFilterNoParams() {
        int activeProducts = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "active = true"
        );

        Paginator<Product> result = productDao.filter(null, null, null, null, null, true, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(activeProducts, result.getTotalItems());
    }

    @Test
    public void testFilterByCompanyId() {
        int productsForCompany = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "company_id = " + EXISTING_COMPANY_ID + " AND active = true"
        );
        Assert.assertTrue(productsForCompany > 0);

        Paginator<Product> result = productDao.filter(null, null, EXISTING_COMPANY_ID, null, null, true, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(productsForCompany, result.getTotalItems());
    }

    @Test
    public void testFilterByCategoryId() {
        int productsForCategory = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "category_id = " + EXISTING_CATEGORY_ID + " AND active = true"
        );
        Assert.assertTrue(productsForCategory > 0);

        Paginator<Product> result = productDao.filter(null, EXISTING_CATEGORY_ID, null, null, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(productsForCategory, result.getTotalItems());
    }

    @Test
    public void testFilterByPriceRange() {
        int productsInRange = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "price >= 50.0 AND price <= 250.0 AND active = true"
        );

        Paginator<Product> result = productDao.filter(null, null, null, 50.0, 250.0, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(productsInRange, result.getTotalItems());
    }

    @Test
    public void testFilterByMinPrice() {
        int productsAboveMin = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "price >= 150.0 AND active = true"
        );

        Paginator<Product> result = productDao.filter(null, null, null, 150.0, null, true, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(productsAboveMin, result.getTotalItems());
    }

    @Test
    public void testFilterByMaxPrice() {
        int productsBelowMax = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "price <= 250.0 AND active = true"
        );

        Paginator<Product> result = productDao.filter(null, null, null, null, 250.0, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(productsBelowMax, result.getTotalItems());
    }

    @Test
    public void testFilterBySearch() {
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "name = '" + EXISTING_PRODUCT_NAME + "' AND active = true"
        ));

        Paginator<Product> result = productDao.filter(EXISTING_PRODUCT_NAME, null, null, null, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.getTotalItems() >= 1);
    }

    @Test
    public void testFilterCombinedParams() {
        int expectedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "company_id = " + EXISTING_COMPANY_ID + " AND category_id = " + EXISTING_CATEGORY_ID + " AND active = true"
        );

        Paginator<Product> result = productDao.filter(null, EXISTING_CATEGORY_ID, EXISTING_COMPANY_ID, null, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertEquals(expectedCount, result.getTotalItems());
    }

    @Test
    public void testFilterExcludesInactiveProducts() {
        int inactiveProduct = EXISTING_PRODUCT_ID_2;
        int inactiveCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                PRODUCTS_TABLE,
                "id = " + inactiveProduct + " AND active = false"
        );
        Assert.assertEquals(1, inactiveCount);

        Paginator<Product> result = productDao.filter(null, null, null, null, null, true, 0, "id", true);

        Assert.assertNotNull(result);
        boolean containsInactive = result.getList().stream()
                .anyMatch(p -> p.getId() == inactiveProduct);
        Assert.assertFalse("No debe contener productos inactivos", containsInactive);
    }

    @Test
    public void testFilterOrderByIdDesc() {
        Paginator<Product> result = productDao.filter(null, null, null, null, null, null, 0, "id", true);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.getList().isEmpty());

        for (int i = 0; i < result.getList().size() - 1; i++) {
            Assert.assertTrue(result.getList().get(i).getId() >= result.getList().get(i + 1).getId());
        }
    }

    @Test
    public void testFilterOrderByIdAsc() {
        Paginator<Product> result = productDao.filter(null, null, null, null, null, null, 0, "id", false);

        Assert.assertNotNull(result);
        Assert.assertFalse("Debe haber al menos un producto", result.getList().isEmpty());

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
    public void testFilterOrderByNameAsc() {
        Paginator<Product> result = productDao.filter(null, null, null, null, null, null, 0, "name", false);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.getList().isEmpty());

        for (int i = 0; i < result.getList().size() - 1; i++) {
            Assert.assertTrue(result.getList().get(i).getName().compareTo(result.getList().get(i + 1).getName()) <= 0);
        }
    }

    @Test
    public void testFilterOrderByPriceDesc() {
        Paginator<Product> result = productDao.filter(null, null, null, null, null, null, 0, "price", true);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.getList().isEmpty());

        for (int i = 0; i < result.getList().size() - 1; i++) {
            Assert.assertTrue(result.getList().get(i).getPrice() >= result.getList().get(i + 1).getPrice());
        }
    }

    @Test
    public void testFilterPagination() {
        Document document = em.find(Document.class, EXISTING_DOCUMENT_ID);
        Company company = em.find(Company.class, EXISTING_COMPANY_ID);
        Category category = em.find(Category.class, EXISTING_CATEGORY_ID);

        for (int i = 0; i < 15; i++) {
            productDao.create("PaginationTest" + i, "Description" + i, 100.0 + i, document, company, category);
        }
        em.flush();

        int totalProducts = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, PRODUCTS_TABLE, "active = true");
        Assert.assertTrue(totalProducts >= 15);

        Paginator<Product> page0 = productDao.filter(null, null, null, null, null, true, 0, "id", true);
        Paginator<Product> page1 = productDao.filter(null, null, null, null, null, true, 1, "id", true);

        Assert.assertNotNull(page0);
        Assert.assertNotNull(page1);
        Assert.assertEquals(totalProducts, page0.getTotalItems());
        Assert.assertEquals(totalProducts, page1.getTotalItems());
        Assert.assertTrue(page0.getList().size() <= Paginator.DEFAULT_PAGE_SIZE);
    }

}
