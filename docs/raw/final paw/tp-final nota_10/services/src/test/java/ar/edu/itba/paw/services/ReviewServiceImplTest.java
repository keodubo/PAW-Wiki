package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.ReviewDao;
import ar.edu.itba.paw.interfaces.exception.ProductNotFoundException;
import ar.edu.itba.paw.interfaces.exception.ReviewNotFoundException;
import ar.edu.itba.paw.interfaces.service.ProductService;
import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.db.Review;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReviewServiceImplTest {

    private static final int REVIEW_ID = 1;
    private static final int PRODUCT_ID = 10;
    private static final int USER_ID = 20;
    private static final int PAGE = 0;
    private static final String DESCRIPTION = "Great product!";
    private static final double RATING = 4.5;

    @Mock
    private ReviewDao reviewDao;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    // tests para create
    @Test
    public void testCreateSuccess() {
        User reviewer = mock(User.class);
        Product product = mock(Product.class);
        Review review = mock(Review.class);

        when(productService.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(reviewDao.create(reviewer, product, DESCRIPTION, RATING)).thenReturn(review);

        Review result = reviewService.create(reviewer, PRODUCT_ID, DESCRIPTION, RATING);

        assertNotNull(result);
        assertEquals(review, result);
        verify(productService).findById(PRODUCT_ID);
        verify(reviewDao).create(reviewer, product, DESCRIPTION, RATING);
    }

    @Test(expected = ProductNotFoundException.class)
    public void testCreateProductNotFound() {
        User reviewer = mock(User.class);

        when(productService.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        reviewService.create(reviewer, PRODUCT_ID, DESCRIPTION, RATING);
    }

    // tests para edit
    @Test
    public void testEditWithBothValuesProvided() {
        Review existingReview = mock(Review.class);
        String newDescription = "Updated description";
        double newRating = 3.0;

        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.of(existingReview));

        reviewService.edit(REVIEW_ID, newDescription, newRating);

        verify(reviewDao).edit(REVIEW_ID, newDescription, newRating);
    }

    @Test
    public void testEditWithNullDescriptionFallsBackToExisting() {
        Review existingReview = mock(Review.class);
        double newRating = 2.0;

        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.of(existingReview));
        when(existingReview.getDescription()).thenReturn(DESCRIPTION);

        reviewService.edit(REVIEW_ID, null, newRating);

        verify(reviewDao).edit(REVIEW_ID, DESCRIPTION, newRating);
    }

    @Test
    public void testEditWithNullRatingFallsBackToExisting() {
        Review existingReview = mock(Review.class);
        String newDescription = "New description";

        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.of(existingReview));
        when(existingReview.getRating()).thenReturn(RATING);

        reviewService.edit(REVIEW_ID, newDescription, null);

        verify(reviewDao).edit(REVIEW_ID, newDescription, RATING);
    }

    @Test(expected = ReviewNotFoundException.class)
    public void testEditReviewNotFound() {
        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.empty());

        reviewService.edit(REVIEW_ID, DESCRIPTION, RATING);
    }

    // tests para delete
    @Test
    public void testDelete() {
        reviewService.delete(REVIEW_ID);

        verify(reviewDao).delete(REVIEW_ID);
    }

    // tests para findById
    @Test
    public void testFindByIdExists() {
        Review review = mock(Review.class);

        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        Optional<Review> result = reviewService.findById(REVIEW_ID);

        assertTrue(result.isPresent());
        assertEquals(review, result.get());
        verify(reviewDao).findById(REVIEW_ID);
    }

    @Test
    public void testFindByIdNotExists() {
        when(reviewDao.findById(REVIEW_ID)).thenReturn(Optional.empty());

        Optional<Review> result = reviewService.findById(REVIEW_ID);

        assertFalse(result.isPresent());
        verify(reviewDao).findById(REVIEW_ID);
    }

    // tests para findByUserAndProduct
    @Test
    public void testFindByUserAndProductExists() {
        Review review = mock(Review.class);

        when(reviewDao.findByUserAndProduct(USER_ID, PRODUCT_ID)).thenReturn(Optional.of(review));

        Optional<Review> result = reviewService.findByUserAndProduct(USER_ID, PRODUCT_ID);

        assertTrue(result.isPresent());
        assertEquals(review, result.get());
        verify(reviewDao).findByUserAndProduct(USER_ID, PRODUCT_ID);
    }

    @Test
    public void testFindByUserAndProductNotExists() {
        when(reviewDao.findByUserAndProduct(USER_ID, PRODUCT_ID)).thenReturn(Optional.empty());

        Optional<Review> result = reviewService.findByUserAndProduct(USER_ID, PRODUCT_ID);

        assertFalse(result.isPresent());
        verify(reviewDao).findByUserAndProduct(USER_ID, PRODUCT_ID);
    }

    // tests para filter
    @Test
    public void testFilterWithUserId() {
        Review review1 = mock(Review.class);
        Review review2 = mock(Review.class);
        Paginator<Review> paginator = new Paginator<>(Arrays.asList(review1, review2), PAGE, 12, 2);

        when(reviewDao.filter(PRODUCT_ID, USER_ID, PAGE)).thenReturn(paginator);

        Paginator<Review> result = reviewService.filter(PRODUCT_ID, USER_ID, PAGE);

        assertNotNull(result);
        assertEquals(2, result.getList().size());
        verify(reviewDao).filter(PRODUCT_ID, USER_ID, PAGE);
    }

    @Test
    public void testFilterWithNullUserId() {
        Review review = mock(Review.class);
        Paginator<Review> paginator = new Paginator<>(Collections.singletonList(review), PAGE, 12, 1);

        when(reviewDao.filter(PRODUCT_ID, null, PAGE)).thenReturn(paginator);

        Paginator<Review> result = reviewService.filter(PRODUCT_ID, null, PAGE);

        assertNotNull(result);
        assertEquals(1, result.getList().size());
        verify(reviewDao).filter(PRODUCT_ID, null, PAGE);
    }

    @Test
    public void testFilterEmptyResult() {
        Paginator<Review> paginator = new Paginator<>();

        when(reviewDao.filter(PRODUCT_ID, USER_ID, PAGE)).thenReturn(paginator);

        Paginator<Review> result = reviewService.filter(PRODUCT_ID, USER_ID, PAGE);

        assertNotNull(result);
        assertEquals(0, result.getList().size());
        verify(reviewDao).filter(PRODUCT_ID, USER_ID, PAGE);
    }

}
