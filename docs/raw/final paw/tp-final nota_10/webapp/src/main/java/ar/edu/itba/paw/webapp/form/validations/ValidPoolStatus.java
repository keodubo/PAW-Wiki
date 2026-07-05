package ar.edu.itba.paw.webapp.form.validations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidPoolStatusValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPoolStatus {

    String message() default "{ValidPoolStatus.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

