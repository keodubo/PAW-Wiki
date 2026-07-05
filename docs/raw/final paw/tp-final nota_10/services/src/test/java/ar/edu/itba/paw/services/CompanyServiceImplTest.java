package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.CompanyDao;
import ar.edu.itba.paw.interfaces.exception.CompanyNotFoundException;
import ar.edu.itba.paw.interfaces.exception.DocumentNotFoundException;
import ar.edu.itba.paw.interfaces.exception.InvalidDocumentVisibilityException;
import ar.edu.itba.paw.interfaces.service.DocumentService;
import ar.edu.itba.paw.interfaces.service.EmailService;
import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CompanyServiceImplTest {

    private static final int ID = 1;
    private static final String NAME = "Test Company";
    private static final String ADDRESS = "Av. Test 123";
    private static final String EMAIL = "test@company.com";
    private static final String PHONE = "1234567890";
    private static final String CBU = "1234567890123456789012";

    private static final int IMAGE_ID_1 = 1;
    private static final String IMAGE_NAME_1 = "image1";
    private static final String IMAGE_FILETYPE_1 = "jpg";
    private static final String IMAGE_URI_1 = "/api/documents/" + IMAGE_ID_1;

    private static final int IMAGE_ID_2 = 2;
    private static final String IMAGE_NAME_2 = "image2";
    private static final String IMAGE_FILETYPE_2 = "jpg";
    private static final String IMAGE_URI_2 = "/api/documents/" + IMAGE_ID_2;

    @InjectMocks
    private CompanyServiceImpl companyService;

    @Mock
    private CompanyDao companyDao;

    @Mock
    private UserService userService;

    @Mock
    private DocumentService documentService;

    @Mock
    private EmailService emailService;

    // tests para create
    @Test
    public void testCreateCompanySuccessfully() {
        Document image = new Document(IMAGE_ID_1, IMAGE_NAME_1, IMAGE_FILETYPE_1, new byte[0], true);
        User owner = mock(User.class), admin = mock(User.class);
        Company company = new Company(ID, NAME, ADDRESS, EMAIL, PHONE, false, CBU, image, owner);

        when(documentService.findById(IMAGE_ID_1)).thenReturn(Optional.of(image));
        when(companyDao.create(any(), any(), any(), any(), anyBoolean(), any(), any(), any())).thenReturn(company);
        when(userService.findAdmin()).thenReturn(Optional.of(admin));

        Company result = companyService.create(NAME, ADDRESS, EMAIL, PHONE, CBU, IMAGE_URI_1, owner);

        assertNotNull(result);
        assertEquals(company, result);
        verify(documentService).findById(eq(IMAGE_ID_1));
        verify(companyDao).create(eq(NAME), eq(ADDRESS), eq(EMAIL), eq(PHONE), eq(false), eq(CBU), eq(image), eq(owner));
        verify(emailService).sendCompanyRegisterEmail(eq(owner), eq(company), eq(admin), any(Locale.class));
    }

    @Test(expected = DocumentNotFoundException.class)
    public void testCreateCompanyWithNonExistentDocumentThrows() {
        User owner = mock(User.class);

        when(documentService.findById(IMAGE_ID_2)).thenReturn(Optional.empty());

        companyService.create(NAME, ADDRESS, EMAIL, PHONE, CBU, IMAGE_URI_2, owner);
    }

    @Test(expected = InvalidDocumentVisibilityException.class)
    public void testCreateCompanyWithPrivateDocumentThrows() {
        Document image = new Document(IMAGE_ID_1, IMAGE_NAME_1, IMAGE_FILETYPE_1, new byte[0], false);
        User owner = mock(User.class);

        when(documentService.findById(IMAGE_ID_1)).thenReturn(Optional.of(image));

        companyService.create(NAME, ADDRESS, EMAIL, PHONE, CBU, IMAGE_URI_1, owner);
    }

    // tests para edit
    @Test
    public void testEditCompanySuccessfully() {
        Document image = mock(Document.class);
        User owner = mock(User.class);
        Company company = new Company(ID, NAME, ADDRESS, EMAIL, PHONE, false, CBU, image, owner);

        String newAddress = "Av. Test 456", newEmail = "newtest@company.com", newPhone = "0987654321", newCbu = "2109876543210987654321";
        Document newImage = new Document(IMAGE_ID_2, IMAGE_NAME_2, IMAGE_FILETYPE_2, new byte[0], true);
        boolean validated = true;

        when(companyDao.findById(ID)).thenReturn(Optional.of(company));
        when(documentService.findById(IMAGE_ID_2)).thenReturn(Optional.of(newImage));

        companyService.edit(ID, newAddress, newEmail, newPhone, newCbu, IMAGE_URI_2, validated);

        verify(companyDao).findById(eq(ID));
        verify(documentService).findById(eq(IMAGE_ID_2));
        verify(companyDao).edit(eq(ID), eq(newAddress), eq(newEmail), eq(newPhone), eq(validated), eq(newCbu), eq(newImage));
    }

    @Test
    public void testEditCompanyWithNullFields() {
        Document image = mock(Document.class);
        User owner = mock(User.class);
        Company company = new Company(ID, NAME, ADDRESS, EMAIL, PHONE, false, CBU, image, owner);

        when(companyDao.findById(ID)).thenReturn(Optional.of(company));

        companyService.edit(ID, null, null, null, null, null, null);

        verify(companyDao).findById(eq(ID));
        verify(companyDao).edit(
                eq(ID),
                eq(company.getAddress()),
                eq(company.getEmail()),
                eq(company.getPhone()),
                eq(company.isValidated()),
                eq(company.getCbu()),
                eq(image)
        );
        verifyZeroInteractions(documentService);
    }

    @Test(expected = CompanyNotFoundException.class)
    public void testEditNonExistentCompanyThrows() {
        when(companyDao.findById(ID)).thenReturn(Optional.empty());

        companyService.edit(ID, "address", "email", "phone", "cbu", null, null);
    }

    @Test(expected = DocumentNotFoundException.class)
    public void testEditWithNonExistentDocumentThrows() {
        Company company = mock(Company.class);

        when(companyDao.findById(ID)).thenReturn(Optional.of(company));
        when(documentService.findById(IMAGE_ID_2)).thenReturn(Optional.empty());

        companyService.edit(ID, null, null, null, null, IMAGE_URI_2, null);
    }

    @Test(expected = InvalidDocumentVisibilityException.class)
    public void testEditWithPrivateDocumentThrows() {
        Document image = new Document(IMAGE_ID_2, IMAGE_NAME_2, IMAGE_FILETYPE_2, new byte[0], false);
        Company company = mock(Company.class);

        when(companyDao.findById(ID)).thenReturn(Optional.of(company));
        when(documentService.findById(IMAGE_ID_2)).thenReturn(Optional.of(image));

        companyService.edit(ID, null, null, null, null, IMAGE_URI_2, null);
    }

    // tests para findById
    @Test
    public void testFindByIdExists() {
        Company company = mock(Company.class);

        when(companyDao.findById(ID)).thenReturn(Optional.of(company));

        Optional<Company> result = companyService.findById(ID);

        assertTrue(result.isPresent());
        assertSame(company, result.get());
        verify(companyDao).findById(eq(ID));
    }

    @Test
    public void testFindByIdNotExists() {
        when(companyDao.findById(ID)).thenReturn(Optional.empty());

        Optional<Company> result = companyService.findById(ID);

        assertFalse(result.isPresent());
        verify(companyDao).findById(eq(ID));
    }

    // tests para filter
    @Test
    public void testFilterWithAllParameters() {
        String search = "test";
        Integer ownerId = 1;
        Boolean validated = true;
        int page = 0;

        Company company1 = mock(Company.class), company2 = mock(Company.class);
        Paginator<Company> paginator = new Paginator<>(Arrays.asList(company1, company2), page, 12, 1);

        when(companyDao.filter(search, ownerId, validated, page)).thenReturn(paginator);

        Paginator<Company> result = companyService.filter(search, ownerId, validated, page);

        assertSame(paginator, result);
        verify(companyDao).filter(eq(search), eq(ownerId), eq(validated), eq(page));
    }

    @Test
    public void testFilterWithNullParameters() {
        int page = 0;

        Company company = mock(Company.class);
        Paginator<Company> paginator = new Paginator<>(Collections.singletonList(company), page, 12, 1);

        when(companyDao.filter(null, null, null, page)).thenReturn(paginator);

        Paginator<Company> result = companyService.filter(null, null, null, page);

        assertNotNull(result);
        assertEquals(1, result.getList().size());
        verify(companyDao).filter(eq(null), eq(null), eq(null), eq(page));
    }

    @Test
    public void testFilterWithEmptyResult() {
        int page = 0;

        Paginator<Company> paginator = new Paginator<>();

        when(companyDao.filter(null, null, null, page)).thenReturn(paginator);

        Paginator<Company> result = companyService.filter(null, null, null, page);

        assertNotNull(result);
        assertEquals(0, result.getList().size());
        verify(companyDao).filter(eq(null), eq(null), eq(null), eq(page));
    }

}
