package ar.edu.itba.paw.webapp.dto;

import ar.edu.itba.paw.models.db.Review;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

public class ReviewDto {

    private int id;
    private double rating;
    private String description;
    private Date createdAt;

    private URI selfUri;
    private URI reviewerUri;
    private URI productUri;

    public static Function<Review, ReviewDto> mapper(UriInfo uriInfo, int productId) {
        return r -> fromReview(uriInfo, productId, r);
    }

    public static ReviewDto fromReview(UriInfo uriInfo, int productId, Review review) {
        final ReviewDto dto = new ReviewDto();

        dto.id = review.getId();
        dto.rating = review.getRating();
        dto.description = review.getDescription();
        dto.createdAt = review.getCreatedAt();

        dto.selfUri = uriInfo.getBaseUriBuilder().path("api/products").path(String.valueOf(productId)).path("reviews").path(String.valueOf(review.getId())).build();
        dto.reviewerUri = uriInfo.getBaseUriBuilder().path("api/users").path(String.valueOf(review.getReviewer().getId())).build();
        dto.productUri = uriInfo.getBaseUriBuilder().path("api/products").path(String.valueOf(review.getProduct().getId())).build();

        return dto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public URI getSelfUri() {
        return selfUri;
    }

    public void setSelfUri(URI selfUri) {
        this.selfUri = selfUri;
    }

    public URI getReviewerUri() {
        return reviewerUri;
    }

    public void setReviewerUri(URI reviewerUri) {
        this.reviewerUri = reviewerUri;
    }

    public URI getProductUri() {
        return productUri;
    }

    public void setProductUri(URI productUri) {
        this.productUri = productUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewDto reviewDto = (ReviewDto) o;
        return id == reviewDto.id && Double.compare(reviewDto.rating, rating) == 0 &&
                Objects.equals(description, reviewDto.description) && Objects.equals(createdAt, reviewDto.createdAt) &&
                Objects.equals(selfUri, reviewDto.selfUri) && Objects.equals(reviewerUri, reviewDto.reviewerUri) &&
                Objects.equals(productUri, reviewDto.productUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rating, description, createdAt, selfUri, reviewerUri, productUri);
    }

}
