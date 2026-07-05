package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Report;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.persistence.config.TestConfig;
import ar.edu.itba.paw.persistence.dao.ReportDaoJpa;
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
import java.util.Date;

@Transactional
@Rollback
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class ReportDaoJpaTest {

    private static final int EXISTING_REPORT_ID = 1;
    private static final String EXISTING_REPORT_DESCRIPTION = "description1";


    private static final int EXISTING_USER_ID = 3;
    private static final int EXISTING_USER_ID_2 = 4;
    private static final int EXISTING_COMPANY_ID = 1;

    private static final int NON_EXISTING_REPORT_ID = 9999;

    private static final String REPORTS_TABLE = "reports";

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ReportDaoJpa reportDao;

    @Autowired
    private DataSource ds;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    // ==================== TESTS PARA CREATE ====================

    @Test
    public void testCreateReportUserReported() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REPORTS_TABLE);
        Company company = em.find(Company.class, EXISTING_COMPANY_ID);
        User user = em.find(User.class, EXISTING_USER_ID);
        Date now = new Date();

        Report report = reportDao.create("Test report description", now, true, company, user);
        em.flush();

        Assert.assertNotNull(report);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, REPORTS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "description = 'Test report description' AND user_reported = true"
        ));
    }

    @Test
    public void testCreateReportCompanyReported() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REPORTS_TABLE);
        Company company = em.find(Company.class, EXISTING_COMPANY_ID);
        User user = em.find(User.class, EXISTING_USER_ID);
        Date now = new Date();

        Report report = reportDao.create("Company report", now, false, company, user);
        em.flush();

        Assert.assertNotNull(report);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, REPORTS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "description = 'Company report' AND user_reported = false"
        ));
    }

    @Test
    public void testCreateReportWithLongDescription() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REPORTS_TABLE);
        Company company = em.find(Company.class, EXISTING_COMPANY_ID);
        User user = em.find(User.class, EXISTING_USER_ID);
        Date now = new Date();
        String longDescription = "A".repeat(500);

        Report report = reportDao.create(longDescription, now, true, company, user);
        em.flush();

        Assert.assertNotNull(report);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, REPORTS_TABLE));

        int createdId = report.getId();
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "id = " + createdId + " AND user_id = " + EXISTING_USER_ID + " AND company_id = " + EXISTING_COMPANY_ID
        ));
    }

    @Test
    public void testCreateMultipleReports() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REPORTS_TABLE);
        Company company = em.find(Company.class, EXISTING_COMPANY_ID);
        User user = em.find(User.class, EXISTING_USER_ID);
        Date now = new Date();

        Report report1 = reportDao.create("Report 1", now, true, company, user);
        Report report2 = reportDao.create("Report 2", now, false, company, user);
        em.flush();

        Assert.assertNotNull(report1);
        Assert.assertNotNull(report2);
        Assert.assertEquals(initialRows + 2, JdbcTestUtils.countRowsInTable(jdbcTemplate, REPORTS_TABLE));
    }

    // ==================== TESTS PARA FIND BY ID ====================

    @Test
    public void testFindByIdExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "id = " + EXISTING_REPORT_ID
        );
        Assert.assertEquals(1, count);

        Report foundReport = reportDao.findById(EXISTING_REPORT_ID).orElse(null);

        Assert.assertNotNull(foundReport);
        Assert.assertEquals(EXISTING_REPORT_ID, foundReport.getId());

        int verifyCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "id = " + EXISTING_REPORT_ID + " AND description = '" + EXISTING_REPORT_DESCRIPTION + "'"
        );
        Assert.assertEquals(1, verifyCount);
    }

    @Test
    public void testFindByIdNotExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "id = " + NON_EXISTING_REPORT_ID
        );
        Assert.assertEquals(0, count);

        Report foundReport = reportDao.findById(NON_EXISTING_REPORT_ID).orElse(null);

        Assert.assertNull(foundReport);
    }

    @Test
    public void testFindByIdReturnsCorrectData() {
        Report report = reportDao.findById(EXISTING_REPORT_ID).orElse(null);

        Assert.assertNotNull(report);
        Assert.assertEquals(EXISTING_REPORT_ID, report.getId());
        Assert.assertEquals(EXISTING_REPORT_DESCRIPTION, report.getDescription());
        Assert.assertTrue(report.isUserReported());
    }

    // ==================== TESTS PARA FILTER ====================

    @Test
    public void testFilterByUserId() {
        int reportsForUser = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "user_id = " + EXISTING_USER_ID
        );
        Assert.assertTrue(reportsForUser > 0);

        Paginator<Report> result = reportDao.filter(EXISTING_USER_ID, 0);

        Assert.assertNotNull(result);
        Assert.assertEquals(reportsForUser, result.getTotalItems());
        Assert.assertTrue(result.getList().stream()
                .allMatch(r -> r.getUser().getId() == EXISTING_USER_ID));
    }

    @Test
    public void testFilterByDifferentUserId() {
        int reportsForUser2 = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "user_id = " + EXISTING_USER_ID_2
        );
        Assert.assertTrue(reportsForUser2 > 0);

        Paginator<Report> result = reportDao.filter(EXISTING_USER_ID_2, 0);

        Assert.assertNotNull(result);
        Assert.assertEquals(reportsForUser2, result.getTotalItems());
    }

    @Test
    public void testFilterByUserWithNoReports() {
        int userWithNoReports = 1;
        int reportsCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "user_id = " + userWithNoReports
        );
        Assert.assertEquals(0, reportsCount);

        Paginator<Report> result = reportDao.filter(userWithNoReports, 0);

        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getTotalItems());
        Assert.assertTrue(result.getList().isEmpty());
    }

    @Test
    public void testFilterReturnsOnlyUserReports() {
        Paginator<Report> result = reportDao.filter(EXISTING_USER_ID, 0);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.getTotalItems() > 0);

        for (Report report : result.getList()) {
            Assert.assertEquals(EXISTING_USER_ID, report.getUser().getId());
        }
    }

    @Test
    public void testFilterPagination() {
        Company company = em.find(Company.class, EXISTING_COMPANY_ID);
        User user = em.find(User.class, EXISTING_USER_ID);
        Date now = new Date();

        for (int i = 0; i < 15; i++) {
            reportDao.create("Pagination test report " + i, now, true, company, user);
        }
        em.flush();

        int totalReports = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "user_id = " + EXISTING_USER_ID
        );
        Assert.assertTrue(totalReports >= 15);

        Paginator<Report> page0 = reportDao.filter(EXISTING_USER_ID, 0);
        Paginator<Report> page1 = reportDao.filter(EXISTING_USER_ID, 1);

        Assert.assertNotNull(page0);
        Assert.assertNotNull(page1);
        Assert.assertEquals(totalReports, page0.getTotalItems());
        Assert.assertEquals(totalReports, page1.getTotalItems());
        Assert.assertTrue(page0.getList().size() <= Paginator.DEFAULT_PAGE_SIZE);
    }

    // ==================== TEST DE INTEGRIDAD ====================

    @Test
    public void testReportFieldsAreNotNull() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, REPORTS_TABLE);
        Company company = em.find(Company.class, EXISTING_COMPANY_ID);
        User user = em.find(User.class, EXISTING_USER_ID);
        Date now = new Date();

        Report report = reportDao.create("Non-null test", now, true, company, user);
        em.flush();

        Assert.assertNotNull(report);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, REPORTS_TABLE));

        int countNotNull = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "description = 'Non-null test' AND description IS NOT NULL AND created_at IS NOT NULL"
        );
        Assert.assertEquals(1, countNotNull);
    }

    @Test
    public void testReportUserReportedFlag() {
        Company company = em.find(Company.class, EXISTING_COMPANY_ID);
        User user = em.find(User.class, EXISTING_USER_ID);
        Date now = new Date();

        Report userReport = reportDao.create("User report test", now, true, company, user);
        Report companyReport = reportDao.create("Company report test", now, false, company, user);
        em.flush();

        Assert.assertNotNull(userReport);
        Assert.assertNotNull(companyReport);

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "description = 'User report test' AND user_reported = true"
        ));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                REPORTS_TABLE,
                "description = 'Company report test' AND user_reported = false"
        ));
    }

}
