package ar.edu.itba.paw.webapp.form.validations;

import javax.validation.Constraint;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailDoesNotExistValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailDoesNotExist {

    String message() default "Email already exists";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};

}
