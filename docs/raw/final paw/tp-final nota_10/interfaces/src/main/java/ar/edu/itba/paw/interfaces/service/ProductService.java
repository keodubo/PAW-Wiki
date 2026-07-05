package ar.edu.itba.paw.interfaces.service;

import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Optional;

public interface ProductService {

    Product create(final String name, final String description, final double price, final String imageUri, final Company company, final String categoryUri);

    void edit(final int id, final String name, final String description, final Double price, final String imageUri, final String categoryUri);

    void retire(final int id);

    Optional<Product> findById(final int id);

    Paginator<Product> filter(final String search, final Integer categoryId, final Integer companyId, final Double priceMin, final Double priceMax, final Boolean active, final int page, final String orderBy, final boolean desc);

}
