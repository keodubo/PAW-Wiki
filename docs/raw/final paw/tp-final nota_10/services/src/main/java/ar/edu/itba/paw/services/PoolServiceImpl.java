package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.PoolDao;
import ar.edu.itba.paw.interfaces.dao.RequestDao;
import ar.edu.itba.paw.interfaces.exception.InvalidPoolStatusException;
import ar.edu.itba.paw.interfaces.exception.LocationNotFoundException;
import ar.edu.itba.paw.interfaces.exception.PoolNotFoundException;
import ar.edu.itba.paw.interfaces.exception.ProductNotFoundException;
import ar.edu.itba.paw.interfaces.service.*;
import ar.edu.itba.paw.interfaces.utils.UriUtils;
import ar.edu.itba.paw.models.db.*;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PoolServiceImpl implements PoolService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolServiceImpl.class);

    private final PoolDao poolDao;
    private final RequestDao requestDao;
    private final ProductService productService;
    private final LocationService locationService;
    private final RequestService requestService;
    private final EmailService emailService;

    @Autowired
    public PoolServiceImpl(final PoolDao poolDao, final RequestDao requestDao, final ProductService productService, final LocationService locationService, @Lazy final RequestService requestService, final EmailService emailService) {
        this.poolDao = poolDao;
        this.requestDao = requestDao;
        this.productService = productService;
        this.locationService = locationService;
        this.requestService = requestService;
        this.emailService = emailService;
    }

    @Transactional
    @Override
    public Pool create(final int minQuantity, final int downPayment, final String productUri, final String locationUri) {
        final int productId = UriUtils.extractIdFromUri(productUri);
        final Product product = productService.findById(productId).orElseThrow(ProductNotFoundException::new);
        final int locationId = UriUtils.extractIdFromUri(locationUri);
        final Location location = locationService.findById(locationId).orElseThrow(LocationNotFoundException::new);
        final Pool pool = poolDao.create(minQuantity, downPayment, product.getPrice(), product, location);
        LOGGER.info("Pool created: {} (product: {})", pool.getId(), productId);
        return pool;
    }

    @Transactional
    @Override
    public void edit(final int id, final Integer minQuantity, final String statusStr) {
        if (minQuantity != null)
            poolDao.edit(id, minQuantity);

        if (statusStr != null) {
            try {
                final Pool.Status status = Pool.Status.valueOf(statusStr);
                setStatus(id, status);
            } catch (Exception e) {
                throw new InvalidPoolStatusException();
            }
        }
    }

    @Transactional
    @Override
    public void cancelAvailablePoolsAndRejectRequests(final int productId) {
        requestService.rejectPendingRequests(productId);
        poolDao.cancelAvailablePools(productId);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Pool> findById(final int id) {
        return poolDao.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Paginator<Pool> filter(final Integer productId, final String search, final String status, final Integer companyId, final Double priceMin, final Double priceMax, final Integer locationId, final Integer categoryId, final int page, final String orderBy, final boolean desc) {
        if (status != null) {
            try {
                Pool.Status.valueOf(status);
            } catch (Exception e) {
                throw new InvalidPoolStatusException();
            }
        }

        return poolDao.filter(productId, search, status, companyId, priceMin, priceMax, locationId, categoryId, Math.max(page, 0), orderBy, desc);
    }

    private void setStatus(final int id, final Pool.Status status) {
        final Pool pool = poolDao.findById(id).orElseThrow(PoolNotFoundException::new);
        poolDao.setStatus(id, status);

        final Product product = pool.getProduct();
        final Company company = product.getCompany();
        final Category category = product.getCategory();
        final Location location = pool.getLocation();
        final List<List<String>> recipientsData = new ArrayList<>(pool.getRequests().stream()
                .map(p -> {
                    final User u = p.getUser();
                    return List.of(u.getEmail(), u.getPreferredLanguage());
                })
                .toList());

        final User owner = company.getOwner();
        recipientsData.add(List.of(owner.getEmail(), owner.getPreferredLanguage()));

        switch (status) {
            case DELIVERING:
                emailService.sendPoolStartsDeliveringEmail(
                        pool.getId(),
                        product.getName(),
                        company.getName(),
                        category != null ? category.getName() : "",
                        location != null ? location.getName() : "",
                        recipientsData,
                        LocaleContextHolder.getLocale()
                );
                break;

            case FINISHED:
                emailService.sendPoolFinishedEmail(
                        pool.getId(),
                        product.getName(),
                        company.getName(),
                        category != null ? category.getName() : "",
                        location != null ? location.getName() : "",
                        recipientsData,
                        LocaleContextHolder.getLocale()
                );
                break;

            case CANCELLED:
                emailService.sendPoolCancelledEmail(
                        pool.getId(),
                        product.getName(),
                        company.getName(),
                        category != null ? category.getName() : "",
                        location != null ? location.getName() : "",
                        recipientsData,
                        LocaleContextHolder.getLocale()
                );
                break;
        }

        LOGGER.info("Pool status changed: {} (new status: {})", id, status);
    }

}
