package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.ProductDao;
import ar.edu.itba.paw.interfaces.exception.CategoryNotFoundException;
import ar.edu.itba.paw.interfaces.exception.DocumentNotFoundException;
import ar.edu.itba.paw.interfaces.exception.InvalidDocumentVisibilityException;
import ar.edu.itba.paw.interfaces.exception.InvalidProductRetirementException;
import ar.edu.itba.paw.interfaces.exception.ProductNotFoundException;
import ar.edu.itba.paw.interfaces.service.CategoryService;
import ar.edu.itba.paw.interfaces.service.DocumentService;
import ar.edu.itba.paw.interfaces.service.EmailService;
import ar.edu.itba.paw.interfaces.service.PoolService;
import ar.edu.itba.paw.models.db.Category;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductServiceImplTest {

    private static final int ID = 1;
    private static final String NAME = "Test Product";
    private static final String DESCRIPTION = "Product description";
    private static final double PRICE = 1000.0;

    private static final int IMAGE_ID = 1;
    private static final String IMAGE_URI = "/api/documents/" + IMAGE_ID;

    private static final int CATEGORY_ID = 2;
    private static final String CATEGORY_URI = "/api/categories/" + CATEGORY_ID;

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductDao productDao;

    @Mock
    private DocumentService documentService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private PoolService poolService;

    @Mock
    private EmailService emailService;

    // tests para create
    @Test
    public void testCreateSuccessfully() {
        Document image = mock(Document.class);
        Category category = mock(Category.class);
        Company company = mock(Company.class);
        Product product = mock(Product.class);

        when(image.isPublic()).thenReturn(true);
        when(documentService.findById(IMAGE_ID)).thenReturn(Optional.of(image));
        when(categoryService.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(productDao.create(NAME, DESCRIPTION, PRICE, image, company, category)).thenReturn(product);

        Product result = productService.create(NAME, DESCRIPTION, PRICE, IMAGE_URI, company, CATEGORY_URI);

        assertNotNull(result);
        assertEquals(product, result);
        verify(documentService).findById(eq(IMAGE_ID));
        verify(categoryService).findById(eq(CATEGORY_ID));
        verify(productDao).create(eq(NAME), eq(DESCRIPTION), eq(PRICE), eq(image), eq(company), eq(category));
    }

    @Test(expected = DocumentNotFoundException.class)
    public void testCreateWithNonExistentDocumentThrows() {
        Company company = mock(Company.class);

        when(documentService.findById(IMAGE_ID)).thenReturn(Optional.empty());

        productService.create(NAME, DESCRIPTION, PRICE, IMAGE_URI, company, CATEGORY_URI);
    }

    @Test(expected = InvalidDocumentVisibilityException.class)
    public void testCreateWithPrivateDocumentThrows() {
        Document image = mock(Document.class);
        Company company = mock(Company.class);

        when(image.isPublic()).thenReturn(false);
        when(documentService.findById(IMAGE_ID)).thenReturn(Optional.of(image));

        productService.create(NAME, DESCRIPTION, PRICE, IMAGE_URI, company, CATEGORY_URI);
    }

    @Test(expected = CategoryNotFoundException.class)
    public void testCreateWithNonExistentCategoryThrows() {
        Document image = mock(Document.class);
        Company company = mock(Company.class);

        when(image.isPublic()).thenReturn(true);
        when(documentService.findById(IMAGE_ID)).thenReturn(Optional.of(image));
        when(categoryService.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        productService.create(NAME, DESCRIPTION, PRICE, IMAGE_URI, company, CATEGORY_URI);
    }

    // tests para edit
    @Test
    public void testEditWithAllFields() {
        int newImageId = 3, newCategoryId = 4;
        String newImageUri = "/api/documents/" + newImageId, newCategoryUri = "/api/categories/" + newCategoryId;
        String newName = "Edited Product", newDescription = "Edited description";
        double newPrice = 3000.0;

        Product existingProduct = mock(Product.class);
        Document newImage = mock(Document.class);
        Category newCategory = mock(Category.class);

        when(productDao.findById(ID)).thenReturn(Optional.of(existingProduct));
        when(documentService.findById(newImageId)).thenReturn(Optional.of(newImage));
        when(newImage.isPublic()).thenReturn(true);
        when(categoryService.findById(newCategoryId)).thenReturn(Optional.of(newCategory));

        productService.edit(ID, newName, newDescription, newPrice, newImageUri, newCategoryUri);

        verify(productDao).edit(eq(ID), eq(newName), eq(newDescription), eq(newPrice), eq(newImage), eq(newCategory));
    }

    @Test
    public void testEditWithNullFieldsFallsBackToExisting() {
        Product existingProduct = mock(Product.class);
        Document existingImage = mock(Document.class);
        Category existingCategory = mock(Category.class);

        when(productDao.findById(ID)).thenReturn(Optional.of(existingProduct));
        when(existingProduct.getName()).thenReturn(NAME);
        when(existingProduct.getDescription()).thenReturn(DESCRIPTION);
        when(existingProduct.getPrice()).thenReturn(PRICE);
        when(existingProduct.getImage()).thenReturn(existingImage);
        when(existingProduct.getCategory()).thenReturn(existingCategory);

        productService.edit(ID, null, null, null, null, null);

        verify(productDao).edit(eq(ID), eq(NAME), eq(DESCRIPTION), eq(PRICE), eq(existingImage), eq(existingCategory));
        verifyZeroInteractions(documentService);
        verifyZeroInteractions(categoryService);
    }

    @Test(expected = ProductNotFoundException.class)
    public void testEditProductNotFoundThrows() {
        when(productDao.findById(ID)).thenReturn(Optional.empty());

        productService.edit(ID, NAME, DESCRIPTION, PRICE, null, null);
    }

    @Test(expected = DocumentNotFoundException.class)
    public void testEditWithNonExistentDocumentThrows() {
        Product existingProduct = mock(Product.class);

        when(productDao.findById(ID)).thenReturn(Optional.of(existingProduct));
        when(documentService.findById(IMAGE_ID)).thenReturn(Optional.empty());

        productService.edit(ID, null, null, null, IMAGE_URI, null);
    }

    @Test(expected = InvalidDocumentVisibilityException.class)
    public void testEditWithPrivateDocumentThrows() {
        Product existingProduct = mock(Product.class);
        Document newImage = mock(Document.class);

        when(productDao.findById(ID)).thenReturn(Optional.of(existingProduct));
        when(documentService.findById(IMAGE_ID)).thenReturn(Optional.of(newImage));
        when(newImage.isPublic()).thenReturn(false);

        productService.edit(ID, null, null, null, IMAGE_URI, null);
    }

    @Test(expected = CategoryNotFoundException.class)
    public void testEditWithNonExistentCategoryThrows() {
        Product existingProduct = mock(Product.class);

        when(productDao.findById(ID)).thenReturn(Optional.of(existingProduct));
        when(categoryService.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        productService.edit(ID, null, null, null, null, CATEGORY_URI);
    }

    // tests para retire
    @Test
    public void testRetireSuccessfully() {
        Product product = mock(Product.class);
        Company company = mock(Company.class);
        User owner = mock(User.class);
        Category category = mock(Category.class);

        String ownerEmail = "owner@example.com", ownerPreferredLanguage = "en", categoryName = "Test Category";

        when(productDao.findById(ID)).thenReturn(Optional.of(product));
        when(product.getCanRetire()).thenReturn(true);
        when(product.getCompany()).thenReturn(company);
        when(company.getOwner()).thenReturn(owner);
        when(owner.getEmail()).thenReturn(ownerEmail);
        when(owner.getPreferredLanguage()).thenReturn(ownerPreferredLanguage);
        when(product.getName()).thenReturn(NAME);
        when(product.getPrice()).thenReturn(PRICE);
        when(product.getCategory()).thenReturn(category);
        when(category.getName()).thenReturn(categoryName);

        productService.retire(ID);

        verify(poolService).cancelAvailablePoolsAndRejectRequests(eq(ID));
        verify(productDao).retire(eq(ID));
        verify(emailService).sendProductRetiredEmail(eq(ownerEmail), eq(ownerPreferredLanguage), eq(NAME), eq(PRICE), eq(categoryName), any(Locale.class));
    }

    @Test
    public void testRetireWithNullCategoryUsesEmptyString() {
        Product product = mock(Product.class);
        Company company = mock(Company.class);
        User owner = mock(User.class);

        String ownerEmail = "owner@example.com", ownerPreferredLanguage = "en";

        when(productDao.findById(ID)).thenReturn(Optional.of(product));
        when(product.getCanRetire()).thenReturn(true);
        when(product.getCompany()).thenReturn(company);
        when(company.getOwner()).thenReturn(owner);
        when(owner.getEmail()).thenReturn(ownerEmail);
        when(owner.getPreferredLanguage()).thenReturn(ownerPreferredLanguage);
        when(product.getName()).thenReturn(NAME);
        when(product.getPrice()).thenReturn(PRICE);
        when(product.getCategory()).thenReturn(null);

        productService.retire(ID);

        verify(poolService).cancelAvailablePoolsAndRejectRequests(eq(ID));
        verify(productDao).retire(eq(ID));
        verify(emailService).sendProductRetiredEmail(any(), any(), any(), anyDouble(), eq(""), any(Locale.class));
    }

    @Test(expected = ProductNotFoundException.class)
    public void testRetireNonExistentProductThrows() {
        when(productDao.findById(ID)).thenReturn(Optional.empty());

        productService.retire(ID);
    }

    @Test(expected = InvalidProductRetirementException.class)
    public void testRetireNonRetirableProductThrows() {
        Product product = mock(Product.class);

        when(productDao.findById(ID)).thenReturn(Optional.of(product));
        when(product.getCanRetire()).thenReturn(false);

        productService.retire(ID);
    }

    // tests para findById
    @Test
    public void testFindByIdExists() {
        Product product = mock(Product.class);

        when(productDao.findById(ID)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.findById(ID);

        assertTrue(result.isPresent());
        assertEquals(product, result.get());
        verify(productDao).findById(eq(ID));
    }

    @Test
    public void testFindByIdNotExists() {
        when(productDao.findById(ID)).thenReturn(Optional.empty());

        Optional<Product> result = productService.findById(ID);

        assertFalse(result.isPresent());
        verify(productDao).findById(eq(ID));
    }

    // tests para filter
    @Test
    public void testFilterWithAllParameters() {
        String search = "test";
        Integer categoryId = 1, companyId = 2;
        Double priceMin = 100.0, priceMax = 1000.0;
        Boolean active = true;
        int page = 0;
        String orderBy = "price";
        boolean desc = false;

        Product product = mock(Product.class);
        Paginator<Product> paginator = new Paginator<>(Collections.singletonList(product), page, 12, 1);

        when(productDao.filter(search, categoryId, companyId, priceMin, priceMax, active, page, orderBy, desc))
                .thenReturn(paginator);

        Paginator<Product> result = productService.filter(search, categoryId, companyId, priceMin, priceMax, active, page, orderBy, desc);

        assertNotNull(result);
        assertEquals(1, result.getList().size());
        verify(productDao).filter(eq(search), eq(categoryId), eq(companyId), eq(priceMin), eq(priceMax), eq(active), eq(page), eq(orderBy), eq(desc));
    }

    @Test
    public void testFilterWithNullParameters() {
        int page = 0;
        Paginator<Product> paginator = new Paginator<>();

        when(productDao.filter(null, null, null, null, null, null, page, null, false)).thenReturn(paginator);

        Paginator<Product> result = productService.filter(null, null, null, null, null, null, page, null, false);

        assertNotNull(result);
        assertEquals(0, result.getList().size());
        verify(productDao).filter(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(page), eq(null), eq(false));
    }

    @Test
    public void testFilterWithNegativePageClampsToZero() {
        int page = -3;
        Paginator<Product> paginator = new Paginator<>();

        when(productDao.filter(null, null, null, null, null, null, 0, null, false)).thenReturn(paginator);

        Paginator<Product> result = productService.filter(null, null, null, null, null, null, page, null, false);

        assertNotNull(result);
        verify(productDao).filter(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(0), eq(null), eq(false));
    }

}
