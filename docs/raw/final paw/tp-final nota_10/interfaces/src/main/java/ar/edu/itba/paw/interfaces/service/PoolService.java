package ar.edu.itba.paw.interfaces.service;

import ar.edu.itba.paw.models.db.Pool;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Optional;

public interface PoolService {

    Pool create(final int minQuantity, final int downPayment, final String productUri, final String locationUri);

    void edit(final int id, final Integer minQuantity, final String statusStr);

    void cancelAvailablePoolsAndRejectRequests(final int productId);

    Optional<Pool> findById(final int id);

    Paginator<Pool> filter(final Integer productId, final String search, final String statusStr, final Integer companyId, final Double priceMin, final Double priceMax, final Integer locationId, final Integer categoryId, final int page, final String orderBy, final boolean desc);

}
