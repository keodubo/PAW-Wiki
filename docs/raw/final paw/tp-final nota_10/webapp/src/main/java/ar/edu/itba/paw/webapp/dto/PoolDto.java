package ar.edu.itba.paw.webapp.dto;

import ar.edu.itba.paw.models.db.Pool;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

public class PoolDto {

    private int id;
    private int minQuantity;
    private Pool.Status status;
    private Date createdAt;
    private int downPayment;
    private double price;
    private RequestsStatsDto requestsStats;

    private URI selfUri;
    private URI productUri;
    private URI locationUri;
    private URI requestsUri;

    public static Function<Pool, PoolDto> mapper(UriInfo uriInfo) {
        return p -> fromPool(uriInfo, p);
    }

    public static PoolDto fromPool(UriInfo uriInfo, Pool pool) {
        final PoolDto dto = new PoolDto();

        dto.id = pool.getId();
        dto.minQuantity = pool.getMinQuantity();
        dto.status = pool.getStatus();
        dto.createdAt = pool.getCreatedAt();
        dto.downPayment = pool.getDownPayment();
        dto.price = pool.getPrice();
        dto.requestsStats = new RequestsStatsDto(
                pool.getPendingRequestsCount(),
                pool.getPendingRequestsSum(),
                pool.getAcceptedRequestsCount(),
                pool.getAcceptedRequestsSum(),
                pool.getRejectedRequestsCount(),
                pool.getDeliveredRequestsCount(),
                pool.getDeliveredRequestsSum(),
                pool.getAcceptedOrDeliveredRequestsSum(),
                pool.getProductsRequestedCount()
        );

        dto.selfUri = uriInfo.getBaseUriBuilder().path("api/pools").path(String.valueOf(pool.getId())).build();
        dto.productUri = uriInfo.getBaseUriBuilder().path("api/products").path(String.valueOf(pool.getProduct().getId())).build();
        dto.locationUri = uriInfo.getBaseUriBuilder().path("api/locations").path(String.valueOf(pool.getLocation().getId())).build();
        dto.requestsUri = uriInfo.getBaseUriBuilder().path("api/requests").queryParam("pool_id", String.valueOf(pool.getId())).build();

        return dto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    public Pool.Status getStatus() {
        return status;
    }

    public void setStatus(Pool.Status status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(int downPayment) {
        this.downPayment = downPayment;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public RequestsStatsDto getRequestsStats() {
        return requestsStats;
    }

    public void setRequestsStats(RequestsStatsDto requestsStats) {
        this.requestsStats = requestsStats;
    }

    public URI getSelfUri() {
        return selfUri;
    }

    public void setSelfUri(URI selfUri) {
        this.selfUri = selfUri;
    }

    public URI getProductUri() {
        return productUri;
    }

    public void setProductUri(URI productUri) {
        this.productUri = productUri;
    }

    public URI getLocationUri() {
        return locationUri;
    }

    public void setLocationUri(URI locationUri) {
        this.locationUri = locationUri;
    }

    public URI getRequestsUri() {
        return requestsUri;
    }

    public void setRequestsUri(URI requestsUri) {
        this.requestsUri = requestsUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PoolDto poolDto = (PoolDto) o;
        return id == poolDto.id && minQuantity == poolDto.minQuantity && downPayment == poolDto.downPayment &&
                Double.compare(poolDto.price, price) == 0 && status == poolDto.status &&
                Objects.equals(createdAt, poolDto.createdAt) && Objects.equals(requestsStats, poolDto.requestsStats) &&
                Objects.equals(selfUri, poolDto.selfUri) && Objects.equals(productUri, poolDto.productUri) &&
                Objects.equals(locationUri, poolDto.locationUri) && Objects.equals(requestsUri, poolDto.requestsUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, minQuantity, status, createdAt, downPayment, price, requestsStats, selfUri, productUri, locationUri, requestsUri);
    }

}
