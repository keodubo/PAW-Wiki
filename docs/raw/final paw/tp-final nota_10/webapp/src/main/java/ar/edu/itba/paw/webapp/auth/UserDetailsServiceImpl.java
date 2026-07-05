package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.models.db.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Pattern;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}");

    private final UserService userService;

    @Autowired
    public UserDetailsServiceImpl(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        final User user = userService.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("No user for email: " + email));

        if (!BCRYPT_PATTERN.matcher(user.getPassword()).matches()) {
            LOGGER.warn("Password not bcrypt encoded for user: {}", email);
            userService.updatePassword(email, user.getPassword());
            return loadUserByUsername(email);
        }

        if (user.getBlockLevel() == 4 || (user.getBlockLevel() >= 1 && user.getBlockedUntil().after(new Date()))) {
            LOGGER.info("Blocked user attempted login: {}", email);
            return new AuthUser(user.getId(), email, user.getPassword(), false, true, true, true, new HashSet<>());
        }

        final Collection<GrantedAuthority> authorities = AuthoritiesResolver.getAuthoritiesForUser(user);
        return new AuthUser(user.getId(), email, user.getPassword(), authorities);
    }

}
