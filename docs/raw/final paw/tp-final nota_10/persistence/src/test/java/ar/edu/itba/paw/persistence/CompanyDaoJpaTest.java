package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.persistence.config.TestConfig;
import ar.edu.itba.paw.persistence.dao.CompanyDaoJpa;
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
public class CompanyDaoJpaTest {

    private static final String TEST_COMPANY_NAME = "TestCompany";
    private static final String TEST_ADDRESS = "TestAddress 123";
    private static final String TEST_EMAIL = "testcompany@test.com";
    private static final String TEST_PHONE = "1234567890";
    private static final String TEST_CBU = "1234567890123456789012";

    private static final int EXISTING_COMPANY_ID = 1;
    private static final String EXISTING_COMPANY_NAME = "company1";
    private static final String EXISTING_COMPANY_EMAIL = "companyemail1";
    private static final String EXISTING_COMPANY_ADDRESS = "address1";
    private static final int EXISTING_COMPANY_OWNER_ID = 1;

    private static final int EXISTING_COMPANY_ID_2 = 2;

    private static final int EXISTING_USER_ID = 1;
    private static final int EXISTING_DOCUMENT_ID = 1;

    private static final int NON_EXISTING_COMPANY_ID = 9999;

    private static final String COMPANIES_TABLE = "companies";

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private CompanyDaoJpa companyDao;

    @Autowired
    private DataSource ds;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    // ==================== TESTS PARA CREATE ====================

    @Test
    public void testCreateCompany() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, COMPANIES_TABLE);
        User owner = em.find(User.class, EXISTING_USER_ID);
        Document document = em.find(Document.class, EXISTING_DOCUMENT_ID);

        Company company = companyDao.create(
                TEST_COMPANY_NAME,
                TEST_ADDRESS,
                TEST_EMAIL,
                TEST_PHONE,
                false,
                TEST_CBU,
                document,
                owner
        );
        em.flush();

        Assert.assertNotNull(company);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, COMPANIES_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "name = '" + TEST_COMPANY_NAME + "' AND " +
                        "email = '" + TEST_EMAIL + "' AND " +
                        "validated = false"
        ));
    }

    @Test
    public void testCreateValidatedCompany() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, COMPANIES_TABLE);
        User owner = em.find(User.class, EXISTING_USER_ID);
        Document document = em.find(Document.class, EXISTING_DOCUMENT_ID);

        Company company = companyDao.create(
                "ValidatedCompany",
                TEST_ADDRESS,
                "validated@test.com",
                TEST_PHONE,
                true,
                TEST_CBU,
                document,
                owner
        );
        em.flush();

        Assert.assertNotNull(company);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, COMPANIES_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "name = 'ValidatedCompany' AND validated = true"
        ));
    }

    @Test
    public void testCreateCompanyWithAllFields() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, COMPANIES_TABLE);
        User owner = em.find(User.class, EXISTING_USER_ID);
        Document document = em.find(Document.class, EXISTING_DOCUMENT_ID);

        Company company = companyDao.create(
                "CompleteCompany",
                "Complete Address 456",
                "complete@test.com",
                "9876543210",
                true,
                "9876543210987654321098",
                document,
                owner
        );
        em.flush();

        Assert.assertNotNull(company);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, COMPANIES_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "name = 'CompleteCompany' AND " +
                        "address = 'Complete Address 456' AND " +
                        "email = 'complete@test.com' AND " +
                        "phone = '9876543210' AND " +
                        "cbu = '9876543210987654321098' AND " +
                        "owner_id = " + EXISTING_USER_ID + " AND " +
                        "document_id = " + EXISTING_DOCUMENT_ID
        ));
    }

    // ==================== TESTS PARA FIND BY ID ====================

    @Test
    public void testFindByIdExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "id = " + EXISTING_COMPANY_ID
        );
        Assert.assertEquals(1, count);

        Company foundCompany = companyDao.findById(EXISTING_COMPANY_ID).orElse(null);

        Assert.assertNotNull(foundCompany);
        Assert.assertEquals(EXISTING_COMPANY_ID, foundCompany.getId());

        int verifyCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "id = " + EXISTING_COMPANY_ID + " AND " +
                        "name = '" + EXISTING_COMPANY_NAME + "' AND " +
                        "email = '" + EXISTING_COMPANY_EMAIL + "'"
        );
        Assert.assertEquals(1, verifyCount);
    }

    @Test
    public void testFindByIdNotExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "id = " + NON_EXISTING_COMPANY_ID
        );
        Assert.assertEquals(0, count);

        Company foundCompany = companyDao.findById(NON_EXISTING_COMPANY_ID).orElse(null);

        Assert.assertNull(foundCompany);
    }

    // ==================== TESTS PARA EDIT ====================

    @Test
    public void testEditCompany() {
        String newAddress = "New Address 789";
        String newEmail = "newemail@test.com";
        String newPhone = "1111111111";
        String newCbu = "1111111111111111111111";
        Document newDocument = em.find(Document.class, 2);

        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "id = " + EXISTING_COMPANY_ID + " AND address = '" + EXISTING_COMPANY_ADDRESS + "'"
        );
        Assert.assertEquals(1, initialCount);

        companyDao.edit(EXISTING_COMPANY_ID, newAddress, newEmail, newPhone, true, newCbu, newDocument);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "id = " + EXISTING_COMPANY_ID + " AND " +
                        "address = '" + newAddress + "' AND " +
                        "email = '" + newEmail + "' AND " +
                        "phone = '" + newPhone + "' AND " +
                        "cbu = '" + newCbu + "' AND " +
                        "validated = true AND " +
                        "document_id = 2"
        );
        Assert.assertEquals(1, updatedCount);

        int oldCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "id = " + EXISTING_COMPANY_ID + " AND address = '" + EXISTING_COMPANY_ADDRESS + "'"
        );
        Assert.assertEquals(0, oldCount);
    }

    @Test
    public void testEditCompanyValidationStatus() {
        int initialValidatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "id = " + EXISTING_COMPANY_ID_2 + " AND validated = false"
        );
        Assert.assertEquals(1, initialValidatedCount);

        Company company = em.find(Company.class, EXISTING_COMPANY_ID_2);
        companyDao.edit(
                EXISTING_COMPANY_ID_2,
                company.getAddress(),
                company.getEmail(),
                company.getPhone(),
                true,
                company.getCbu(),
                company.getImage()
        );
        em.flush();

        int validatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "id = " + EXISTING_COMPANY_ID_2 + " AND validated = true"
        );
        Assert.assertEquals(1, validatedCount);
    }

    // ==================== TESTS PARA FILTER ====================

    @Test
    public void testFilterNoParams() {
        int totalCompanies = JdbcTestUtils.countRowsInTable(jdbcTemplate, COMPANIES_TABLE);

        Paginator<Company> result = companyDao.filter(null, null, null, 0);

        Assert.assertNotNull(result);
        Assert.assertEquals(totalCompanies, result.getTotalItems());
        Assert.assertEquals(totalCompanies, result.getList().size());
    }

    @Test
    public void testFilterBySearch() {
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "name = '" + EXISTING_COMPANY_NAME + "'"
        ));

        Paginator<Company> result = companyDao.filter(EXISTING_COMPANY_NAME, null, null, 0);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.getTotalItems() >= 1);
        Assert.assertTrue(result.getList().stream()
                .anyMatch(c -> c.getName().equals(EXISTING_COMPANY_NAME)));
    }

    @Test
    public void testFilterByOwnerId() {
        int companiesForOwner = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "owner_id = " + EXISTING_COMPANY_OWNER_ID
        );
        Assert.assertTrue(companiesForOwner > 0);

        Paginator<Company> result = companyDao.filter(null, EXISTING_COMPANY_OWNER_ID, null, 0);

        Assert.assertNotNull(result);
        Assert.assertEquals(companiesForOwner, result.getTotalItems());
        Assert.assertTrue(result.getList().stream()
                .allMatch(c -> c.getOwner().getId() == EXISTING_COMPANY_OWNER_ID));
    }

    @Test
    public void testFilterByValidated() {
        int validatedCompanies = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "validated = true"
        );
        Assert.assertTrue(validatedCompanies > 0);

        Paginator<Company> result = companyDao.filter(null, null, true, 0);

        Assert.assertNotNull(result);
        Assert.assertEquals(validatedCompanies, result.getTotalItems());
    }

    @Test
    public void testFilterByNotValidated() {
        int notValidatedCompanies = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "validated = false"
        );
        Assert.assertTrue(notValidatedCompanies > 0);

        Paginator<Company> result = companyDao.filter(null, null, false, 0);

        Assert.assertNotNull(result);
        Assert.assertEquals(notValidatedCompanies, result.getTotalItems());
    }

    @Test
    public void testFilterCombinedParams() {
        int expectedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                COMPANIES_TABLE,
                "owner_id = " + EXISTING_COMPANY_OWNER_ID + " AND validated = true"
        );

        Paginator<Company> result = companyDao.filter(null, EXISTING_COMPANY_OWNER_ID, true, 0);

        Assert.assertNotNull(result);
        Assert.assertEquals(expectedCount, result.getTotalItems());
    }

    @Test
    public void testFilterPagination() {
        User owner = em.find(User.class, EXISTING_USER_ID);
        Document document = em.find(Document.class, EXISTING_DOCUMENT_ID);

        for (int i = 0; i < 15; i++) {
            companyDao.create(
                    "PaginationTest" + i,
                    "Address" + i,
                    "email" + i + "@test.com",
                    "1234567890",
                    true,
                    String.format("12345678901234567890%02d", i),
                    document,
                    owner
            );
        }
        em.flush();

        int totalCompanies = JdbcTestUtils.countRowsInTable(jdbcTemplate, COMPANIES_TABLE);
        Assert.assertTrue(totalCompanies >= 15);

        Paginator<Company> page0 = companyDao.filter(null, null, null, 0);
        Paginator<Company> page1 = companyDao.filter(null, null, null, 1);

        Assert.assertNotNull(page0);
        Assert.assertNotNull(page1);
        Assert.assertEquals(totalCompanies, page0.getTotalItems());
        Assert.assertEquals(totalCompanies, page1.getTotalItems());
        Assert.assertTrue(page0.getList().size() <= Paginator.DEFAULT_PAGE_SIZE);
    }

    @Test
    public void testFilterOrdering() {
        Paginator<Company> result = companyDao.filter(null, null, null, 0);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.getList().isEmpty());

        for (int i = 0; i < result.getList().size() - 1; i++) {
            Company current = result.getList().get(i);
            Company next = result.getList().get(i + 1);

            int nameComparison = current.getName().compareTo(next.getName());
            if (nameComparison == 0) {
                int emailComparison = current.getEmail().compareTo(next.getEmail());
                if (emailComparison == 0) {
                    Assert.assertTrue(current.getId() >= next.getId());
                }
            }
        }
    }

}

