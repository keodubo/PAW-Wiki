package ar.edu.itba.paw.webapp.dto;

import java.util.Objects;

public class RequestsStatsDto {

    private int pendingCount;
    private int pendingSum;
    private int acceptedCount;
    private int acceptedSum;
    private int rejectedCount;
    private int deliveredCount;
    private int deliveredSum;
    private int acceptedOrDeliveredSum;
    private int productsCount;

    public RequestsStatsDto() {
    }

    public RequestsStatsDto(int pendingCount, int pendingSum, int acceptedCount, int acceptedSum, int rejectedCount, int deliveredCount, int deliveredSum, int acceptedOrDeliveredSum, int productsCount) {
        this.pendingCount = pendingCount;
        this.pendingSum = pendingSum;
        this.acceptedCount = acceptedCount;
        this.acceptedSum = acceptedSum;
        this.rejectedCount = rejectedCount;
        this.deliveredCount = deliveredCount;
        this.deliveredSum = deliveredSum;
        this.acceptedOrDeliveredSum = acceptedOrDeliveredSum;
        this.productsCount = productsCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public int getPendingSum() {
        return pendingSum;
    }

    public void setPendingSum(int pendingSum) {
        this.pendingSum = pendingSum;
    }

    public int getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(int acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public int getAcceptedSum() {
        return acceptedSum;
    }

    public void setAcceptedSum(int acceptedSum) {
        this.acceptedSum = acceptedSum;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public int getDeliveredCount() {
        return deliveredCount;
    }

    public void setDeliveredCount(int deliveredCount) {
        this.deliveredCount = deliveredCount;
    }

    public int getDeliveredSum() {
        return deliveredSum;
    }

    public void setDeliveredSum(int deliveredSum) {
        this.deliveredSum = deliveredSum;
    }

    public int getAcceptedOrDeliveredSum() {
        return acceptedOrDeliveredSum;
    }

    public void setAcceptedOrDeliveredSum(int acceptedOrDeliveredSum) {
        this.acceptedOrDeliveredSum = acceptedOrDeliveredSum;
    }

    public int getProductsCount() {
        return productsCount;
    }

    public void setProductsCount(int productsCount) {
        this.productsCount = productsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestsStatsDto that = (RequestsStatsDto) o;
        return pendingCount == that.pendingCount && pendingSum == that.pendingSum &&
                acceptedCount == that.acceptedCount && acceptedSum == that.acceptedSum &&
                rejectedCount == that.rejectedCount && deliveredCount == that.deliveredCount &&
                deliveredSum == that.deliveredSum && acceptedOrDeliveredSum == that.acceptedOrDeliveredSum &&
                productsCount == that.productsCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pendingCount, pendingSum, acceptedCount, acceptedSum, rejectedCount, deliveredCount, deliveredSum, acceptedOrDeliveredSum, productsCount);
    }
}

