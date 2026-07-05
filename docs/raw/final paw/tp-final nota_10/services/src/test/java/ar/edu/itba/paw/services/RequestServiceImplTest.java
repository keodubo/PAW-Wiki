package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.RequestDao;
import ar.edu.itba.paw.interfaces.exception.DocumentNotFoundException;
import ar.edu.itba.paw.interfaces.exception.InvalidDocumentVisibilityException;
import ar.edu.itba.paw.interfaces.exception.InvalidPoolStatusException;
import ar.edu.itba.paw.interfaces.exception.InvalidRequestStatusException;
import ar.edu.itba.paw.interfaces.exception.PoolNotFoundException;
import ar.edu.itba.paw.interfaces.exception.RequestNotFoundException;
import ar.edu.itba.paw.interfaces.service.DocumentService;
import ar.edu.itba.paw.interfaces.service.EmailService;
import ar.edu.itba.paw.interfaces.service.PoolService;
import ar.edu.itba.paw.models.db.Category;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.Location;
import ar.edu.itba.paw.models.db.Pool;
import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.db.Request;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RequestServiceImplTest {

    private static final int ID = 1;
    private static final int POOL_ID = 1;
    private static final int USER_ID = 10;
    private static final int PRODUCT_ID = 1;
    private static final int QUANTITY = 10;
    private static final int DOWN_PAYMENT_AMOUNT = 50;
    private static final double PRICE = 100.0;
    private static final String POOL_URI = "/api/pools/" + POOL_ID;
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_LANG = "en";
    private static final String PRODUCT_NAME = "Test Product";
    private static final String CATEGORY_NAME = "TestCategory";
    private static final String LOCATION_NAME = "TestLocation";

    private static final int DOWN_PAYMENT_DOC_ID = 5;
    private static final String DOWN_PAYMENT_URI = "/api/documents/" + DOWN_PAYMENT_DOC_ID;

    private static final int FINAL_PAYMENT_DOC_ID = 6;
    private static final String FINAL_PAYMENT_URI = "/api/documents/" + FINAL_PAYMENT_DOC_ID;

    @InjectMocks
    private RequestServiceImpl requestService;

    @Mock
    private RequestDao requestDao;

    @Mock
    private PoolService poolService;

    @Mock
    private DocumentService documentService;

    @Mock
    private EmailService emailService;

    private void configureMocksForEmailOnPool(Pool pool, Product product, Category category, Location location, User user) {
        when(user.getEmail()).thenReturn(USER_EMAIL);
        when(user.getPreferredLanguage()).thenReturn(USER_LANG);
        when(pool.getId()).thenReturn(POOL_ID);
        when(product.getName()).thenReturn(PRODUCT_NAME);
        when(product.getPrice()).thenReturn(PRICE);
        when(category.getName()).thenReturn(CATEGORY_NAME);
        when(location.getName()).thenReturn(LOCATION_NAME);
        when(pool.getDownPayment()).thenReturn(DOWN_PAYMENT_AMOUNT);
    }

    // tests para create
    @Test
    public void testCreateSuccessfully() {
        User user = mock(User.class);
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Category category = mock(Category.class);
        Location location = mock(Location.class);
        Request request = mock(Request.class);

        configureMocksForEmailOnPool(pool, product, category, location, user);
        when(poolService.findById(POOL_ID)).thenReturn(Optional.of(pool));
        when(requestDao.create(QUANTITY, user, pool)).thenReturn(request);
        when(pool.getProduct()).thenReturn(product);
        when(product.getCategory()).thenReturn(category);
        when(pool.getLocation()).thenReturn(location);

        Request result = requestService.create(QUANTITY, user, POOL_URI);

        assertNotNull(result);
        assertEquals(request, result);
        verify(poolService).findById(eq(POOL_ID));
        verify(requestDao).create(eq(QUANTITY), eq(user), eq(pool));
        verify(emailService).sendRequestMadeEmail(eq(USER_EMAIL), eq(USER_LANG), eq(POOL_ID), eq(PRODUCT_NAME), eq(PRICE), eq(CATEGORY_NAME), eq(LOCATION_NAME), eq(QUANTITY), eq(DOWN_PAYMENT_AMOUNT), any(Locale.class));
    }

    @Test
    public void testCreateWithNullCategoryAndLocationUsesEmptyString() {
        User user = mock(User.class);
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Request request = mock(Request.class);

        when(user.getEmail()).thenReturn(USER_EMAIL);
        when(user.getPreferredLanguage()).thenReturn(USER_LANG);
        when(pool.getId()).thenReturn(POOL_ID);
        when(product.getName()).thenReturn(PRODUCT_NAME);
        when(product.getPrice()).thenReturn(PRICE);
        when(pool.getDownPayment()).thenReturn(DOWN_PAYMENT_AMOUNT);
        when(poolService.findById(POOL_ID)).thenReturn(Optional.of(pool));
        when(requestDao.create(QUANTITY, user, pool)).thenReturn(request);
        when(pool.getProduct()).thenReturn(product);
        when(product.getCategory()).thenReturn(null);
        when(pool.getLocation()).thenReturn(null);

        Request result = requestService.create(QUANTITY, user, POOL_URI);

        assertNotNull(result);
        verify(emailService).sendRequestMadeEmail(any(), any(), anyInt(), any(), anyDouble(), eq(""), eq(""), anyInt(), anyInt(), any(Locale.class));
    }

    @Test(expected = PoolNotFoundException.class)
    public void testCreateWithNonExistentPoolThrows() {
        User user = mock(User.class);

        when(poolService.findById(POOL_ID)).thenReturn(Optional.empty());

        requestService.create(QUANTITY, user, POOL_URI);
    }

    // tests para delete
    @Test
    public void testDeleteSuccessfully() {
        Request request = mock(Request.class);
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Category category = mock(Category.class);
        Location location = mock(Location.class);
        User user = mock(User.class);

        configureMocksForEmailOnPool(pool, product, category, location, user);
        when(request.getQuantity()).thenReturn(QUANTITY);
        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(request.getPool()).thenReturn(pool);
        when(pool.getProduct()).thenReturn(product);
        when(product.getCategory()).thenReturn(category);
        when(pool.getLocation()).thenReturn(location);
        when(request.getUser()).thenReturn(user);

        requestService.delete(ID);

        verify(emailService).sendRequestDeletedEmail(eq(USER_EMAIL), eq(USER_LANG), eq(POOL_ID), eq(PRODUCT_NAME), eq(PRICE), eq(CATEGORY_NAME), eq(LOCATION_NAME), eq(QUANTITY), eq(DOWN_PAYMENT_AMOUNT), any(Locale.class));
        verify(requestDao).delete(eq(ID));
    }

    @Test(expected = RequestNotFoundException.class)
    public void testDeleteNonExistentRequestThrows() {
        when(requestDao.findById(ID)).thenReturn(Optional.empty());

        requestService.delete(ID);
    }

    // tests para setStatus
    @Test
    public void testSetStatusToAcceptedSendsEmail() {
        Request request = mock(Request.class);
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Category category = mock(Category.class);
        Location location = mock(Location.class);
        User user = mock(User.class);

        configureMocksForEmailOnPool(pool, product, category, location, user);
        when(request.getQuantity()).thenReturn(QUANTITY);
        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(request.getStatus()).thenReturn(Request.Status.PENDING);
        when(request.getPool()).thenReturn(pool);
        when(pool.getProduct()).thenReturn(product);
        when(product.getCategory()).thenReturn(category);
        when(pool.getLocation()).thenReturn(location);
        when(request.getUser()).thenReturn(user);
        when(pool.getAcceptedRequestsSum()).thenReturn(0);
        when(pool.getMinQuantity()).thenReturn(100);

        Request.Status oldStatus = requestService.setStatus(ID, Request.Status.ACCEPTED);

        assertEquals(Request.Status.PENDING, oldStatus);
        verify(requestDao).setStatus(eq(ID), eq(Request.Status.ACCEPTED));
        verify(emailService).sendRequestAcceptedEmail(eq(USER_EMAIL), eq(USER_LANG), eq(POOL_ID), eq(PRODUCT_NAME), eq(PRICE), eq(CATEGORY_NAME), eq(LOCATION_NAME), eq(QUANTITY), eq(DOWN_PAYMENT_AMOUNT), any(Locale.class));
    }

    @Test
    public void testSetStatusToAcceptedWhenMinQuantityReachedSendsPoolFullEmail() {
        Request request = mock(Request.class);
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Category category = mock(Category.class);
        Location location = mock(Location.class);
        User user = mock(User.class);
        Company company = mock(Company.class);
        User owner = mock(User.class);

        configureMocksForEmailOnPool(pool, product, category, location, user);
        when(request.getQuantity()).thenReturn(QUANTITY);
        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(request.getStatus()).thenReturn(Request.Status.PENDING);
        when(request.getPool()).thenReturn(pool);
        when(pool.getProduct()).thenReturn(product);
        when(product.getCategory()).thenReturn(category);
        when(pool.getLocation()).thenReturn(location);
        when(request.getUser()).thenReturn(user);

        when(pool.getAcceptedRequestsSum()).thenReturn(5);
        when(pool.getMinQuantity()).thenReturn(10);
        when(pool.getRequests()).thenReturn(new HashSet<>());
        when(product.getCompany()).thenReturn(company);
        when(company.getName()).thenReturn("Test Company");
        when(owner.getEmail()).thenReturn("owner@example.com");
        when(owner.getPreferredLanguage()).thenReturn("en");
        when(company.getOwner()).thenReturn(owner);

        requestService.setStatus(ID, Request.Status.ACCEPTED);

        verify(emailService).sendPoolFullEmail(anyInt(), any(), any(), any(), any(), any(), any(Locale.class));
    }

    @Test
    public void testSetStatusToRejectedSendsEmail() {
        Request request = mock(Request.class);
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Category category = mock(Category.class);
        Location location = mock(Location.class);
        User user = mock(User.class);

        configureMocksForEmailOnPool(pool, product, category, location, user);
        when(request.getQuantity()).thenReturn(QUANTITY);
        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(request.getStatus()).thenReturn(Request.Status.PENDING);
        when(request.getPool()).thenReturn(pool);
        when(pool.getProduct()).thenReturn(product);
        when(product.getCategory()).thenReturn(category);
        when(pool.getLocation()).thenReturn(location);
        when(request.getUser()).thenReturn(user);

        Request.Status oldStatus = requestService.setStatus(ID, Request.Status.REJECTED);

        assertEquals(Request.Status.PENDING, oldStatus);
        verify(requestDao).setStatus(eq(ID), eq(Request.Status.REJECTED));
        verify(emailService).sendRequestRejectedEmail(eq(USER_EMAIL), eq(USER_LANG), eq(POOL_ID), eq(PRODUCT_NAME), eq(PRICE), eq(CATEGORY_NAME), eq(LOCATION_NAME), eq(QUANTITY), eq(DOWN_PAYMENT_AMOUNT), any(Locale.class));
    }

    @Test
    public void testSetStatusToDeliveredSendsEmail() {
        Request request = mock(Request.class);
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Category category = mock(Category.class);
        Location location = mock(Location.class);
        User user = mock(User.class);

        configureMocksForEmailOnPool(pool, product, category, location, user);
        when(request.getQuantity()).thenReturn(QUANTITY);
        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(request.getStatus()).thenReturn(Request.Status.ACCEPTED);
        when(request.getPool()).thenReturn(pool);
        when(pool.getProduct()).thenReturn(product);
        when(product.getCategory()).thenReturn(category);
        when(pool.getLocation()).thenReturn(location);
        when(request.getUser()).thenReturn(user);

        Request.Status oldStatus = requestService.setStatus(ID, Request.Status.DELIVERED);

        assertEquals(Request.Status.ACCEPTED, oldStatus);
        verify(requestDao).setStatus(eq(ID), eq(Request.Status.DELIVERED));
        verify(emailService).sendRequestDeliveredEmail(eq(USER_EMAIL), eq(USER_LANG), eq(POOL_ID), eq(PRODUCT_NAME), eq(PRICE), eq(CATEGORY_NAME), eq(LOCATION_NAME), eq(QUANTITY), eq(DOWN_PAYMENT_AMOUNT), any(Locale.class));
    }

    @Test
    public void testSetStatusToPendingDoesNotSendEmail() {
        Request request = mock(Request.class);
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);

        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(request.getStatus()).thenReturn(Request.Status.ACCEPTED);
        when(request.getPool()).thenReturn(pool);
        when(pool.getProduct()).thenReturn(product);

        Request.Status oldStatus = requestService.setStatus(ID, Request.Status.PENDING);

        assertEquals(Request.Status.ACCEPTED, oldStatus);
        verify(requestDao).setStatus(eq(ID), eq(Request.Status.PENDING));
        verify(emailService, never()).sendRequestAcceptedEmail(any(), any(), anyInt(), any(), anyDouble(), any(), any(), anyInt(), anyInt(), any());
        verify(emailService, never()).sendRequestRejectedEmail(any(), any(), anyInt(), any(), anyDouble(), any(), any(), anyInt(), anyInt(), any());
        verify(emailService, never()).sendRequestDeliveredEmail(any(), any(), anyInt(), any(), anyDouble(), any(), any(), anyInt(), anyInt(), any());
    }

    @Test(expected = RequestNotFoundException.class)
    public void testSetStatusRequestNotFoundThrows() {
        when(requestDao.findById(ID)).thenReturn(Optional.empty());

        requestService.setStatus(ID, Request.Status.ACCEPTED);
    }

    // tests para edit
    @Test
    public void testEditQuantityOnly() {
        int newQuantity = 15;
        Request request = mock(Request.class);
        Document downPayment = mock(Document.class);
        Document finalPayment = mock(Document.class);

        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(request.getDownPayment()).thenReturn(downPayment);
        when(request.getFinalPayment()).thenReturn(finalPayment);

        requestService.edit(ID, newQuantity, null, null, null);

        verify(requestDao).edit(eq(ID), eq(newQuantity), eq(downPayment), eq(finalPayment));
        verify(requestDao, never()).setStatus(anyInt(), any());
    }

    @Test
    public void testEditStatusOnly() {
        Request request = mock(Request.class);
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Category category = mock(Category.class);
        Location location = mock(Location.class);
        User user = mock(User.class);

        configureMocksForEmailOnPool(pool, product, category, location, user);
        when(request.getQuantity()).thenReturn(QUANTITY);
        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(request.getStatus()).thenReturn(Request.Status.PENDING);
        when(request.getPool()).thenReturn(pool);
        when(pool.getProduct()).thenReturn(product);
        when(product.getCategory()).thenReturn(category);
        when(pool.getLocation()).thenReturn(location);
        when(request.getUser()).thenReturn(user);
        when(pool.getAcceptedRequestsSum()).thenReturn(0);
        when(pool.getMinQuantity()).thenReturn(100);

        requestService.edit(ID, null, "ACCEPTED", null, null);

        verify(requestDao).setStatus(eq(ID), eq(Request.Status.ACCEPTED));
    }

    @Test
    public void testEditDownPaymentOnly() {
        Request request = mock(Request.class);
        Document newDownPayment = mock(Document.class);
        Document finalPayment = mock(Document.class);

        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(request.getQuantity()).thenReturn(QUANTITY);
        when(request.getFinalPayment()).thenReturn(finalPayment);
        when(documentService.findById(DOWN_PAYMENT_DOC_ID)).thenReturn(Optional.of(newDownPayment));
        when(newDownPayment.isPublic()).thenReturn(false);

        requestService.edit(ID, null, null, DOWN_PAYMENT_URI, null);

        verify(requestDao).edit(eq(ID), eq(QUANTITY), eq(newDownPayment), eq(finalPayment));
    }

    @Test
    public void testEditFinalPaymentOnly() {
        Request request = mock(Request.class);
        Document downPayment = mock(Document.class);
        Document newFinalPayment = mock(Document.class);

        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(request.getQuantity()).thenReturn(QUANTITY);
        when(request.getDownPayment()).thenReturn(downPayment);
        when(documentService.findById(FINAL_PAYMENT_DOC_ID)).thenReturn(Optional.of(newFinalPayment));
        when(newFinalPayment.isPublic()).thenReturn(false);

        requestService.edit(ID, null, null, null, FINAL_PAYMENT_URI);

        verify(requestDao).edit(eq(ID), eq(QUANTITY), eq(downPayment), eq(newFinalPayment));
    }

    @Test(expected = InvalidDocumentVisibilityException.class)
    public void testEditWithPublicDownPaymentThrows() {
        Request request = mock(Request.class);
        Document publicDoc = mock(Document.class);

        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(documentService.findById(DOWN_PAYMENT_DOC_ID)).thenReturn(Optional.of(publicDoc));
        when(publicDoc.isPublic()).thenReturn(true);

        requestService.edit(ID, null, null, DOWN_PAYMENT_URI, null);
    }

    @Test(expected = InvalidDocumentVisibilityException.class)
    public void testEditWithPublicFinalPaymentThrows() {
        Request request = mock(Request.class);
        Document publicDoc = mock(Document.class);

        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(documentService.findById(FINAL_PAYMENT_DOC_ID)).thenReturn(Optional.of(publicDoc));
        when(publicDoc.isPublic()).thenReturn(true);

        requestService.edit(ID, null, null, null, FINAL_PAYMENT_URI);
    }

    @Test(expected = DocumentNotFoundException.class)
    public void testEditWithNonExistentDownPaymentDocumentThrows() {
        Request request = mock(Request.class);

        when(requestDao.findById(ID)).thenReturn(Optional.of(request));
        when(documentService.findById(DOWN_PAYMENT_DOC_ID)).thenReturn(Optional.empty());

        requestService.edit(ID, null, null, DOWN_PAYMENT_URI, null);
    }

    @Test(expected = RequestNotFoundException.class)
    public void testEditRequestNotFoundThrows() {
        when(requestDao.findById(ID)).thenReturn(Optional.empty());

        requestService.edit(ID, QUANTITY, null, null, null);
    }

    // tests para rejectPendingRequests
    @Test
    public void testRejectPendingRequests() {
        requestService.rejectPendingRequests(PRODUCT_ID);

        verify(requestDao).rejectPendingRequests(eq(PRODUCT_ID));
    }

    // tests para findById
    @Test
    public void testFindByIdExists() {
        Request request = mock(Request.class);

        when(requestDao.findById(ID)).thenReturn(Optional.of(request));

        Optional<Request> result = requestService.findById(ID);

        assertTrue(result.isPresent());
        assertEquals(request, result.get());
        verify(requestDao).findById(eq(ID));
    }

    @Test
    public void testFindByIdNotExists() {
        when(requestDao.findById(ID)).thenReturn(Optional.empty());

        Optional<Request> result = requestService.findById(ID);

        assertFalse(result.isPresent());
        verify(requestDao).findById(eq(ID));
    }

    // tests para findFinishedByProductAndUser
    @Test
    public void testFindFinishedByProductAndUserExists() {
        int productId = 1;
        Request request = mock(Request.class);

        when(requestDao.findFinishedByProductAndUser(productId, USER_ID)).thenReturn(Optional.of(request));

        Optional<Request> result = requestService.findFinishedByProductAndUser(productId, USER_ID);

        assertTrue(result.isPresent());
        assertEquals(request, result.get());
        verify(requestDao).findFinishedByProductAndUser(eq(productId), eq(USER_ID));
    }

    @Test
    public void testFindFinishedByProductAndUserNotExists() {
        int productId = 1;

        when(requestDao.findFinishedByProductAndUser(productId, USER_ID)).thenReturn(Optional.empty());

        Optional<Request> result = requestService.findFinishedByProductAndUser(productId, USER_ID);

        assertFalse(result.isPresent());
        verify(requestDao).findFinishedByProductAndUser(eq(productId), eq(USER_ID));
    }

    // tests para findByCompanyAndUser
    @Test
    public void testFindByCompanyAndUserExists() {
        int companyId = 1;
        Request request = mock(Request.class);

        when(requestDao.findByCompanyAndUser(companyId, USER_ID)).thenReturn(Optional.of(request));

        Optional<Request> result = requestService.findByCompanyAndUser(companyId, USER_ID);

        assertTrue(result.isPresent());
        assertEquals(request, result.get());
        verify(requestDao).findByCompanyAndUser(eq(companyId), eq(USER_ID));
    }

    @Test
    public void testFindByCompanyAndUserNotExists() {
        int companyId = 1;

        when(requestDao.findByCompanyAndUser(companyId, USER_ID)).thenReturn(Optional.empty());

        Optional<Request> result = requestService.findByCompanyAndUser(companyId, USER_ID);

        assertFalse(result.isPresent());
        verify(requestDao).findByCompanyAndUser(eq(companyId), eq(USER_ID));
    }

    // tests para findByPayment
    @Test
    public void testFindByPaymentExists() {
        int paymentId = 99;
        Request request = mock(Request.class);

        when(requestDao.findByPayment(paymentId)).thenReturn(Optional.of(request));

        Optional<Request> result = requestService.findByPayment(paymentId);

        assertTrue(result.isPresent());
        assertEquals(request, result.get());
        verify(requestDao).findByPayment(eq(paymentId));
    }

    @Test
    public void testFindByPaymentNotExists() {
        int paymentId = 99;

        when(requestDao.findByPayment(paymentId)).thenReturn(Optional.empty());

        Optional<Request> result = requestService.findByPayment(paymentId);

        assertFalse(result.isPresent());
        verify(requestDao).findByPayment(eq(paymentId));
    }

    // tests para filter
    @Test
    public void testFilterWithAllParameters() {
        String search = "test";
        Integer companyId = 1, productId = 2, poolId = 3;
        String poolStatus = "AVAILABLE", requestStatus = "PENDING";
        int page = 0;
        String orderBy = "id";
        boolean desc = false;

        Request request = mock(Request.class);
        Paginator<Request> paginator = new Paginator<>(Collections.singletonList(request), page, 12, 1);

        when(requestDao.filter(search, companyId, productId, poolStatus, requestStatus, poolId, USER_ID, page, orderBy, desc))
                .thenReturn(paginator);

        Paginator<Request> result = requestService.filter(search, companyId, productId, poolStatus, requestStatus, poolId, USER_ID, page, orderBy, desc);

        assertNotNull(result);
        assertEquals(1, result.getList().size());
        verify(requestDao).filter(eq(search), eq(companyId), eq(productId), eq(poolStatus), eq(requestStatus), eq(poolId), eq(USER_ID), eq(page), eq(orderBy), eq(desc));
    }

    @Test
    public void testFilterWithNullParameters() {
        int page = 0;
        Paginator<Request> paginator = new Paginator<>();

        when(requestDao.filter(null, null, null, null, null, null, null, page, null, false)).thenReturn(paginator);

        Paginator<Request> result = requestService.filter(null, null, null, null, null, null, null, page, null, false);

        assertNotNull(result);
        assertEquals(0, result.getList().size());
        verify(requestDao).filter(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(page), eq(null), eq(false));
    }

    @Test(expected = InvalidPoolStatusException.class)
    public void testFilterWithInvalidPoolStatusThrows() {
        requestService.filter(null, null, null, "INVALID_POOL_STATUS", null, null, null, 0, null, false);
    }

    @Test(expected = InvalidRequestStatusException.class)
    public void testFilterWithInvalidRequestStatusThrows() {
        requestService.filter(null, null, null, null, "INVALID_REQUEST_STATUS", null, null, 0, null, false);
    }

}
