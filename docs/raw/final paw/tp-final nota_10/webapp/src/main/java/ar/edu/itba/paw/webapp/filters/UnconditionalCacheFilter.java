package ar.edu.itba.paw.webapp.filters;

import org.springframework.stereotype.Component;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Component
@Priority(1)
public class UnconditionalCacheFilter implements ContainerResponseFilter {

    private static final int MAX_AGE_SECONDS = 31536000; // 1 año

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (!"GET".equalsIgnoreCase(requestContext.getMethod()) || responseContext.getStatus() != 200)
            return;

        Boolean isPublic;
        if ((isPublic = (Boolean) requestContext.getProperty("isPublicDocument")) != null) {
            if (isPublic)
                setUnconditionalPublicCache(responseContext);
            else
                setUnconditionalPrivateCache(responseContext);
        }
    }

    private void setUnconditionalPublicCache(ContainerResponseContext responseContext) {
        responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL,
                "public, max-age=" + MAX_AGE_SECONDS + ", immutable");
        responseContext.getHeaders().remove(HttpHeaders.ETAG);
    }

    private void setUnconditionalPrivateCache(ContainerResponseContext responseContext) {
        responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL,
                "private, max-age=" + MAX_AGE_SECONDS + ", immutable");
        responseContext.getHeaders().remove(HttpHeaders.ETAG);
    }
}