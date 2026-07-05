package ar.edu.itba.paw.webapp.auth.filters;

import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.auth.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Optional;

@Component
@Order(1)
public class BasicAuthorizationFilter extends OncePerRequestFilter {

    Logger LOGGER = LoggerFactory.getLogger(BasicAuthorizationFilter.class);

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailService;
    private final UserService userService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    public BasicAuthorizationFilter(
            final JwtService jwtService,
            final AuthenticationManager authenticationManager,
            final UserDetailsService userDetailService,
            final UserService userService,
            final AuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailService = userDetailService;
        this.userService = userService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            final String intentHeader = request.getHeader(UserIntentFilter.ACTION_HEADER);
            if (UserIntentFilter.ACTION_RECOVER_PASSWORD.equalsIgnoreCase(intentHeader)) {
                // El recover password ya lo manejó el UserIntentFilter
                filterChain.doFilter(request, response);
                return;
            }

            try {
                handleBasicAuth(authHeader, request, response);
            } catch (BadCredentialsException e) {
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(request, response, e);
                return;
            } catch (Exception e) {
                LOGGER.error("Error during authentication", e);
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private void handleBasicAuth(final String authHeader, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String base64Credentials = authHeader.substring(6);
        String credentials = new String(Base64.getDecoder().decode(base64Credentials));
        String[] values = credentials.split(":", 2);

        final Optional<User> userOptional = userService.findByEmail(values[0]);
        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            AuthUser userDetails = (AuthUser) userDetailService.loadUserByUsername(user.getEmail());

            if (user.getValidationToken() != null || user.getPasswordToken() != null) {
                final ArrayList<TokenPair> tokens = new ArrayList<>();

                if (user.getValidationToken() != null) {
                    String[] split = user.getValidationToken().split(":");
                    tokens.add(new TokenPair(split[0], Instant.ofEpochMilli(Long.parseLong(split[1]))));
                }
                if (user.getPasswordToken() != null) {
                    String[] split = user.getPasswordToken().split(":");
                    tokens.add(new TokenPair(split[0], Instant.ofEpochMilli(Long.parseLong(split[1]))));
                }

                for (TokenPair pair : tokens) {
                    if (pair.token.equals(values[1]) && pair.expirationTime.isAfter(Instant.now())) {
                        userService.validateAccount(user.getId());
                        userDetails = (AuthUser) userDetailService.loadUserByUsername(user.getEmail());
                        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        addTokensToResponse(response, userDetails);
                        response.addHeader("X-Grupi-Account-Validated", "true");
                        return;
                    }
                }

                try {
                    final Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(values[0], values[1])
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    if (!user.isValidated()) {
                        response.addHeader("X-Grupi-Account-Validated", "false");
                    }

                    addTokensToResponse(response, userDetails);
                    return;
                } catch (Exception e) {
                    throw new BadCredentialsException("Invalid credentials");
                }
            } else {
                final Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(values[0], values[1])
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                if (!user.isValidated()) {
                    response.addHeader("X-Grupi-Account-Validated", "false");
                }
            }

            addTokensToResponse(response, userDetails);
        } else {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    private void addTokensToResponse(final HttpServletResponse response, final AuthUser userDetails) {
        final String accessToken = jwtService.generateAccessToken(userDetails);
        final String refreshToken = jwtService.generateRefreshToken(userDetails);

        response.addHeader("X-Grupi-Access-Token", accessToken);
        response.addHeader("X-Grupi-Refresh-Token", refreshToken);
        response.addHeader("X-Grupi-User-Id", String.valueOf(userDetails.getUserId()));
    }

    static class TokenPair {

        public final String token;
        public final Instant expirationTime;

        public TokenPair(String token, Instant expirationTime) {
            this.token = token;
            this.expirationTime = expirationTime;
        }

    }

}
