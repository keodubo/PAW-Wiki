package ar.edu.itba.paw.webapp.auth.filters;

import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.auth.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(2)
public class BearerAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    public BearerAuthorizationFilter(
            final JwtService jwtService,
            final UserDetailsService userDetailService,
            final AuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtService = jwtService;
        this.userDetailService = userDetailService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                validateAndSetAuthentication(token, response, request);
            } catch (JwtException e) {
                LOGGER.error("JWT validation failed", e);
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(request, response, new BadCredentialsException("Invalid authentication token", e));
                return;
            } catch (Exception e) {
                LOGGER.error("Authentication error", e);
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private void validateAndSetAuthentication(final String token, final HttpServletResponse response, final HttpServletRequest request) {
        Claims accessClaims;
        try {
            accessClaims = jwtService.verifyAccessToken(token);
        } catch (Exception e1) {
            try {
                Claims refreshClaims = jwtService.verifyRefreshToken(token);
                AuthUser userDetails = (AuthUser) userDetailService.loadUserByUsername(refreshClaims.getSubject());

                String newAccessToken = jwtService.generateAccessToken(userDetails);
                String newRefreshToken = jwtService.generateRefreshToken(userDetails);

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                );

                response.setHeader("X-Grupi-Access-Token", newAccessToken);
                response.setHeader("X-Grupi-Refresh-Token", newRefreshToken);
                response.setHeader("X-Grupi-User-Id", String.valueOf(userDetails.getUserId()));
                request.setAttribute("skipConditionalCache", true);
                LOGGER.info("Token refreshed for user: {}", userDetails.getUsername());
                return;
            } catch (Exception e2) {
                throw new JwtException("Invalid token");
            }
        }

        AuthUser userDetails = (AuthUser) userDetailService.loadUserByUsername(accessClaims.getSubject());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

}