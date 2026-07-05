package ar.edu.itba.paw.interfaces.dao;

import ar.edu.itba.paw.models.db.Location;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Date;
import java.util.Optional;

public interface UserDao {

    User create(final String email, final String password, final String firstName, final String lastName, final Location location, final boolean isCompany, final boolean validated, final String validationToken, final String passwordToken, final boolean admin, final int blockLevel, final Date blockedUntil, final String preferredLanguage);

    void edit(final int id, final String firstName, final String lastName, final Location location, final int blockLevel, final Date blockedUntil);

    void updatePasswordToken(final int id, final String token);

    void updatePassword(final String email, final String password);

    void updateValidationToken(final int id, final String token);

    void updatePreferredLanguage(final int id, final String language);

    void validateAccount(final int id);

    Optional<User> findById(final int id);

    Optional<User> findByEmail(final String email);

    Optional<User> findAdmin();

    Paginator<User> filter(final String search, final Boolean validated, final int page);

}
