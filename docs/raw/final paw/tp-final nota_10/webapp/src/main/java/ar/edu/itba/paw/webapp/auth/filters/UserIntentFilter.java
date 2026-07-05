package ar.edu.itba.paw.webapp.auth.filters;

import ar.edu.itba.paw.interfaces.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

@Component
@Order(3)
public class UserIntentFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserIntentFilter.class);

    public static final String ACTION_HEADER = "X-Grupi-Action";
    public static final String ACTION_ATTRIBUTE = "user.action";

    public static final String ACTION_RESEND_VALIDATION = "resend-validation";
    public static final String ACTION_RECOVER_PASSWORD = "recover-password";
    public static final String RECOVER_PASSWORD_AUTH_VALUE = "recover-password";

    private final UserService userService;

    @Autowired
    public UserIntentFilter(final UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        // El filtro solo aplica cuando se hace un PATCH a /api/users o /api/users/[id]
        if (!"PATCH".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String path = request.getRequestURI();
        if (!path.contains("/api/users") && !path.contains("/users")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Se envía un Header custom
        // X-Grupi-Action: "resend-validation" o X-Grupi-Action: "recover-password"
        String action = request.getHeader(ACTION_HEADER);
        if (action == null || action.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        action = action.trim().toLowerCase();

        if (ACTION_RECOVER_PASSWORD.equals(action)) {
            if (handleRecoverPassword(request, response))
                return;
            filterChain.doFilter(request, response);
            return;
        }

        // El envío de mail de verificación se maneja en el controller (para aprovechar el chequeo de seguridad)
        if (ACTION_RESEND_VALIDATION.equals(action))
            request.setAttribute(ACTION_ATTRIBUTE, action);

        filterChain.doFilter(request, response);
    }

    private boolean handleRecoverPassword(final HttpServletRequest request, final HttpServletResponse response) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic "))
            return false;

        try {
            String base64Credentials = authHeader.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] values = credentials.split(":", 2);

            if (values.length != 2)
                return false;

            String email = values[0];
            String password = values[1];

            if (!RECOVER_PASSWORD_AUTH_VALUE.equals(password))
                return false;

            if (email == null || !email.contains("@"))
                return false;

            userService.sendRecoverPasswordToken(email);
            LOGGER.info("Password recovery token sent to: {}", email);

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return true;

        } catch (Exception e) {
            LOGGER.error("Error processing password recovery request", e);
            return false;
        }
    }

}
