package ar.edu.itba.paw.webapp.form.validations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AllowedFiletypesValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedFiletypes {

    String message() default "File type is not allowed";

    String[] filetypes() default {"image/jpeg", "image/jpg", "image/png", "application/pdf"};

    boolean optional() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
