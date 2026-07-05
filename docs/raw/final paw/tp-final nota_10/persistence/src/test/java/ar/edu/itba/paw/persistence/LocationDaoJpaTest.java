package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.db.Location;
import ar.edu.itba.paw.persistence.config.TestConfig;
import ar.edu.itba.paw.persistence.dao.LocationDaoJpa;
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
public class LocationDaoJpaTest {

    private static final String TEST_LOCATION_NAME = "TestLocation";

    private static final int EXISTING_LOCATION_ID = 1;
    private static final String EXISTING_LOCATION_NAME = "location1";

    private static final int EXISTING_LOCATION_ID_2 = 2;
    private static final String EXISTING_LOCATION_NAME_2 = "location2";

    private static final int EXISTING_LOCATION_ID_3 = 3;

    private static final int NON_EXISTING_LOCATION_ID = 9999;

    private static final String LOCATIONS_TABLE = "locations";

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private LocationDaoJpa locationDao;

    @Autowired
    private DataSource ds;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    // ==================== TESTS PARA CREATE ====================

    @Test
    public void testCreateLocation() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, LOCATIONS_TABLE);

        Location location = locationDao.create(TEST_LOCATION_NAME);
        em.flush();

        Assert.assertNotNull(location);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, LOCATIONS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "name = '" + TEST_LOCATION_NAME + "'"
        ));
    }

    @Test
    public void testCreateLocationWithDifferentName() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, LOCATIONS_TABLE);
        String locationName = "Buenos Aires";

        Location location = locationDao.create(locationName);
        em.flush();

        Assert.assertNotNull(location);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, LOCATIONS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "name = '" + locationName + "'"
        ));
    }

    @Test
    public void testCreateMultipleLocations() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, LOCATIONS_TABLE);
        String location1Name = "Location1Test";
        String location2Name = "Location2Test";

        Location location1 = locationDao.create(location1Name);
        Location location2 = locationDao.create(location2Name);
        em.flush();

        Assert.assertNotNull(location1);
        Assert.assertNotNull(location2);
        Assert.assertEquals(initialRows + 2, JdbcTestUtils.countRowsInTable(jdbcTemplate, LOCATIONS_TABLE));

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "name = '" + location1Name + "'"
        ));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "name = '" + location2Name + "'"
        ));
    }

    // ==================== TESTS PARA FIND BY ID ====================

    @Test
    public void testFindByIdExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "id = " + EXISTING_LOCATION_ID
        );
        Assert.assertEquals(1, count);

        Location foundLocation = locationDao.findById(EXISTING_LOCATION_ID).orElse(null);

        Assert.assertNotNull(foundLocation);
        Assert.assertEquals(EXISTING_LOCATION_ID, foundLocation.getId());

        int verifyCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "id = " + EXISTING_LOCATION_ID + " AND name = '" + EXISTING_LOCATION_NAME + "'"
        );
        Assert.assertEquals(1, verifyCount);
    }

    @Test
    public void testFindByIdNotExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "id = " + NON_EXISTING_LOCATION_ID
        );
        Assert.assertEquals(0, count);

        Location foundLocation = locationDao.findById(NON_EXISTING_LOCATION_ID).orElse(null);

        Assert.assertNull(foundLocation);
    }

    @Test
    public void testFindByIdMultipleExistingLocations() {
        int totalCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, LOCATIONS_TABLE);
        Assert.assertTrue(totalCount >= 3);

        Location location1 = locationDao.findById(EXISTING_LOCATION_ID).orElse(null);
        Location location2 = locationDao.findById(EXISTING_LOCATION_ID_2).orElse(null);
        Location location3 = locationDao.findById(EXISTING_LOCATION_ID_3).orElse(null);

        Assert.assertNotNull(location1);
        Assert.assertNotNull(location2);
        Assert.assertNotNull(location3);

        Assert.assertEquals(EXISTING_LOCATION_ID, location1.getId());
        Assert.assertEquals(EXISTING_LOCATION_ID_2, location2.getId());
        Assert.assertEquals(EXISTING_LOCATION_ID_3, location3.getId());
    }

    // ==================== TESTS PARA GET ALL ====================

    @Test
    public void testGetAllLocations() {
        int expectedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "name != 'Desconocido'"
        );
        Assert.assertTrue(expectedCount >= 3);

        List<Location> locations = locationDao.getAll();

        Assert.assertNotNull(locations);
        Assert.assertEquals(expectedCount, locations.size());

        Assert.assertEquals(
                JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, LOCATIONS_TABLE, "name != 'Desconocido'"),
                locations.size()
        );
    }

    @Test
    public void testGetAllLocationsOrderedByName() {
        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "name != 'Desconocido'"
        );

        List<Location> locations = locationDao.getAll();

        Assert.assertNotNull(locations);
        Assert.assertEquals(initialCount, locations.size());

        for (int i = 0; i < locations.size() - 1; i++) {
            Assert.assertTrue(
                    "Las ubicaciones deben estar ordenadas por nombre",
                    locations.get(i).getName().compareTo(locations.get(i + 1).getName()) <= 0
            );
        }
    }

    @Test
    public void testGetAllAfterCreatingNewLocation() {
        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "name != 'Desconocido'"
        );
        List<Location> locationsBefore = locationDao.getAll();
        Assert.assertEquals(initialCount, locationsBefore.size());

        locationDao.create("NewLocationForGetAll");
        em.flush();

        int newCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "name != 'Desconocido'"
        );
        Assert.assertEquals(initialCount + 1, newCount);

        List<Location> locationsAfter = locationDao.getAll();
        Assert.assertEquals(newCount, locationsAfter.size());
        Assert.assertEquals(initialCount + 1, locationsAfter.size());
    }

    @Test
    public void testGetAllReturnsAllExistingLocations() {
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "name = '" + EXISTING_LOCATION_NAME + "'"
        ));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "name = '" + EXISTING_LOCATION_NAME_2 + "'"
        ));

        List<Location> locations = locationDao.getAll();

        Assert.assertNotNull(locations);
        Assert.assertTrue(locations.size() >= 2);

        boolean containsLocation1 = locations.stream()
                .anyMatch(l -> l.getName().equals(EXISTING_LOCATION_NAME));
        boolean containsLocation2 = locations.stream()
                .anyMatch(l -> l.getName().equals(EXISTING_LOCATION_NAME_2));

        Assert.assertTrue("Debe contener " + EXISTING_LOCATION_NAME, containsLocation1);
        Assert.assertTrue("Debe contener " + EXISTING_LOCATION_NAME_2, containsLocation2);
    }

    @Test
    public void testGetAllExcludesDesconocido() {
        locationDao.create("Desconocido");
        em.flush();

        int desconocidoCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "name = 'Desconocido'"
        );
        Assert.assertTrue(desconocidoCount >= 1);

        List<Location> locations = locationDao.getAll();

        boolean containsDesconocido = locations.stream()
                .anyMatch(l -> l.getName().equals("Desconocido"));

        Assert.assertFalse("No debe contener 'Desconocido'", containsDesconocido);
    }

    // ==================== TEST DE INTEGRIDAD ====================

    @Test
    public void testLocationNameIsNotNull() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, LOCATIONS_TABLE);

        Location location = locationDao.create("NotNullLocation");
        em.flush();

        Assert.assertNotNull(location);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, LOCATIONS_TABLE));

        int countNotNull = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                LOCATIONS_TABLE,
                "name = 'NotNullLocation' AND name IS NOT NULL"
        );
        Assert.assertEquals(1, countNotNull);
    }

}
