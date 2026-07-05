package ar.edu.itba.paw.interfaces.dao;

import ar.edu.itba.paw.models.db.Category;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Optional;

public interface ProductDao {

    Product create(final String name, final String description, final double price, final Document image, final Company company, final Category category);

    void edit(final int id, final String name, final String description, final double price, final Document image, final Category category);

    void retire(final int id);

    Optional<Product> findById(final int id);

    Paginator<Product> filter(final String search, final Integer categoryId, final Integer companyId, final Double priceMin, final Double priceMax, final Boolean active, final int page, String orderBy, boolean desc);

}
