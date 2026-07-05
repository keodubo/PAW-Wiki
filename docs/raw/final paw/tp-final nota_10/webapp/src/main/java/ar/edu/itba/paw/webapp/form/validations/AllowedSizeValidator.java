package ar.edu.itba.paw.webapp.form.validations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AllowedSizeValidator implements ConstraintValidator<AllowedSize, byte[]> {

    private long maxSizeInBytes;

    @Override
    public void initialize(AllowedSize constraintAnnotation) {
        this.maxSizeInBytes = constraintAnnotation.maxSizeInBytes();
    }

    @Override
    public boolean isValid(byte[] bytes, ConstraintValidatorContext constraintValidatorContext) {
        if (bytes == null)
            return false;
        return bytes.length <= maxSizeInBytes;
    }

}
