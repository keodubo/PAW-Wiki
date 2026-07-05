package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.ReviewDao;
import ar.edu.itba.paw.interfaces.exception.ProductNotFoundException;
import ar.edu.itba.paw.interfaces.exception.ReviewNotFoundException;
import ar.edu.itba.paw.interfaces.service.ProductService;
import ar.edu.itba.paw.interfaces.service.ReviewService;
import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.db.Review;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewDao reviewDao;

    private final ProductService productService;

    @Autowired
    public ReviewServiceImpl(final ReviewDao reviewDao, final ProductService productService) {
        this.reviewDao = reviewDao;
        this.productService = productService;
    }

    @Transactional
    @Override
    public Review create(final User reviewer, final int productId, final String description, final double rating) {
        final Product product = productService.findById(productId).orElseThrow(ProductNotFoundException::new);
        final Review review = reviewDao.create(reviewer, product, description, rating);
        LOGGER.info("Review created: {} (product: {}, user: {})", review.getId(), productId, reviewer.getId());
        return review;
    }

    @Transactional
    @Override
    public void edit(final int id, final String description, final Double rating) {
        final Review review = reviewDao.findById(id).orElseThrow(ReviewNotFoundException::new);
        final String finalDescription = description != null ? description : review.getDescription();
        final double finalRating = rating != null ? rating : review.getRating();
        reviewDao.edit(id, finalDescription, finalRating);
        LOGGER.info("Review edited: {}", id);
    }

    @Transactional
    @Override
    public void delete(final int id) {
        reviewDao.delete(id);
        LOGGER.info("Review deleted: {}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Review> findById(final int id) {
        return reviewDao.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Review> findByUserAndProduct(final int userId, final int productId) {
        return reviewDao.findByUserAndProduct(userId, productId);
    }

    @Transactional(readOnly = true)
    @Override
    public Paginator<Review> filter(final int productId, final Integer userId, int page) {
        return reviewDao.filter(productId, userId, page);
    }

}
