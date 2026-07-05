package ar.edu.itba.paw.webapp.dto;

import ar.edu.itba.paw.models.db.Product;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

public class ProductDto {

    private int id;
    private String name;
    private String description;
    private double price;
    private boolean active;
    private double rating;
    private RatingsDto ratings;
    private boolean canRetire;

    private URI selfUri;
    private URI imageUri;
    private URI companyUri;
    private URI categoryUri;
    private URI poolsUri;
    private URI reviewsUri;

    public static Function<Product, ProductDto> mapper(UriInfo uriInfo) {
        return p -> fromProduct(uriInfo, p);
    }

    public static ProductDto fromProduct(UriInfo uriInfo, Product product) {
        final ProductDto dto = new ProductDto();

        dto.id = product.getId();
        dto.name = product.getName();
        dto.description = product.getDescription();
        dto.price = product.getPrice();
        dto.active = product.getActive();
        dto.rating = product.getRating();
        dto.ratings = new RatingsDto(
                product.getOneStar(),
                product.getTwoStars(),
                product.getThreeStars(),
                product.getFourStars(),
                product.getFiveStars()
        );
        dto.canRetire = product.getCanRetire();

        dto.selfUri = uriInfo.getBaseUriBuilder().path("api/products").path(String.valueOf(product.getId())).build();
        dto.imageUri = uriInfo.getBaseUriBuilder().path("api/documents").path(String.valueOf(product.getImage().getId())).build();
        dto.companyUri = uriInfo.getBaseUriBuilder().path("api/companies").path(String.valueOf(product.getCompany().getId())).build();
        dto.categoryUri = uriInfo.getBaseUriBuilder().path("api/categories").path(String.valueOf(product.getCategory().getId())).build();
        dto.poolsUri = uriInfo.getBaseUriBuilder().path("api/pools").queryParam("product_id", String.valueOf(product.getId())).build();
        dto.reviewsUri = uriInfo.getBaseUriBuilder().path("api/products").path(String.valueOf(product.getId())).path("reviews").build();

        return dto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public RatingsDto getRatings() {
        return ratings;
    }

    public void setRatings(RatingsDto ratings) {
        this.ratings = ratings;
    }

    public boolean getCanRetire() {
        return canRetire;
    }

    public void setCanRetire(boolean canRetire) {
        this.canRetire = canRetire;
    }

    public URI getSelfUri() {
        return selfUri;
    }

    public void setSelfUri(URI selfUri) {
        this.selfUri = selfUri;
    }

    public URI getImageUri() {
        return imageUri;
    }

    public void setImageUri(URI imageUri) {
        this.imageUri = imageUri;
    }

    public URI getCompanyUri() {
        return companyUri;
    }

    public void setCompanyUri(URI companyUri) {
        this.companyUri = companyUri;
    }

    public URI getCategoryUri() {
        return categoryUri;
    }

    public void setCategoryUri(URI categoryUri) {
        this.categoryUri = categoryUri;
    }

    public URI getPoolsUri() {
        return poolsUri;
    }

    public void setPoolsUri(URI poolsUri) {
        this.poolsUri = poolsUri;
    }

    public URI getReviewsUri() {
        return reviewsUri;
    }

    public void setReviewsUri(URI reviewsUri) {
        this.reviewsUri = reviewsUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDto that = (ProductDto) o;
        return id == that.id && Double.compare(that.price, price) == 0 && active == that.active &&
                Double.compare(that.rating, rating) == 0 && canRetire == that.canRetire &&
                Objects.equals(name, that.name) && Objects.equals(description, that.description) &&
                Objects.equals(ratings, that.ratings) && Objects.equals(selfUri, that.selfUri) &&
                Objects.equals(imageUri, that.imageUri) && Objects.equals(companyUri, that.companyUri) &&
                Objects.equals(categoryUri, that.categoryUri) && Objects.equals(poolsUri, that.poolsUri) &&
                Objects.equals(reviewsUri, that.reviewsUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, price, active, rating, ratings, canRetire, selfUri, imageUri, companyUri, categoryUri, poolsUri, reviewsUri);
    }

}
