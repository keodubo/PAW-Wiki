package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.interfaces.exception.ReviewNotFoundException;
import ar.edu.itba.paw.interfaces.exception.UserNotFoundException;
import ar.edu.itba.paw.interfaces.service.ReviewService;
import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.models.db.Review;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.webapp.CustomMediaType;
import ar.edu.itba.paw.webapp.dto.ReviewDto;
import ar.edu.itba.paw.webapp.form.ReviewCreateForm;
import ar.edu.itba.paw.webapp.form.ReviewPatchForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/api/products/{product_id:\\d+}/reviews")
@Component
public class ReviewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewController.class);

    @Context
    private UriInfo uriInfo;

    private final ReviewService reviewService;
    private final UserService userService;

    @Autowired
    public ReviewController(final ReviewService reviewService, final UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GET
    @Produces(CustomMediaType.APPLICATION_REVIEW_JSON)
    public Response listReviews(
            @PathParam("product_id") final int productId,
            @QueryParam("user_id") final Integer userId,
            @QueryParam("page") @DefaultValue("0") final int page
    ) {

        Paginator<Review> paginator = reviewService.filter(productId, userId, page);
        List<ReviewDto> reviews = paginator.getList().stream().map(ReviewDto.mapper(uriInfo, productId)).toList();
        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
        Response.ResponseBuilder r = Response.ok(new GenericEntity<>(reviews) {
                })
                .header("X-Total-Count", String.valueOf(paginator.getTotalItems()))
                .link(uriBuilder.clone().replaceQueryParam("page", 0).build(), "first")
                .link(uriBuilder.clone().replaceQueryParam("page", paginator.getLastPage()).build(), "last");

        if (paginator.getPage() > 0)
            r.link(uriBuilder.clone().replaceQueryParam("page", paginator.getPage() - 1).build(), "prev");
        if (paginator.getPage() < paginator.getLastPage())
            r.link(uriBuilder.clone().replaceQueryParam("page", paginator.getPage() + 1).build(), "next");

        return r.build();
    }

    @POST
    @Consumes(CustomMediaType.APPLICATION_REVIEW_JSON)
    public Response createReview(@PathParam("product_id") final int productId, @Valid @NotNull final ReviewCreateForm form) {
        final User user = userService.getCurrentUser().orElseThrow(UserNotFoundException::new);
        final Review review = reviewService.create(user, productId, form.getDescription(), form.getRating());
        LOGGER.info("Review created: {}", review.getId());
        return Response.created(
                uriInfo.getBaseUriBuilder()
                        .path("api/products")
                        .path(String.valueOf(productId))
                        .path("reviews")
                        .path(String.valueOf(review.getId()))
                        .build()
        ).build();
    }

    @PATCH
    @Path("/{review_id:\\d+}")
    @Consumes(CustomMediaType.APPLICATION_REVIEW_JSON)
    public Response updateReview(@PathParam("product_id") final int productId, @PathParam("review_id") final int reviewId, @Valid @NotNull final ReviewPatchForm form) {
        reviewService.edit(reviewId, form.getDescription(), form.getRating());
        LOGGER.info("Review edited: {}", reviewId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{review_id:\\d+}")
    public Response deleteReview(@PathParam("product_id") final int productId, @PathParam("review_id") final int reviewId) {
        reviewService.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
        reviewService.delete(reviewId);
        LOGGER.info("Review deleted: {}", reviewId);
        return Response.noContent().build();
    }

}
