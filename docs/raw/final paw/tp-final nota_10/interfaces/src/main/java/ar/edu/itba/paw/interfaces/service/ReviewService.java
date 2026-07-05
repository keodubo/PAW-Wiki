package ar.edu.itba.paw.interfaces.service;

import ar.edu.itba.paw.models.db.Review;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Optional;

public interface ReviewService {

    Review create(final User reviewer, final int productId, final String description, final double rating);

    void edit(final int id, final String description, final Double rating);

    void delete(final int id);

    Optional<Review> findById(final int id);

    Optional<Review> findByUserAndProduct(final int userId, final int productId);

    Paginator<Review> filter(final int productId, final Integer userId, final int page);

}
