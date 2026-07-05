package ar.edu.itba.paw.webapp.mapper;

import ar.edu.itba.paw.interfaces.exception.base.CustomRuntimeException;
import ar.edu.itba.paw.webapp.errors.ProblemDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Locale;

@Component
@Provider
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Throwable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionMapper.class);

    private final MessageSource messageSource;

    @Context
    private HttpHeaders headers;

    @Autowired
    public ExceptionMapper(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.error("Exception caught: {}", exception.getClass().getSimpleName(), exception);
        int status = 500;
        String titleKey = "error.internal.title";
        String messageKey = exception.getMessage() != null ? exception.getMessage() : "error.unexpected";
        String type = "about:blank";
        String instance = null;

        switch (exception) {
            case WebApplicationException webApplicationException -> {
                status = webApplicationException.getResponse().getStatus();
                titleKey = "error.webapplication.title";
            }
            case CustomRuntimeException customRuntimeException -> {
                status = customRuntimeException.getStatusCode();
                titleKey = "error.custom.title";
            }
            case AccessDeniedException accessDeniedException -> {
                status = 403;
                titleKey = "error.forbidden.title";
                messageKey = "error.forbidden.detail";
            }
            default -> {
            }
        }

        Locale locale = getLocaleFromHeaders();
        String title = resolveMessage(titleKey, locale);
        String detail = resolveMessage(messageKey, locale);

        ProblemDetails details = new ProblemDetails(type, title, status, detail, instance);
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(details)
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
