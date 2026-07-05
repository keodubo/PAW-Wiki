package ar.edu.itba.paw.webapp.form.validations;

import ar.edu.itba.paw.models.db.Request;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidRequestStatusValidator implements ConstraintValidator<ValidRequestStatus, String> {

    @Override
    public void initialize(ValidRequestStatus constraintAnnotation) {
    }

    @Override
    public boolean isValid(String status, ConstraintValidatorContext constraintValidatorContext) {
        if (status == null || status.isEmpty())
            return true;

        try {
            Request.Status.valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}

