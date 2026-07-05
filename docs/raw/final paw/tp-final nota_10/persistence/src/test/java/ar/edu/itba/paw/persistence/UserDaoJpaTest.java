package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.db.Location;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.persistence.config.TestConfig;
import ar.edu.itba.paw.persistence.dao.UserDaoJpa;
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
public class UserDaoJpaTest {

    private static final String TEST_EMAIL = "test@itba.edu.ar";
    private static final String TEST_PASSWORD = "testPassword123";
    private static final String TEST_FIRST_NAME = "TestFirstName";
    private static final String TEST_LAST_NAME = "TestLastName";
    private static final String TEST_VALIDATION_TOKEN = "testValidationToken123";
    private static final String TEST_PREFERRED_LANGUAGE = "en";
    private static final String TEST_PREFERRED_LANGUAGE_ES = "es";

    private static final int EXISTING_USER_ID = 1;
    private static final String EXISTING_USER_EMAIL = "email1";
    private static final String EXISTING_USER_PASSWORD = "password1";
    private static final String EXISTING_USER_FIRST_NAME = "user1";
    private static final int EXISTING_LOCATION_ID = 1;

    private static final int NON_EXISTING_USER_ID = 9999;
    private static final String NON_EXISTING_EMAIL = "nonexisting@test.com";

    private static final String USERS_TABLE = "users";

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private UserDaoJpa userDao;

    @Autowired
    private DataSource ds;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    // ==================== TESTS PARA CREATE ====================

    @Test
    public void testCreateUser() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, USERS_TABLE);
        Location location = em.find(Location.class, EXISTING_LOCATION_ID);

        User user = userDao.create(
                TEST_EMAIL,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                location,
                false,
                false,
                TEST_VALIDATION_TOKEN,
                null,
                false,
                0,
                null,
                TEST_PREFERRED_LANGUAGE
        );
        em.flush();

        Assert.assertNotNull(user);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, USERS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "email = '" + TEST_EMAIL + "' AND " +
                        "password = '" + TEST_PASSWORD + "' AND " +
                        "first_name = '" + TEST_FIRST_NAME + "' AND " +
                        "last_name = '" + TEST_LAST_NAME + "' AND " +
                        "location_id = " + EXISTING_LOCATION_ID + " AND " +
                        "is_company = false AND " +
                        "validated = false AND " +
                        "validation_token = '" + TEST_VALIDATION_TOKEN + "' AND " +
                        "password_token IS NULL AND " +
                        "admin = false AND " +
                        "block_level = 0 AND " +
                        "blocked_until IS NULL AND " +
                        "preferred_language = '" + TEST_PREFERRED_LANGUAGE + "'"
        ));
    }

    @Test
    public void testCreateCompanyUser() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, USERS_TABLE);
        Location location = em.find(Location.class, EXISTING_LOCATION_ID);

        User user = userDao.create(
                TEST_EMAIL,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                location,
                true,
                false,
                TEST_VALIDATION_TOKEN,
                null,
                false,
                0,
                null,
                TEST_PREFERRED_LANGUAGE
        );
        em.flush();

        Assert.assertNotNull(user);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, USERS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "email = '" + TEST_EMAIL + "' AND is_company = true"
        ));
    }

    @Test
    public void testCreateValidatedUser() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, USERS_TABLE);
        Location location = em.find(Location.class, EXISTING_LOCATION_ID);

        User user = userDao.create(
                TEST_EMAIL,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                location,
                false,
                true,
                null,
                null,
                false,
                0,
                null,
                TEST_PREFERRED_LANGUAGE
        );
        em.flush();

        Assert.assertNotNull(user);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, USERS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "email = '" + TEST_EMAIL + "' AND validated = true AND validation_token IS NULL"
        ));
    }

    @Test
    public void testCreateAdminUser() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, USERS_TABLE);
        Location location = em.find(Location.class, EXISTING_LOCATION_ID);

        User user = userDao.create(
                TEST_EMAIL,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                location,
                false,
                true,
                null,
                null,
                true,
                0,
                null,
                TEST_PREFERRED_LANGUAGE
        );
        em.flush();

        Assert.assertNotNull(user);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, USERS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "email = '" + TEST_EMAIL + "' AND admin = true"
        ));
    }

    @Test
    public void testCreateUserWithBlockLevel() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, USERS_TABLE);
        Location location = em.find(Location.class, EXISTING_LOCATION_ID);
        Date blockedUntil = new Date(System.currentTimeMillis() + 86400000); // +1 día

        User user = userDao.create(
                TEST_EMAIL,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                location,
                false,
                true,
                null,
                null,
                false,
                2,
                blockedUntil,
                TEST_PREFERRED_LANGUAGE
        );
        em.flush();

        Assert.assertNotNull(user);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, USERS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "email = '" + TEST_EMAIL + "' AND block_level = 2 AND blocked_until IS NOT NULL"
        ));
    }

    // ==================== TESTS PARA FIND BY ID ====================

    @Test
    public void testFindByIdExists() {
        User foundUser = userDao.findById(EXISTING_USER_ID).orElse(null);

        Assert.assertNotNull(foundUser);
        Assert.assertEquals(EXISTING_USER_ID, foundUser.getId());

        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + EXISTING_USER_ID + " AND " +
                        "email = '" + EXISTING_USER_EMAIL + "' AND " +
                        "first_name = '" + EXISTING_USER_FIRST_NAME + "'"
        );
        Assert.assertEquals(1, count);
    }

    @Test
    public void testFindByIdNotExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + NON_EXISTING_USER_ID
        );
        Assert.assertEquals(0, count);

        User foundUser = userDao.findById(NON_EXISTING_USER_ID).orElse(null);

        Assert.assertNull(foundUser);
    }

    // ==================== TESTS PARA FIND BY EMAIL ====================

    @Test
    public void testFindByEmailExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "email = '" + EXISTING_USER_EMAIL + "'"
        );
        Assert.assertEquals(1, count);

        User foundUser = userDao.findByEmail(EXISTING_USER_EMAIL).orElse(null);

        Assert.assertNotNull(foundUser);
        Assert.assertEquals(EXISTING_USER_EMAIL, foundUser.getEmail());
        Assert.assertEquals(EXISTING_USER_ID, foundUser.getId());
    }

    @Test
    public void testFindByEmailNotExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "email = '" + NON_EXISTING_EMAIL + "'"
        );
        Assert.assertEquals(0, count);

        User foundUser = userDao.findByEmail(NON_EXISTING_EMAIL).orElse(null);

        Assert.assertNull(foundUser);
    }

    // ==================== TESTS PARA EDIT ====================

    @Test
    public void testEditUser() {
        String newFirstName = "NewFirstName";
        String newLastName = "NewLastName";
        Location newLocation = em.find(Location.class, 2);
        int newBlockLevel = 1;
        Date newBlockedUntil = new Date(System.currentTimeMillis() + 86400000);

        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + EXISTING_USER_ID + " AND first_name = '" + EXISTING_USER_FIRST_NAME + "'"
        );
        Assert.assertEquals(1, initialCount);

        userDao.edit(EXISTING_USER_ID, newFirstName, newLastName, newLocation, newBlockLevel, newBlockedUntil);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + EXISTING_USER_ID + " AND " +
                        "first_name = '" + newFirstName + "' AND " +
                        "last_name = '" + newLastName + "' AND " +
                        "location_id = 2 AND " +
                        "block_level = 1 AND " +
                        "blocked_until IS NOT NULL"
        );
        Assert.assertEquals(1, updatedCount);

        int oldCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + EXISTING_USER_ID + " AND first_name = '" + EXISTING_USER_FIRST_NAME + "'"
        );
        Assert.assertEquals(0, oldCount);
    }

    // ==================== TESTS PARA UPDATE PASSWORD TOKEN ====================

    @Test
    public void testUpdatePasswordToken() {
        String newToken = "newPasswordToken123";

        int initialNullTokenCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + EXISTING_USER_ID + " AND password_token IS NULL"
        );
        Assert.assertEquals(1, initialNullTokenCount);

        userDao.updatePasswordToken(EXISTING_USER_ID, newToken);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + EXISTING_USER_ID + " AND password_token = '" + newToken + "'"
        );
        Assert.assertEquals(1, updatedCount);
    }

    // ==================== TESTS PARA UPDATE PASSWORD ====================

    @Test
    public void testUpdatePassword() {
        String newPassword = "newPassword456";

        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "email = '" + EXISTING_USER_EMAIL + "' AND password = '" + EXISTING_USER_PASSWORD + "'"
        );
        Assert.assertEquals(1, initialCount);

        userDao.updatePassword(EXISTING_USER_EMAIL, newPassword);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "email = '" + EXISTING_USER_EMAIL + "' AND " +
                        "password = '" + newPassword + "' AND " +
                        "password_token IS NULL"
        );
        Assert.assertEquals(1, updatedCount);

        int oldPasswordCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "email = '" + EXISTING_USER_EMAIL + "' AND password = '" + EXISTING_USER_PASSWORD + "'"
        );
        Assert.assertEquals(0, oldPasswordCount);
    }

    // ==================== TESTS PARA UPDATE VALIDATION TOKEN ====================

    @Test
    public void testUpdateValidationToken() {
        String newValidationToken = "newValidationToken123";

        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + EXISTING_USER_ID + " AND validation_token = 'a'"
        );
        Assert.assertEquals(1, initialCount);

        userDao.updateValidationToken(EXISTING_USER_ID, newValidationToken);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + EXISTING_USER_ID + " AND validation_token = '" + newValidationToken + "'"
        );
        Assert.assertEquals(1, updatedCount);
    }

    // ==================== TESTS PARA UPDATE PREFERRED LANGUAGE ====================

    @Test
    public void testUpdatePreferredLanguage() {
        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + EXISTING_USER_ID + " AND preferred_language = '" + TEST_PREFERRED_LANGUAGE + "'"
        );
        Assert.assertEquals(1, initialCount);

        userDao.updatePreferredLanguage(EXISTING_USER_ID, TEST_PREFERRED_LANGUAGE_ES);
        em.flush();

        int updatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + EXISTING_USER_ID + " AND preferred_language = '" + TEST_PREFERRED_LANGUAGE_ES + "'"
        );
        Assert.assertEquals(1, updatedCount);
    }

    // ==================== TESTS PARA VALIDATE ACCOUNT ====================

    @Test
    public void testValidateAccount() {
        int unvalidatedUserId = 5;

        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + unvalidatedUserId + " AND validated = false AND validation_token IS NOT NULL"
        );
        Assert.assertEquals(1, initialCount);

        userDao.validateAccount(unvalidatedUserId);
        em.flush();

        int validatedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                USERS_TABLE,
                "id = " + unvalidatedUserId + " AND validated = true AND validation_token IS NULL"
        );
        Assert.assertEquals(1, validatedCount);
    }

}
