package ar.edu.itba.paw.webapp.form.validations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AllowedSizeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedSize {

    String message() default "Document size exceeds the allowed limit";

    long maxSizeInBytes() default 10 * 1024 * 1024; // 10MB default

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
