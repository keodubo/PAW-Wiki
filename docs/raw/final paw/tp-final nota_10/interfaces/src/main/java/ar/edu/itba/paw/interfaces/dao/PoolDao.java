package ar.edu.itba.paw.interfaces.dao;

import ar.edu.itba.paw.models.db.Location;
import ar.edu.itba.paw.models.db.Pool;
import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Optional;

public interface PoolDao {

    Pool create(final int minQuantity, final int downPayment, final double price, final Product product, final Location location);

    void edit(final int id, final int minQuantity);

    void setStatus(int id, Pool.Status status);

    void cancelAvailablePools(final int productId);

    Optional<Pool> findById(final int id);

    Paginator<Pool> filter(final Integer productId, final String search, final String statusStr, final Integer companyId, final Double priceMin, final Double priceMax, final Integer locationId, final Integer categoryId, final int page, final String orderBy, final boolean desc);

}
