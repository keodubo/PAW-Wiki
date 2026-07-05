package ar.edu.itba.paw.webapp.servlet.filters;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StaticAssetsCacheFilter extends OncePerRequestFilter {

    private static final int MAX_AGE_SECONDS = 31536000; // 1 año

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        if ("GET".equalsIgnoreCase(request.getMethod()) && isStaticAsset(path)) {
            response.setHeader("Cache-Control", "public, max-age=" + MAX_AGE_SECONDS + ", immutable");
            if (response.containsHeader("ETag"))
                response.setHeader("ETag", "");
        }

        filterChain.doFilter(request, response);
    }

    private boolean isStaticAsset(String path) {
        return path.contains("/assets/") ||
                path.contains("/static/") ||
                path.equals("/favicon.ico");
    }

}

