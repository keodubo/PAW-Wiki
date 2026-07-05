package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class RequestCreateForm {

    @NotNull
    @Pattern(regexp = "^.*/api/pools/\\d+$")
    private String poolUri;

    @NotNull
    @Min(value = 1)
    private Integer quantity;

    public String getPoolUri() {
        return poolUri;
    }

    public void setPoolUri(final String poolUri) {
        this.poolUri = poolUri;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}