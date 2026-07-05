package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.DocumentDao;
import ar.edu.itba.paw.models.db.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceImplTest {

    private static final int ID = 1;
    private static final String NAME = "test.pdf";
    private static final String FILE_TYPE = "application/pdf";
    private static final byte[] BYTES = new byte[]{1, 2, 3, 4, 5};
    private static final boolean IS_PUBLIC = true;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Mock
    private DocumentDao documentDao;

    // tests para create
    @Test
    public void testCreatePublicDocument() {
        Document document = mock(Document.class);

        when(documentDao.create(NAME, FILE_TYPE, BYTES, true)).thenReturn(document);

        Document result = documentService.create(NAME, FILE_TYPE, BYTES, true);

        assertNotNull(result);
        assertEquals(document, result);
        verify(documentDao).create(eq(NAME), eq(FILE_TYPE), eq(BYTES), eq(true));
    }

    @Test
    public void testCreatePrivateDocument() {
        Document document = mock(Document.class);

        when(documentDao.create(NAME, FILE_TYPE, BYTES, false)).thenReturn(document);

        Document result = documentService.create(NAME, FILE_TYPE, BYTES, false);

        assertNotNull(result);
        assertEquals(document, result);
        verify(documentDao).create(eq(NAME), eq(FILE_TYPE), eq(BYTES), eq(false));
    }

    @Test
    public void testCreateWithEmptyBytes() {
        byte[] emptyBytes = new byte[]{};
        Document document = mock(Document.class);

        when(documentDao.create(NAME, FILE_TYPE, emptyBytes, IS_PUBLIC)).thenReturn(document);

        Document result = documentService.create(NAME, FILE_TYPE, emptyBytes, IS_PUBLIC);

        assertNotNull(result);
        assertEquals(document, result);
        verify(documentDao).create(eq(NAME), eq(FILE_TYPE), eq(emptyBytes), eq(IS_PUBLIC));
    }

    // tests para delete
    @Test
    public void testDelete() {
        documentService.delete(ID);

        verify(documentDao).delete(eq(ID));
    }

    // tests para findById
    @Test
    public void testFindByIdExists() {
        Document document = mock(Document.class);

        when(documentDao.findById(ID)).thenReturn(Optional.of(document));

        Optional<Document> result = documentService.findById(ID);

        assertTrue(result.isPresent());
        assertEquals(document, result.get());
        verify(documentDao).findById(eq(ID));
    }

    @Test
    public void testFindByIdNotExists() {
        when(documentDao.findById(ID)).thenReturn(Optional.empty());

        Optional<Document> result = documentService.findById(ID);

        assertFalse(result.isPresent());
        verify(documentDao).findById(eq(ID));
    }

}
