package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.PoolDao;
import ar.edu.itba.paw.interfaces.dao.RequestDao;
import ar.edu.itba.paw.interfaces.exception.InvalidPoolStatusException;
import ar.edu.itba.paw.interfaces.exception.LocationNotFoundException;
import ar.edu.itba.paw.interfaces.exception.PoolNotFoundException;
import ar.edu.itba.paw.interfaces.exception.ProductNotFoundException;
import ar.edu.itba.paw.interfaces.service.EmailService;
import ar.edu.itba.paw.interfaces.service.LocationService;
import ar.edu.itba.paw.interfaces.service.ProductService;
import ar.edu.itba.paw.interfaces.service.RequestService;
import ar.edu.itba.paw.models.db.Company;
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
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PoolServiceImplTest {

    private static final int ID = 1;
    private static final int PRODUCT_ID = 1;
    private static final int LOCATION_ID = 2;
    private static final int MIN_QUANTITY = 10;
    private static final int DOWN_PAYMENT = 5000;
    private static final double PRICE = 10000.0;
    private static final String PRODUCT_URI = "/api/products/" + PRODUCT_ID;
    private static final String LOCATION_URI = "/api/locations/" + LOCATION_ID;

    @InjectMocks
    private PoolServiceImpl poolService;

    @Mock
    private PoolDao poolDao;

    @Mock
    private ProductService productService;

    @Mock
    private LocationService locationService;

    @Mock
    private RequestService requestService;

    @Mock
    private EmailService emailService;

    // tests para create
    @Test
    public void testCreateSuccessfully() {
        Product product = mock(Product.class);
        Location location = mock(Location.class);
        Pool pool = mock(Pool.class);

        when(product.getPrice()).thenReturn(PRICE);
        when(productService.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(locationService.findById(LOCATION_ID)).thenReturn(Optional.of(location));
        when(poolDao.create(MIN_QUANTITY, DOWN_PAYMENT, PRICE, product, location)).thenReturn(pool);

        Pool result = poolService.create(MIN_QUANTITY, DOWN_PAYMENT, PRODUCT_URI, LOCATION_URI);

        assertNotNull(result);
        assertEquals(pool, result);
        verify(productService).findById(eq(PRODUCT_ID));
        verify(locationService).findById(eq(LOCATION_ID));
        verify(poolDao).create(eq(MIN_QUANTITY), eq(DOWN_PAYMENT), eq(PRICE), eq(product), eq(location));
    }

    @Test(expected = ProductNotFoundException.class)
    public void testCreateWithNonExistentProduct() {
        when(productService.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        poolService.create(MIN_QUANTITY, DOWN_PAYMENT, PRODUCT_URI, LOCATION_URI);
    }

    @Test(expected = LocationNotFoundException.class)
    public void testCreateWithNonExistentLocation() {
        Product product = mock(Product.class);

        when(productService.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(locationService.findById(LOCATION_ID)).thenReturn(Optional.empty());

        poolService.create(MIN_QUANTITY, DOWN_PAYMENT, PRODUCT_URI, LOCATION_URI);
    }

    // tests para edit
    @Test
    public void testEditMinQuantityOnly() {
        int newMinQuantity = 20;

        poolService.edit(ID, newMinQuantity, null);

        verify(poolDao).edit(eq(ID), eq(newMinQuantity));
        verify(poolDao, never()).setStatus(anyInt(), any());
    }

    @Test
    public void testEditStatusToPaused() {
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Company company = mock(Company.class);
        User owner = mock(User.class);

        when(poolDao.findById(ID)).thenReturn(Optional.of(pool));
        when(pool.getRequests()).thenReturn(new HashSet<>());
        when(pool.getProduct()).thenReturn(product);
        when(product.getCompany()).thenReturn(company);
        when(company.getOwner()).thenReturn(owner);
        when(owner.getEmail()).thenReturn("owner@test.com");
        when(owner.getPreferredLanguage()).thenReturn("en");

        poolService.edit(ID, null, "PAUSED");

        verify(poolDao, never()).edit(anyInt(), anyInt());
        verify(poolDao).setStatus(eq(ID), eq(Pool.Status.PAUSED));
    }

    @Test
    public void testEditStatusToDelivering() {
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Company company = mock(Company.class);
        User owner = mock(User.class);

        when(poolDao.findById(ID)).thenReturn(Optional.of(pool));
        when(pool.getRequests()).thenReturn(new HashSet<>());
        when(pool.getProduct()).thenReturn(product);
        when(product.getCompany()).thenReturn(company);
        when(company.getOwner()).thenReturn(owner);
        when(owner.getEmail()).thenReturn("owner@test.com");
        when(owner.getPreferredLanguage()).thenReturn("en");

        poolService.edit(ID, null, "DELIVERING");

        verify(poolDao).setStatus(eq(ID), eq(Pool.Status.DELIVERING));
        verify(emailService).sendPoolStartsDeliveringEmail(anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testEditStatusToFinished() {
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Company company = mock(Company.class);
        User owner = mock(User.class);

        when(poolDao.findById(ID)).thenReturn(Optional.of(pool));
        when(pool.getRequests()).thenReturn(new HashSet<>());
        when(pool.getProduct()).thenReturn(product);
        when(product.getCompany()).thenReturn(company);
        when(company.getOwner()).thenReturn(owner);
        when(owner.getEmail()).thenReturn("owner@test.com");
        when(owner.getPreferredLanguage()).thenReturn("en");

        poolService.edit(ID, null, "FINISHED");

        verify(poolDao).setStatus(eq(ID), eq(Pool.Status.FINISHED));
        verify(emailService).sendPoolFinishedEmail(anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testEditStatusToCancelled() {
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Company company = mock(Company.class);
        User owner = mock(User.class);

        when(poolDao.findById(ID)).thenReturn(Optional.of(pool));
        when(pool.getRequests()).thenReturn(new HashSet<>());
        when(pool.getProduct()).thenReturn(product);
        when(product.getCompany()).thenReturn(company);
        when(company.getOwner()).thenReturn(owner);
        when(owner.getEmail()).thenReturn("owner@test.com");
        when(owner.getPreferredLanguage()).thenReturn("en");

        poolService.edit(ID, null, "CANCELLED");

        verify(poolDao).setStatus(eq(ID), eq(Pool.Status.CANCELLED));
        verify(emailService).sendPoolCancelledEmail(anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testEditBothMinQuantityAndStatus() {
        int newMinQuantity = 15;
        Pool pool = mock(Pool.class);
        Product product = mock(Product.class);
        Company company = mock(Company.class);
        User owner = mock(User.class);

        when(poolDao.findById(ID)).thenReturn(Optional.of(pool));
        when(pool.getRequests()).thenReturn(new HashSet<>());
        when(pool.getProduct()).thenReturn(product);
        when(product.getCompany()).thenReturn(company);
        when(company.getOwner()).thenReturn(owner);
        when(owner.getEmail()).thenReturn("owner@test.com");
        when(owner.getPreferredLanguage()).thenReturn("en");

        poolService.edit(ID, newMinQuantity, "AVAILABLE");

        verify(poolDao).edit(eq(ID), eq(newMinQuantity));
        verify(poolDao).setStatus(eq(ID), eq(Pool.Status.AVAILABLE));
    }

    @Test(expected = InvalidPoolStatusException.class)
    public void testEditWithInvalidStatusThrows() {
        poolService.edit(ID, null, "INVALID_STATUS");
    }

    @Test
    public void testEditWithNullParameters() {
        poolService.edit(ID, null, null);

        verify(poolDao, never()).edit(anyInt(), anyInt());
        verify(poolDao, never()).setStatus(anyInt(), any());
    }

    @Test(expected = InvalidPoolStatusException.class)
    public void testEditStatusPoolNotFoundThrows() {
        when(poolDao.findById(ID)).thenReturn(Optional.empty());

        poolService.edit(ID, null, "CANCELLED");
    }

    // tests para cancelAvailablePoolsAndRejectRequests
    @Test
    public void testCancelAvailablePoolsAndRejectRequests() {
        poolService.cancelAvailablePoolsAndRejectRequests(PRODUCT_ID);

        verify(requestService).rejectPendingRequests(eq(PRODUCT_ID));
        verify(poolDao).cancelAvailablePools(eq(PRODUCT_ID));
    }

    // tests para findById
    @Test
    public void testFindByIdExists() {
        Pool pool = mock(Pool.class);

        when(poolDao.findById(ID)).thenReturn(Optional.of(pool));

        Optional<Pool> result = poolService.findById(ID);

        assertTrue(result.isPresent());
        assertEquals(pool, result.get());
        verify(poolDao).findById(eq(ID));
    }

    @Test
    public void testFindByIdNotExists() {
        when(poolDao.findById(ID)).thenReturn(Optional.empty());

        Optional<Pool> result = poolService.findById(ID);

        assertFalse(result.isPresent());
        verify(poolDao).findById(eq(ID));
    }

    // tests para filter
    @Test
    public void testFilterWithAllParameters() {
        String search = "test";
        String status = "AVAILABLE";
        Integer companyId = 2;
        Double priceMin = 100.0, priceMax = 1000.0;
        Integer locationId = 3, categoryId = 4;
        int page = 0;
        String orderBy = "price";
        boolean desc = false;

        Pool pool = mock(Pool.class);
        Paginator<Pool> paginator = new Paginator<>(Collections.singletonList(pool), page, 12, 1);

        when(poolDao.filter(PRODUCT_ID, search, status, companyId, priceMin, priceMax, locationId, categoryId, page, orderBy, desc))
                .thenReturn(paginator);

        Paginator<Pool> result = poolService.filter(PRODUCT_ID, search, status, companyId, priceMin, priceMax, locationId, categoryId, page, orderBy, desc);

        assertNotNull(result);
        assertEquals(1, result.getList().size());
        assertEquals(pool, result.getList().getFirst());
        verify(poolDao).filter(eq(PRODUCT_ID), eq(search), eq(status), eq(companyId), eq(priceMin), eq(priceMax), eq(locationId), eq(categoryId), eq(page), eq(orderBy), eq(desc));
    }

    @Test
    public void testFilterWithNullParameters() {
        int page = 0;
        Paginator<Pool> paginator = new Paginator<>();

        when(poolDao.filter(null, null, null, null, null, null, null, null, page, null, true)).thenReturn(paginator);

        Paginator<Pool> result = poolService.filter(null, null, null, null, null, null, null, null, page, null, true);

        assertNotNull(result);
        assertEquals(0, result.getList().size());
        verify(poolDao).filter(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(page), eq(null), eq(true));
    }

    @Test
    public void testFilterWithNegativePageClampsToZero() {
        int page = -5;
        Paginator<Pool> paginator = new Paginator<>();

        when(poolDao.filter(null, null, null, null, null, null, null, null, 0, null, true)).thenReturn(paginator);

        Paginator<Pool> result = poolService.filter(null, null, null, null, null, null, null, null, page, null, true);

        assertNotNull(result);
        verify(poolDao).filter(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(0), eq(null), eq(true));
    }

    @Test(expected = InvalidPoolStatusException.class)
    public void testFilterWithInvalidStatusThrows() {
        poolService.filter(null, null, "INVALID_STATUS", null, null, null, null, null, 0, null, false);
    }

}
