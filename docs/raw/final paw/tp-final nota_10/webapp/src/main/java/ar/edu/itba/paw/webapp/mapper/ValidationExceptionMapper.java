package ar.edu.itba.paw.webapp.mapper;

import ar.edu.itba.paw.webapp.errors.ProblemDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@Provider
public class ValidationExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    private final MessageSource messageSource;

    @Context
    private HttpHeaders headers;

    @Autowired
    public ValidationExceptionMapper(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        LOGGER.warn("Validation exception: {}", exception.getMessage());
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        List<String> errorMessages = new ArrayList<>();
        Locale locale = getLocaleFromHeaders();

        for (ConstraintViolation<?> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String messageKey = getMessageKey(violation);
            String resolvedMessage = resolveMessage(messageKey, locale);

            if (propertyPath != null && !propertyPath.isEmpty()) {
                errorMessages.add(String.format("%s: %s", propertyPath, resolvedMessage));
            } else {
                errorMessages.add(resolvedMessage);
            }
        }

        String detail = String.join(";", errorMessages);
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

    private String getMessageKey(ConstraintViolation<?> violation) {
        try {
            return violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        } catch (Exception e) {
            String messageTemplate = violation.getMessageTemplate();
            if (messageTemplate != null && !messageTemplate.startsWith("{") && !messageTemplate.endsWith("}"))
                return messageTemplate;
            return violation.getMessage();
        }
    }

    private String resolveMessage(String messageKey, Locale locale) {
        try {
            return messageSource.getMessage(messageKey, null, locale);
        } catch (Exception e) {
            return messageKey;
        }
    }
}
