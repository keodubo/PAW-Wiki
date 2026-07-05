package ar.edu.itba.paw.webapp.form.validations;

import ar.edu.itba.paw.models.db.Pool;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidPoolStatusValidator implements ConstraintValidator<ValidPoolStatus, String> {

    @Override
    public void initialize(ValidPoolStatus constraintAnnotation) {
    }

    @Override
    public boolean isValid(String status, ConstraintValidatorContext constraintValidatorContext) {
        if (status == null || status.isEmpty())
            return true;

        try {
            Pool.Status.valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}

