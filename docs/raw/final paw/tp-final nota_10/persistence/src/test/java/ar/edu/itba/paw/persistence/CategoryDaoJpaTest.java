package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.db.Category;
import ar.edu.itba.paw.persistence.config.TestConfig;
import ar.edu.itba.paw.persistence.dao.CategoryDaoJpa;
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
import java.util.List;

@Transactional
@Rollback
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class CategoryDaoJpaTest {

    private static final String TEST_CATEGORY_NAME = "TestCategory";
    private static final String TEST_ICON_NAME = "TestIcon";

    private static final int EXISTING_CATEGORY_ID = 1;
    private static final String EXISTING_CATEGORY_NAME = "category1";
    private static final String EXISTING_ICON_NAME = "icon1";

    private static final int EXISTING_CATEGORY_ID_2 = 2;
    private static final String EXISTING_CATEGORY_NAME_2 = "category2";

    private static final int EXISTING_CATEGORY_ID_3 = 3;

    private static final int NON_EXISTING_CATEGORY_ID = 9999;

    // Nombres de tablas
    private static final String CATEGORIES_TABLE = "categories";

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private CategoryDaoJpa categoryDao;

    @Autowired
    private DataSource ds;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    // ==================== TESTS PARA CREATE ====================

    @Test
    public void testCreateCategory() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE);

        Category category = categoryDao.create(TEST_CATEGORY_NAME, TEST_ICON_NAME);
        em.flush();

        Assert.assertNotNull(category);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                CATEGORIES_TABLE,
                "name = '" + TEST_CATEGORY_NAME + "' AND icon_name = '" + TEST_ICON_NAME + "'"
        ));
    }

    @Test
    public void testCreateCategoryWithDifferentIcon() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE);
        String categoryName = "AnotherCategory";
        String iconName = "AnotherIcon";

        Category category = categoryDao.create(categoryName, iconName);
        em.flush();

        Assert.assertNotNull(category);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                CATEGORIES_TABLE,
                "name = '" + categoryName + "' AND icon_name = '" + iconName + "'"
        ));
    }

    @Test
    public void testCreateMultipleCategories() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE);
        String category1Name = "Category1Test";
        String category2Name = "Category2Test";

        Category category1 = categoryDao.create(category1Name, "icon1test");
        Category category2 = categoryDao.create(category2Name, "icon2test");
        em.flush();

        Assert.assertNotNull(category1);
        Assert.assertNotNull(category2);
        Assert.assertEquals(initialRows + 2, JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE));

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                CATEGORIES_TABLE,
                "name = '" + category1Name + "'"
        ));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                CATEGORIES_TABLE,
                "name = '" + category2Name + "'"
        ));
    }

    // ==================== TESTS PARA FIND BY ID ====================

    @Test
    public void testFindByIdExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                CATEGORIES_TABLE,
                "id = " + EXISTING_CATEGORY_ID
        );
        Assert.assertEquals(1, count);

        Category foundCategory = categoryDao.findById(EXISTING_CATEGORY_ID).orElse(null);

        Assert.assertNotNull(foundCategory);
        Assert.assertEquals(EXISTING_CATEGORY_ID, foundCategory.getId());

        int verifyCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                CATEGORIES_TABLE,
                "id = " + EXISTING_CATEGORY_ID + " AND " +
                        "name = '" + EXISTING_CATEGORY_NAME + "' AND " +
                        "icon_name = '" + EXISTING_ICON_NAME + "'"
        );
        Assert.assertEquals(1, verifyCount);
    }

    @Test
    public void testFindByIdNotExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                CATEGORIES_TABLE,
                "id = " + NON_EXISTING_CATEGORY_ID
        );
        Assert.assertEquals(0, count);

        Category foundCategory = categoryDao.findById(NON_EXISTING_CATEGORY_ID).orElse(null);

        Assert.assertNull(foundCategory);
    }

    @Test
    public void testFindByIdMultipleExistingCategories() {
        int totalCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE);
        Assert.assertTrue(totalCount >= 3); // Debería haber al menos 3 según inserts.sql

        Category category1 = categoryDao.findById(EXISTING_CATEGORY_ID).orElse(null);
        Category category2 = categoryDao.findById(EXISTING_CATEGORY_ID_2).orElse(null);
        Category category3 = categoryDao.findById(EXISTING_CATEGORY_ID_3).orElse(null);

        Assert.assertNotNull(category1);
        Assert.assertNotNull(category2);
        Assert.assertNotNull(category3);

        Assert.assertEquals(EXISTING_CATEGORY_ID, category1.getId());
        Assert.assertEquals(EXISTING_CATEGORY_ID_2, category2.getId());
        Assert.assertEquals(EXISTING_CATEGORY_ID_3, category3.getId());
    }

    // ==================== TESTS PARA GET ALL ====================

    @Test
    public void testGetAllCategories() {
        int expectedCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE);
        Assert.assertTrue(expectedCount >= 3); // Según inserts.sql

        List<Category> categories = categoryDao.getAll();

        Assert.assertNotNull(categories);
        Assert.assertEquals(expectedCount, categories.size());

        Assert.assertEquals(
                JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE),
                categories.size()
        );
    }

    @Test
    public void testGetAllCategoriesOrderedByIdDesc() {
        int initialCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE);

        List<Category> categories = categoryDao.getAll();

        Assert.assertNotNull(categories);
        Assert.assertEquals(initialCount, categories.size());

        for (int i = 0; i < categories.size() - 1; i++) {
            Assert.assertTrue(
                    "Las categorías deben estar ordenadas por ID descendente",
                    categories.get(i).getId() >= categories.get(i + 1).getId()
            );
        }
    }

    @Test
    public void testGetAllAfterCreatingNewCategory() {
        int initialCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE);
        List<Category> categoriesBefore = categoryDao.getAll();
        Assert.assertEquals(initialCount, categoriesBefore.size());

        categoryDao.create("NewCategoryForGetAll", "newIcon");
        em.flush();

        int newCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE);
        Assert.assertEquals(initialCount + 1, newCount);

        List<Category> categoriesAfter = categoryDao.getAll();
        Assert.assertEquals(newCount, categoriesAfter.size());
        Assert.assertEquals(initialCount + 1, categoriesAfter.size());
    }

    @Test
    public void testGetAllReturnsAllExistingCategories() {
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                CATEGORIES_TABLE,
                "name = '" + EXISTING_CATEGORY_NAME + "'"
        ));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                CATEGORIES_TABLE,
                "name = '" + EXISTING_CATEGORY_NAME_2 + "'"
        ));

        List<Category> categories = categoryDao.getAll();

        Assert.assertNotNull(categories);
        Assert.assertTrue(categories.size() >= 2);

        boolean containsCategory1 = categories.stream()
                .anyMatch(c -> c.getName().equals(EXISTING_CATEGORY_NAME));
        boolean containsCategory2 = categories.stream()
                .anyMatch(c -> c.getName().equals(EXISTING_CATEGORY_NAME_2));

        Assert.assertTrue("Debe contener " + EXISTING_CATEGORY_NAME, containsCategory1);
        Assert.assertTrue("Debe contener " + EXISTING_CATEGORY_NAME_2, containsCategory2);
    }

    // ==================== TEST DE INTEGRIDAD ====================

    @Test
    public void testCategoryNameAndIconAreNotNull() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE);

        Category category = categoryDao.create("NotNullCategory", "notNullIcon");
        em.flush();

        Assert.assertNotNull(category);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, CATEGORIES_TABLE));

        int countNotNull = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                CATEGORIES_TABLE,
                "name = 'NotNullCategory' AND icon_name = 'notNullIcon' AND " +
                        "name IS NOT NULL AND icon_name IS NOT NULL"
        );
        Assert.assertEquals(1, countNotNull);
    }

}
