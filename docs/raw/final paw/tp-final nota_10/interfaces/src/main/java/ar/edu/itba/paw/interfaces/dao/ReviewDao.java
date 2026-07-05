package ar.edu.itba.paw.interfaces.dao;

import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.db.Review;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Optional;

public interface ReviewDao {

    Review create(final User reviewer, final Product product, final String description, final double rating);

    void edit(final int id, String description, double rating);

    void delete(final int id);

    Optional<Review> findById(final int id);

    Optional<Review> findByUserAndProduct(final int userId, final int productId);

    Paginator<Review> filter(final int productId, final Integer userId, final int page);

}
