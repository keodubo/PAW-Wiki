package ar.edu.itba.paw.webapp.form.validations;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

public class AllowedFiletypesValidator implements ConstraintValidator<AllowedFiletypes, FormDataBodyPart> {

    private boolean optional;
    private String[] filetypes;

    @Override
    public void initialize(AllowedFiletypes constraintAnnotation) {
        this.optional = constraintAnnotation.optional();
        this.filetypes = constraintAnnotation.filetypes();
    }

    @Override
    public boolean isValid(FormDataBodyPart formDataBodyPart, ConstraintValidatorContext constraintValidatorContext) {
        if (formDataBodyPart == null)
            return optional;

        MediaType mediaType = formDataBodyPart.getMediaType();
        if (mediaType == null)
            return false;

        String contentType = mediaType.toString();
        return Arrays.stream(filetypes).anyMatch(contentType::startsWith);
    }

}
