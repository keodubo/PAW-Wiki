package ar.edu.itba.paw.webapp.dto;

import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.Request;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

public class RequestDto {

    private int id;
    private Request.Status status;
    private int quantity;
    private double total;

    private URI selfUri;
    private URI downPaymentUri;
    private URI finalPaymentUri;
    private URI userUri;
    private URI poolUri;

    public static Function<Request, RequestDto> mapper(UriInfo uriInfo) {
        return r -> fromRequest(uriInfo, r);
    }

    public static RequestDto fromRequest(UriInfo uriInfo, Request request) {
        final RequestDto dto = new RequestDto();

        dto.id = request.getId();
        dto.status = request.getStatus();
        dto.quantity = request.getQuantity();
        dto.total = request.getTotal();

        dto.selfUri = uriInfo.getBaseUriBuilder().path("api/requests").path(String.valueOf(request.getId())).build();

        Document downPayment = request.getDownPayment();
        if (downPayment != null)
            dto.downPaymentUri = uriInfo.getBaseUriBuilder().path("api/documents").path(String.valueOf(downPayment.getId())).build();

        Document finalPayment = request.getFinalPayment();
        if (finalPayment != null)
            dto.finalPaymentUri = uriInfo.getBaseUriBuilder().path("api/documents").path(String.valueOf(finalPayment.getId())).build();

        dto.userUri = uriInfo.getBaseUriBuilder().path("api/users").path(String.valueOf(request.getUser().getId())).build();
        dto.poolUri = uriInfo.getBaseUriBuilder().path("api/pools").path(String.valueOf(request.getPool().getId())).build();

        return dto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Request.Status getStatus() {
        return status;
    }

    public void setStatus(Request.Status status) {
        this.status = status;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public URI getSelfUri() {
        return selfUri;
    }

    public void setSelfUri(URI selfUri) {
        this.selfUri = selfUri;
    }

    public URI getDownPaymentUri() {
        return downPaymentUri;
    }

    public void setDownPaymentUri(URI downPaymentUri) {
        this.downPaymentUri = downPaymentUri;
    }

    public URI getFinalPaymentUri() {
        return finalPaymentUri;
    }

    public void setFinalPaymentUri(URI finalPaymentUri) {
        this.finalPaymentUri = finalPaymentUri;
    }

    public URI getUserUri() {
        return userUri;
    }

    public void setUserUri(URI userUri) {
        this.userUri = userUri;
    }

    public URI getPoolUri() {
        return poolUri;
    }

    public void setPoolUri(URI poolUri) {
        this.poolUri = poolUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestDto that = (RequestDto) o;
        return id == that.id && quantity == that.quantity && status == that.status &&
                Objects.equals(total, that.total) && Objects.equals(selfUri, that.selfUri) &&
                Objects.equals(downPaymentUri, that.downPaymentUri) &&
                Objects.equals(finalPaymentUri, that.finalPaymentUri) && Objects.equals(userUri, that.userUri) &&
                Objects.equals(poolUri, that.poolUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, quantity, total, selfUri, downPaymentUri, finalPaymentUri, userUri, poolUri);
    }

}
