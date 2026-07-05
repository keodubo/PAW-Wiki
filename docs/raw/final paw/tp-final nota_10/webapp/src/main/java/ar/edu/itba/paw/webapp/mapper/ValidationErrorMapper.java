package ar.edu.itba.paw.webapp.mapper;

import ar.edu.itba.paw.webapp.errors.ProblemDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.validation.ValidationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Locale;

@Component
@Provider
public class ValidationErrorMapper implements javax.ws.rs.ext.ExceptionMapper<ValidationException> {

    private final MessageSource messageSource;

    @Context
    private HttpHeaders headers;

    @Autowired
    public ValidationErrorMapper(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public Response toResponse(ValidationException exception) {
        Locale locale = getLocaleFromHeaders();
        String detail = exception.getMessage() != null ? exception.getMessage() : resolveMessage("error.validation.default", locale);
        String title = resolveMessage("error.validation.title", locale);

        ProblemDetails problemDetails = new ProblemDetails(
                "about:blank",
                title,
                400,
                detail,
                null
        );

        return Response.status(400)
                .type(MediaType.APPLICATION_JSON)
                .entity(problemDetails)
                .build();
    }

    private Locale getLocaleFromHeaders() {
        if (headers != null && headers.getAcceptableLanguages() != null && !headers.getAcceptableLanguages().isEmpty())
            return headers.getAcceptableLanguages().getFirst();
        return Locale.getDefault();
    }

    private String resolveMessage(String messageKey, Locale locale) {
        try {
            return messageSource.getMessage(messageKey, null, locale);
        } catch (Exception e) {
            return messageKey;
        }
    }
}
