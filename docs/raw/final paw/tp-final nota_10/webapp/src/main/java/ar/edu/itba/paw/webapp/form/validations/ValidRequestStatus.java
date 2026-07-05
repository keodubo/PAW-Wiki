package ar.edu.itba.paw.webapp.form.validations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidRequestStatusValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRequestStatus {

    String message() default "{ValidRequestStatus.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

