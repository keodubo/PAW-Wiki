package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.webapp.form.validations.ValidPoolStatus;

import javax.validation.constraints.Min;

public class PoolPatchForm {

    @Min(value = 1)
    private Integer minQuantity;

    @ValidPoolStatus
    private String status;

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

