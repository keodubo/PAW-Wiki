package ar.edu.itba.paw.interfaces.service;

import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Optional;

public interface UserService {

    User create(final String email, final String password, final String firstName, final String lastName, final String locationUri, final boolean isCompany, final String preferredLanguage);

    void edit(final int id, final String firstName, final String lastName, final String locationUri, final String password, final String language, final Integer blockLevel);

    void updatePassword(final String email, final String password);

    boolean checkPassword(final int id, final String password);

    void validateAccount(final int id);

    void resendValidationEmail(final int id);

    void sendRecoverPasswordToken(final String email);

    Optional<User> getCurrentUser();

    Optional<User> findById(final int id);

    Optional<User> findByEmail(final String email);

    Optional<User> findAdmin();

    Paginator<User> filter(final String search, final Boolean validated, final int page);

}
