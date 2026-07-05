package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.persistence.config.TestConfig;
import ar.edu.itba.paw.persistence.dao.DocumentDaoJpa;
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
import java.util.Arrays;

@Transactional
@Rollback
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class DocumentDaoJpaTest {

    private static final String TEST_DOCUMENT_NAME = "TestDocument.pdf";
    private static final String TEST_FILE_TYPE = "application/pdf";
    private static final byte[] TEST_BYTES = "TestDocumentContent".getBytes();

    private static final int EXISTING_DOCUMENT_ID = 1;
    private static final String EXISTING_DOCUMENT_NAME = "image1";
    private static final String EXISTING_FILE_TYPE = "image/png";

    private static final int EXISTING_DOCUMENT_ID_2 = 2;

    private static final int NON_EXISTING_DOCUMENT_ID = 9999;

    private static final String DOCUMENTS_TABLE = "documents";

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private DocumentDaoJpa documentDao;

    @Autowired
    private DataSource ds;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    // ==================== TESTS PARA CREATE ====================

    @Test
    public void testCreateDocument() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE);

        Document document = documentDao.create(TEST_DOCUMENT_NAME, TEST_FILE_TYPE, TEST_BYTES, true);
        em.flush();

        Assert.assertNotNull(document);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "name = '" + TEST_DOCUMENT_NAME + "' AND " +
                        "filetype = '" + TEST_FILE_TYPE + "' AND " +
                        "is_public = true"
        ));
    }

    @Test
    public void testCreatePrivateDocument() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE);

        Document document = documentDao.create("PrivateDoc.pdf", "application/pdf", TEST_BYTES, false);
        em.flush();

        Assert.assertNotNull(document);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "name = 'PrivateDoc.pdf' AND is_public = false"
        ));
    }

    @Test
    public void testCreateDocumentWithDifferentFileType() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE);
        byte[] imageBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};

        Document document = documentDao.create("image.png", "image/png", imageBytes, true);
        em.flush();

        Assert.assertNotNull(document);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "name = 'image.png' AND filetype = 'image/png'"
        ));
    }

    @Test
    public void testCreateDocumentWithLargeBytes() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE);
        byte[] largeBytes = new byte[1024];
        Arrays.fill(largeBytes, (byte) 0xFF);

        Document document = documentDao.create("large.bin", "application/octet-stream", largeBytes, true);
        em.flush();

        Assert.assertNotNull(document);
        Assert.assertEquals(initialRows + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "name = 'large.bin' AND filetype = 'application/octet-stream'"
        ));
    }

    @Test
    public void testCreateMultipleDocuments() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE);

        Document doc1 = documentDao.create("doc1.pdf", "application/pdf", "content1".getBytes(), true);
        Document doc2 = documentDao.create("doc2.pdf", "application/pdf", "content2".getBytes(), false);
        em.flush();

        Assert.assertNotNull(doc1);
        Assert.assertNotNull(doc2);
        Assert.assertEquals(initialRows + 2, JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "name = 'doc1.pdf'"
        ));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "name = 'doc2.pdf'"
        ));
    }

    // ==================== TESTS PARA FIND BY ID ====================

    @Test
    public void testFindByIdExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "id = " + EXISTING_DOCUMENT_ID
        );
        Assert.assertEquals(1, count);

        Document foundDocument = documentDao.findById(EXISTING_DOCUMENT_ID).orElse(null);

        Assert.assertNotNull(foundDocument);
        Assert.assertEquals(EXISTING_DOCUMENT_ID, foundDocument.getId());

        int verifyCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "id = " + EXISTING_DOCUMENT_ID + " AND name = '" + EXISTING_DOCUMENT_NAME + "'"
        );
        Assert.assertEquals(1, verifyCount);
    }

    @Test
    public void testFindByIdNotExists() {
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "id = " + NON_EXISTING_DOCUMENT_ID
        );
        Assert.assertEquals(0, count);

        Document foundDocument = documentDao.findById(NON_EXISTING_DOCUMENT_ID).orElse(null);

        Assert.assertNull(foundDocument);
    }

    @Test
    public void testFindByIdMultipleDocuments() {
        int totalCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE);
        Assert.assertTrue(totalCount >= 2);

        em.clear();

        Document doc1 = documentDao.findById(EXISTING_DOCUMENT_ID).orElse(null);
        Document doc2 = documentDao.findById(EXISTING_DOCUMENT_ID_2).orElse(null);

        Assert.assertNotNull(doc1);
        Assert.assertNotNull(doc2);
        Assert.assertEquals(EXISTING_DOCUMENT_ID, doc1.getId());
        Assert.assertEquals(EXISTING_DOCUMENT_ID_2, doc2.getId());
    }

    @Test
    public void testFindByIdReturnsCorrectData() {
        Document document = documentDao.findById(EXISTING_DOCUMENT_ID).orElse(null);

        Assert.assertNotNull(document);
        Assert.assertEquals(EXISTING_DOCUMENT_ID, document.getId());
        Assert.assertEquals(EXISTING_DOCUMENT_NAME, document.getName());
        Assert.assertEquals(EXISTING_FILE_TYPE, document.getFileType());
        Assert.assertTrue(document.isPublic());
    }

    // ==================== TESTS PARA DELETE ====================

    @Test
    public void testDeleteExistingDocument() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE);
        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "id = " + EXISTING_DOCUMENT_ID_2
        );
        Assert.assertEquals(1, initialCount);

        documentDao.delete(EXISTING_DOCUMENT_ID_2);
        em.flush();

        Assert.assertEquals(initialRows - 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE));
        int deletedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "id = " + EXISTING_DOCUMENT_ID_2
        );
        Assert.assertEquals(0, deletedCount);
    }

    @Test
    public void testDeleteNonExistingDocument() {
        int initialRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE);
        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "id = " + NON_EXISTING_DOCUMENT_ID
        );
        Assert.assertEquals(0, initialCount);

        documentDao.delete(NON_EXISTING_DOCUMENT_ID);
        em.flush();

        Assert.assertEquals(initialRows, JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE));
    }

    @Test
    public void testDeleteAndTryToFind() {
        Document docToDelete = documentDao.create("ToDeleteAndFind.pdf", "application/pdf", "test".getBytes(), true);
        em.flush();

        int createdDocId = docToDelete.getId();
        int initialCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "id = " + createdDocId
        );
        Assert.assertEquals(1, initialCount);

        documentDao.delete(createdDocId);
        em.flush();

        int deletedCount = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "id = " + createdDocId
        );
        Assert.assertEquals(0, deletedCount);

        Document notFoundDocument = documentDao.findById(createdDocId).orElse(null);
        Assert.assertNull(notFoundDocument);
    }

    @Test
    public void testDeleteMultipleDocuments() {
        Document doc1 = documentDao.create("ToDelete1.pdf", "application/pdf", "test1".getBytes(), true);
        Document doc2 = documentDao.create("ToDelete2.pdf", "application/pdf", "test2".getBytes(), true);
        em.flush();

        int afterCreateCount = JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE);

        documentDao.delete(doc1.getId());
        documentDao.delete(doc2.getId());
        em.flush();

        Assert.assertEquals(afterCreateCount - 2, JdbcTestUtils.countRowsInTable(jdbcTemplate, DOCUMENTS_TABLE));
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "name = 'ToDelete1.pdf'"
        ));
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "name = 'ToDelete2.pdf'"
        ));
    }

    // ==================== TESTS DE INTEGRIDAD ====================

    @Test
    public void testDocumentBytesAreStored() {
        byte[] testBytes = "SpecificContent123".getBytes();

        Document document = documentDao.create("ByteTest.txt", "text/plain", testBytes, true);
        em.flush();

        Assert.assertNotNull(document);
        int count = JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "name = 'ByteTest.txt' AND filetype = 'text/plain'"
        );
        Assert.assertEquals(1, count);
    }

    @Test
    public void testDocumentIsPublicFlag() {
        Document publicDoc = documentDao.create("PublicDoc.pdf", "application/pdf", TEST_BYTES, true);
        Document privateDoc = documentDao.create("PrivateDoc.pdf", "application/pdf", TEST_BYTES, false);
        em.flush();

        Assert.assertNotNull(publicDoc);
        Assert.assertNotNull(privateDoc);

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "name = 'PublicDoc.pdf' AND is_public = true"
        ));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                DOCUMENTS_TABLE,
                "name = 'PrivateDoc.pdf' AND is_public = false"
        ));
    }

}
