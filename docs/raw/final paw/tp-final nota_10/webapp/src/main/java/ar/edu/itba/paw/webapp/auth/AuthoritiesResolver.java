package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.models.db.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

public abstract class AuthoritiesResolver {

    public static List<GrantedAuthority> getAuthoritiesForUser(final User user) {
        final List<GrantedAuthority> authorities = new ArrayList<>();

        if (user.isValidated()) {
            if (user.isAdmin()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else if (user.getIsCompany()) {
                if (user.getCompany() != null) {
                    if (user.getCompany().isValidated())
                        authorities.add(new SimpleGrantedAuthority("ROLE_COMPANY_VERIFIED"));
                    else
                        authorities.add(new SimpleGrantedAuthority("ROLE_COMPANY_UNVERIFIED"));
                } else {
                    authorities.add(new SimpleGrantedAuthority("ROLE_COMPANY_PENDING"));
                }
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_CLIENT"));
            }
        }

        return authorities;
    }

}
