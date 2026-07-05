package ar.edu.itba.paw.webapp.filters;

import org.springframework.stereotype.Component;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Provider
@Component
@Priority(2)
public class ConditionalCacheFilter implements ContainerResponseFilter {

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (!"GET".equalsIgnoreCase(requestContext.getMethod()) || responseContext.getStatus() != 200 || responseContext.getEntity() == null)
            return;
        if (requestContext.getUriInfo().getPath().contains("/assets/") || requestContext.getProperty("isPublicDocument") != null)
            return;
        if (servletRequest != null && servletRequest.getAttribute("skipConditionalCache") != null)
            return;

        Object entity = responseContext.getEntity();
        String etagHash = buildEtagForObject(entity);
        EntityTag etag = new EntityTag(etagHash);

        Response.ResponseBuilder builder = requestContext.getRequest().evaluatePreconditions(etag);

        if (builder != null) {
            responseContext.setStatus(Response.Status.NOT_MODIFIED.getStatusCode());
            responseContext.setEntity(null);
            responseContext.getHeaders().remove(HttpHeaders.CONTENT_TYPE);
            responseContext.getHeaders().remove(HttpHeaders.CONTENT_LENGTH);
            responseContext.getHeaders().putSingle(HttpHeaders.ETAG, etag);
            responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, "no-transform, must-revalidate, max-age=0");
            return;
        }

        responseContext.getHeaders().add(HttpHeaders.ETAG, etag);
        responseContext.getHeaders().add(HttpHeaders.CACHE_CONTROL, "no-transform, must-revalidate, max-age=0");
    }

    private String buildEtagForObject(Object item) {
        String serialized = String.valueOf(item.hashCode());
        return generateEtagHash(serialized);
    }

    private String generateEtagHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating ETag", e);
        }
    }

}
