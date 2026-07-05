package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.webapp.auth.filters.BasicAuthorizationFilter;
import ar.edu.itba.paw.webapp.auth.filters.BearerAuthorizationFilter;
import ar.edu.itba.paw.webapp.auth.filters.UserIntentFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan("ar.edu.itba.paw.webapp.auth")
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetails;

    @Autowired
    private BasicAuthorizationFilter basicAuthorizationFilter;

    @Autowired
    private BearerAuthorizationFilter bearerAuthorizationFilter;

    @Autowired
    private UserIntentFilter userIntentFilter;

    @Qualifier("corsBasePath")
    private final String corsBasePath;

    @Autowired
    public SecurityConfig(final String corsBasePath) {
        this.corsBasePath = corsBasePath;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetails).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().authorizeRequests()

                .antMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .antMatchers(HttpMethod.HEAD, "/api/users").authenticated()
                .antMatchers(HttpMethod.POST, "/api/users").permitAll()
                .antMatchers(HttpMethod.PATCH, "/api/users").permitAll()
                .antMatchers(HttpMethod.GET, "/api/users/{user_id:\\d+}").permitAll()
                .antMatchers(HttpMethod.PATCH, "/api/users/{user_id:\\d+}").access("hasRole('ADMIN') or (authenticated and @accessValidator.isUserSelf(principal, #user_id))")

                .antMatchers(HttpMethod.GET, "/api/companies/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/companies").hasRole("COMPANY_PENDING")
                .antMatchers(HttpMethod.PATCH, "/api/companies/{company_id:\\d+}").access("hasRole('ADMIN') or ((hasRole('COMPANY_PENDING') or hasRole('COMPANY_UNVERIFIED') or hasRole('COMPANY_VERIFIED')) and @accessValidator.isCompanyOwner(principal, #company_id))")

                .antMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/products").hasRole("COMPANY_VERIFIED")
                .antMatchers(HttpMethod.PATCH, "/api/products/{product_id:\\d+}").access("hasRole('COMPANY_VERIFIED') and @accessValidator.isProductOwnerId(principal, #product_id)")

                .antMatchers(HttpMethod.GET, "/api/pools/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/pools").hasRole("COMPANY_VERIFIED")
                .antMatchers(HttpMethod.PATCH, "/api/pools/{pool_id:\\d+}").access("hasRole('COMPANY_VERIFIED') and @accessValidator.isPoolOwner(principal, #pool_id)")

                .antMatchers(HttpMethod.GET, "/api/requests").access("hasRole('COMPANY_VERIFIED') or hasRole('CLIENT')")
                .antMatchers(HttpMethod.POST, "/api/requests").hasRole("CLIENT")
                .antMatchers(HttpMethod.PATCH, "/api/requests/{request_id:\\d+}").access("(hasRole('COMPANY_VERIFIED') and @accessValidator.isRequestPoolOwner(principal, #request_id)) or (hasRole('CLIENT') and @accessValidator.isRequestOwner(principal, #request_id))")
                .antMatchers(HttpMethod.DELETE, "/api/requests/{request_id:\\d+}").access("hasRole('CLIENT') and @accessValidator.isRequestOwner(principal, #request_id) and @accessValidator.canDeleteRequest(principal, #request_id)")

                .antMatchers(HttpMethod.GET, "/api/products/{product_id:\\d+}/reviews").permitAll()
                .antMatchers(HttpMethod.POST, "/api/products/{product_id:\\d+}/reviews").access("hasRole('CLIENT') and @accessValidator.hasInteractedWithProduct(principal, #product_id) and @accessValidator.hasNotReviewedProduct(principal, #product_id)")
                .antMatchers(HttpMethod.PATCH, "/api/products/{product_id:\\d+}/reviews/{review_id:\\d+}").access("hasRole('CLIENT') and @accessValidator.isReviewOwner(principal, #review_id)")
                .antMatchers(HttpMethod.DELETE, "/api/products/{product_id:\\d+}/reviews/{review_id:\\d+}").access("hasRole('CLIENT') and @accessValidator.isReviewOwner(principal, #review_id)")

                .antMatchers(HttpMethod.GET, "/api/users/{user_id:\\d+}/reports").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/users/{user_id:\\d+}/reports").access("hasRole('COMPANY_VERIFIED') and @accessValidator.isUserInCompanyProductPool(principal, #user_id)")

                .antMatchers(HttpMethod.POST, "/api/documents").authenticated()
                .antMatchers(HttpMethod.GET, "/api/documents/{document_id:\\d+}").access("hasRole('ADMIN') or @accessValidator.isNotReceipt(authentication, #document_id)")

                .antMatchers("/**").permitAll()

                .and().exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
                .and().headers().cacheControl().disable()
                .and()
                .addFilterBefore(basicAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(bearerAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(userIntentFilter, UsernamePasswordAuthenticationFilter.class)
                .cors().and().csrf().disable();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/problem+json");

            String detail = exception != null && exception.getMessage() != null
                    ? exception.getMessage()
                    : "Authentication is required to access this resource.";

            String json = String.format(
                    "{ \"type\": \"%s\", \"title\": \"%s\", \"status\": %d, \"detail\": \"%s\", \"instance\": \"%s\" }",
                    "about:blank",
                    "Unauthorized",
                    HttpServletResponse.SC_UNAUTHORIZED,
                    detail,
                    request.getRequestURI()
            );

            response.getWriter().write(json);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/problem+json");

            String json = String.format(
                    "{ \"type\": \"%s\", \"title\": \"%s\", \"status\": %d, \"detail\": \"%s\", \"instance\": \"%s\" }",
                    "about:blank",
                    "Forbidden",
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to access this resource.",
                    request.getRequestURI()
            );

            response.getWriter().write(json);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOrigin(corsBasePath);
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Grupi-Action", "If-None-Match", "If-Match", "If-Modified-Since"));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Grupi-Access-Token",
                "X-Grupi-Refresh-Token",
                "X-Grupi-User-Id",
                "X-Grupi-Account-Validated",
                "X-Total-Count",
                "Link",
                "ETag",
                "Location",
                "Content-Disposition",
                "WWW-Authenticate"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
