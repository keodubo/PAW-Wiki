package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.webapp.form.validations.ValidRequestStatus;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

public class RequestPatchForm {

    @Min(value = 1)
    private Integer quantity;

    @ValidRequestStatus
    private String status;

    @Pattern(regexp = "^.*/api/documents/\\d+$")
    private String downPaymentUri;

    @Pattern(regexp = "^.*/api/documents/\\d+$")
    private String finalPaymentUri;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDownPaymentUri() {
        return downPaymentUri;
    }

    public void setDownPaymentUri(String downPaymentUri) {
        this.downPaymentUri = downPaymentUri;
    }

    public String getFinalPaymentUri() {
        return finalPaymentUri;
    }

    public void setFinalPaymentUri(String finalPaymentUri) {
        this.finalPaymentUri = finalPaymentUri;
    }
}

