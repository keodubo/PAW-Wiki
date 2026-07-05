package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class PoolCreateForm {

    @NotNull
    @Pattern(regexp = "^.*/api/products/\\d+$")
    private String productUri;

    @NotNull
    @Pattern(regexp = "^.*/api/locations/\\d+$")
    private String locationUri;

    @NotNull
    @Min(value = 1)
    private Integer minQuantity;

    @NotNull
    @Min(value = 0)
    @Max(value = 100)
    private Integer downPayment;

    public String getProductUri() {
        return productUri;
    }

    public void setProductUri(final String productUri) {
        this.productUri = productUri;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(final Integer minQuantity) {
        this.minQuantity = minQuantity;
    }

    public Integer getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(final Integer downPayment) {
        this.downPayment = downPayment;
    }

    public String getLocationUri() {
        return locationUri;
    }

    public void setLocationUri(final String locationUri) {
        this.locationUri = locationUri;
    }

}