package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.UserDao;
import ar.edu.itba.paw.interfaces.exception.*;
import ar.edu.itba.paw.interfaces.service.EmailService;
import ar.edu.itba.paw.interfaces.service.LocationService;
import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.interfaces.utils.UriUtils;
import ar.edu.itba.paw.models.db.Location;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final int BLOCK_LEVEL_1_DURATION = 7 * 24 * 60 * 60; // 1 semana
    private static final int BLOCK_LEVEL_2_DURATION = 2 * BLOCK_LEVEL_1_DURATION; // 2 semanas
    private static final int BLOCK_LEVEL_3_DURATION = 4 * BLOCK_LEVEL_1_DURATION; // 4 semanas

    private final UserDao userDao;
    private final LocationService locationService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(final UserDao userDao, final LocationService locationService, final EmailService emailService, final PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.locationService = locationService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public User create(final String email, final String password, final String firstName, final String lastName, final String locationUri, final boolean isCompany, final String preferredLanguage) {
        final int locationId = UriUtils.extractIdFromUri(locationUri);
        final Location location = locationService.findById(locationId).orElseThrow(LocationNotFoundException::new);
        final String lang = ("es".equals(preferredLanguage) || "en".equals(preferredLanguage)) ? preferredLanguage : "en";
        final User user = userDao.create(email, passwordEncoder.encode(password), firstName, lastName, location, isCompany, false, generateToken(24), null, false, 0, null, lang);
        emailService.sendUserRegisterEmail(user, LocaleContextHolder.getLocale());
        LOGGER.info("User created: {} (email: {}, isCompany: {})", user.getId(), email, isCompany);
        return user;
    }

    @Transactional
    @Override
    public void edit(final int id, final String firstName, final String lastName, final String locationUri, final String password, final String language, final Integer blockLevel) {
        final User user = userDao.findById(id).orElseThrow(UserNotFoundException::new);

        if (firstName != null || lastName != null || locationUri != null) {
            final String finalFirstName = firstName != null ? firstName : user.getFirstName();
            final String finalLastName = lastName != null ? lastName : user.getLastName();
            Location finalLocation = user.getLocation();
            if (locationUri != null) {
                final int locationId = UriUtils.extractIdFromUri(locationUri);
                finalLocation = locationService.findById(locationId).orElseThrow(LocationNotFoundException::new);
            }
            userDao.edit(id, finalFirstName, finalLastName, finalLocation, user.getBlockLevel(), user.getBlockedUntil());
        }

        if (password != null)
            userDao.updatePassword(user.getEmail(), passwordEncoder.encode(password));

        if (language != null) {
            if (!language.equals("es") && !language.equals("en"))
                throw new InvalidLanguageException();
            userDao.updatePreferredLanguage(id, language);
        }

        if (blockLevel != null && !blockLevel.equals(user.getBlockLevel())) {
            Date blockedUntil = switch (blockLevel) {
                case 0, 4 -> null;
                case 1 -> Date.from(Instant.now().plusSeconds(BLOCK_LEVEL_1_DURATION));
                case 2 -> Date.from(Instant.now().plusSeconds(BLOCK_LEVEL_2_DURATION));
                case 3 -> Date.from(Instant.now().plusSeconds(BLOCK_LEVEL_3_DURATION));
                default -> throw new InvalidBlockLevelException();
            };
            userDao.edit(id, user.getFirstName(), user.getLastName(), user.getLocation(), blockLevel, blockedUntil);
            LOGGER.info("User {} blocked: level {}", id, blockLevel);
        }
    }

    @Transactional
    @Override
    public void updatePassword(final String email, final String password) {
        userDao.updatePassword(email, passwordEncoder.encode(password));
    }

    @Transactional(readOnly = true)
    @Override
    public boolean checkPassword(final int id, final String password) {
        final Optional<User> user = userDao.findById(id);
        return user.isPresent() && passwordEncoder.matches(password, user.get().getPassword());
    }

    @Transactional
    @Override
    public void validateAccount(final int id) {
        userDao.validateAccount(id);
        LOGGER.info("User account validated: {}", id);
    }

    @Transactional
    @Override
    public void resendValidationEmail(final int id) {
        final User user = userDao.findById(id).orElseThrow(UserNotFoundException::new);
        if (user.isValidated())
            throw new UserAlreadyValidatedException();
        userDao.updateValidationToken(user.getId(), generateToken(24));
        emailService.sendValidationTokenEmail(user, LocaleContextHolder.getLocale());
    }

    @Transactional
    @Override
    public void sendRecoverPasswordToken(final String email) {
        final Optional<User> user = userDao.findByEmail(email);
        if (user.isEmpty())
            return;
        userDao.updatePasswordToken(user.get().getId(), generateToken(1));
        emailService.sendUserPasswordResetEmail(user.get(), LocaleContextHolder.getLocale());
        LOGGER.info("Password reset token sent for user: {}", email);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> getCurrentUser() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return Optional.empty();
        return userDao.findByEmail(auth.getName());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findById(final int id) {
        return userDao.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findByEmail(final String email) {
        return userDao.findByEmail(email);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findAdmin() {
        return userDao.findAdmin();
    }

    @Transactional(readOnly = true)
    @Override
    public Paginator<User> filter(final String search, final Boolean validated, final int page) {
        return userDao.filter(search, validated, page);
    }

    private String generateToken(int hours) {
        String numbers = "0123456789";
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            otp.append(numbers.charAt(random.nextInt(numbers.length())));
        }

        final long expirationTime = Instant.now().plus(hours, ChronoUnit.HOURS).toEpochMilli();
        return otp + ":" + expirationTime;
    }

}
