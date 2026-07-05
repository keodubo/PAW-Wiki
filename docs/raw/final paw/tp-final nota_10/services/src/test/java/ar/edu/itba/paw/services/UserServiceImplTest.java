package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.UserDao;
import ar.edu.itba.paw.interfaces.exception.*;
import ar.edu.itba.paw.interfaces.service.EmailService;
import ar.edu.itba.paw.interfaces.service.LocationService;
import ar.edu.itba.paw.models.db.Location;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    private static final int ID = 1;
    private static final String EMAIL = "test@user.com";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String FIRST_NAME = "Test";
    private static final String LAST_NAME = "User";
    private static final boolean IS_COMPANY = false;
    private static final String PREFERRED_LANGUAGE = "en";

    private static final int LOCATION_ID_1 = 1;
    private static final String LOCATION_NAME_1 = "location1";
    private static final String LOCATION_URI_1 = "/location/" + LOCATION_ID_1;

    private static final int LOCATION_ID_2 = 2;
    private static final String LOCATION_NAME_2 = "location2";
    private static final String LOCATION_URI_2 = "/location/" + LOCATION_ID_2;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserDao userDao;

    @Mock
    private LocationService locationService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    // tests de create
    @Test
    public void testCreateUserValidLocation() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, false, null, null, false, 0, null, PREFERRED_LANGUAGE);

        when(locationService.findById(LOCATION_ID_1)).thenReturn(Optional.of(location));
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        when(userDao.create(any(), any(), any(), any(), any(), anyBoolean(), anyBoolean(), any(), any(), anyBoolean(), anyInt(), any(), any())).thenReturn(user);

        User result = userService.create(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, LOCATION_URI_1, IS_COMPANY, PREFERRED_LANGUAGE);

        verify(locationService).findById(eq(LOCATION_ID_1));
        verify(passwordEncoder).encode(eq(PASSWORD));
        verify(userDao).create(
                eq(EMAIL),
                eq(ENCODED_PASSWORD),
                eq(FIRST_NAME),
                eq(LAST_NAME),
                eq(location),
                eq(IS_COMPANY),
                eq(false),
                anyString(),
                eq(null),
                eq(false),
                eq(0),
                eq(null),
                eq(PREFERRED_LANGUAGE)
        );
        verify(emailService).sendUserRegisterEmail(eq(result), any(Locale.class));
        assertSame(user, result);
    }

    @Test(expected = LocationNotFoundException.class)
    public void testCreateUserInvalidLocation() {
        when(locationService.findById(LOCATION_ID_1)).thenReturn(Optional.empty());

        userService.create(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, LOCATION_URI_1, IS_COMPANY, PREFERRED_LANGUAGE);
    }

    @Test
    public void testCreateUserWithUnknownLanguageDefaultsToEn() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);

        when(locationService.findById(LOCATION_ID_1)).thenReturn(Optional.of(location));
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        when(userDao.create(any(), any(), any(), any(), any(), anyBoolean(),
                anyBoolean(), any(), any(), anyBoolean(), anyInt(), any(), any()))
                .thenReturn(new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, false, null, null, false, 0, null, "en"));

        User user = userService.create(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, LOCATION_URI_1, IS_COMPANY, "fr");

        verify(userDao).create(
                eq(EMAIL), anyString(), eq(FIRST_NAME), eq(LAST_NAME),
                eq(location), eq(IS_COMPANY), eq(false), anyString(),
                eq(null), eq(false), eq(0), eq(null), eq("en")
        );

        assertNotNull(user);
    }

    // tests de edit
    @Test
    public void testEditNameAndLocation() {
        Location oldLocation = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        Location newLocation = new Location(LOCATION_ID_2, LOCATION_NAME_2);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, oldLocation, IS_COMPANY, false, null, null, false, 0, null, PREFERRED_LANGUAGE);

        String newFirstName = "NewFirst", newLastName = "NewLast";

        when(userDao.findById(ID)).thenReturn(Optional.of(user));
        when(locationService.findById(LOCATION_ID_2)).thenReturn(Optional.of(newLocation));

        userService.edit(ID, newFirstName, newLastName, LOCATION_URI_2, null, null, null);

        verify(userDao).edit(eq(ID), eq(newFirstName), eq(newLastName), eq(newLocation), eq(0), eq(null));
        verify(userDao, never()).updatePassword(any(), any());
        verify(userDao, never()).updatePreferredLanguage(anyInt(), any());
        verifyZeroInteractions(passwordEncoder);
    }

    @Test
    public void testEditPassword() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, false, null, null, false, 0, null, PREFERRED_LANGUAGE);

        String newPassword = "newpassword";

        when(userDao.findById(ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn(ENCODED_PASSWORD);

        userService.edit(ID, null, null, null, newPassword, null, null);

        verify(passwordEncoder).encode(eq(newPassword));
        verify(userDao).updatePassword(eq(EMAIL), eq(ENCODED_PASSWORD));
        verify(userDao, never()).edit(anyInt(), any(), any(), any(), anyInt(), any());
        verify(userDao, never()).updatePreferredLanguage(anyInt(), any());
    }

    @Test
    public void testEditLanguage() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, false, null, null, false, 0, null, PREFERRED_LANGUAGE);

        String newLanguage = "es";

        when(userDao.findById(ID)).thenReturn(Optional.of(user));

        userService.edit(ID, null, null, null, null, newLanguage, null);

        verify(userDao).updatePreferredLanguage(eq(ID), eq(newLanguage));
        verify(userDao, never()).updatePassword(any(), any());
        verify(userDao, never()).edit(anyInt(), any(), any(), any(), anyInt(), any());
        verifyZeroInteractions(passwordEncoder);
    }

    @Test(expected = InvalidLanguageException.class)
    public void testEditInvalidLanguageThrows() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, false, null, null, false, 0, null, PREFERRED_LANGUAGE);

        when(userDao.findById(ID)).thenReturn(Optional.of(user));

        userService.edit(ID, null, null, null, null, "fr", null);
    }

    @Test
    public void testEditBlockLevel1() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, false, null, null, false, 0, null, PREFERRED_LANGUAGE);

        when(userDao.findById(ID)).thenReturn(Optional.of(user));

        userService.edit(ID, null, null, null, null, null, 1);

        verify(userDao).edit(eq(ID), eq(FIRST_NAME), eq(LAST_NAME), eq(location), eq(1), notNull());
        verify(userDao, never()).updatePassword(any(), any());
        verify(userDao, never()).updatePreferredLanguage(anyInt(), any());
        verifyZeroInteractions(passwordEncoder);
    }

    @Test
    public void testEditBlockLevel0ResetsBlock() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, false, null, null, false, 1, null, PREFERRED_LANGUAGE);

        when(userDao.findById(ID)).thenReturn(Optional.of(user));

        userService.edit(ID, null, null, null, null, null, 0);

        verify(userDao).edit(eq(ID), eq(FIRST_NAME), eq(LAST_NAME), eq(location), eq(0), eq(null));
        verify(userDao, never()).updatePassword(any(), any());
        verify(userDao, never()).updatePreferredLanguage(anyInt(), any());
        verifyZeroInteractions(passwordEncoder);
    }

    @Test(expected = InvalidBlockLevelException.class)
    public void testEditInvalidBlockLevelThrows() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, false, null, null, false, 0, null, PREFERRED_LANGUAGE);

        when(userDao.findById(ID)).thenReturn(Optional.of(user));

        userService.edit(ID, null, null, null, null, null, 99);
    }

    @Test(expected = UserNotFoundException.class)
    public void testEditNotFoundThrows() {
        when(userDao.findById(ID)).thenReturn(Optional.empty());

        userService.edit(ID, FIRST_NAME, LAST_NAME, null, null, null, null);
    }

    @Test(expected = LocationNotFoundException.class)
    public void testEditLocationNotFoundThrows() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, false, null, null, false, 0, null, PREFERRED_LANGUAGE);

        when(userDao.findById(ID)).thenReturn(Optional.of(user));
        when(locationService.findById(LOCATION_ID_2)).thenReturn(Optional.empty());

        userService.edit(ID, FIRST_NAME, LAST_NAME, LOCATION_URI_2, null, null, null);
    }

    // tests de updatePassword
    @Test
    public void testUpdatePassword() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);

        userService.updatePassword(EMAIL, PASSWORD);

        verify(passwordEncoder).encode(eq(PASSWORD));
        verify(userDao).updatePassword(eq(EMAIL), eq(ENCODED_PASSWORD));
    }

    // tests de checkPassword
    @Test
    public void testCheckPasswordMatchingPassword() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, false, null, null, false, 0, null, PREFERRED_LANGUAGE);

        when(userDao.findById(ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(ENCODED_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        boolean result = userService.checkPassword(ID, ENCODED_PASSWORD);

        assertTrue(result);
        verify(passwordEncoder).matches(eq(ENCODED_PASSWORD), eq(ENCODED_PASSWORD));
    }

    @Test
    public void testCheckPasswordWrongPassword() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, false, null, null, false, 0, null, PREFERRED_LANGUAGE);

        String wrongPassword = "wrongpassword";

        when(userDao.findById(ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        boolean result = userService.checkPassword(ID, wrongPassword);

        assertFalse(result);
        verify(passwordEncoder).matches(eq(wrongPassword), eq(ENCODED_PASSWORD));
    }

    @Test
    public void testCheckPasswordUserNotExistsReturnsFalse() {
        when(userDao.findById(ID)).thenReturn(Optional.empty());

        boolean result = userService.checkPassword(ID, PASSWORD);

        assertFalse(result);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    // tests de validateAccount
    @Test
    public void testValidateAccountCallsDao() {
        userService.validateAccount(ID);

        verify(userDao).validateAccount(eq(ID));
    }

    // tests de resendValidationEmail
    @Test
    public void testResendValidationEmailSuccess() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, false, null, null, false, 0, null, PREFERRED_LANGUAGE);

        when(userDao.findById(ID)).thenReturn(Optional.of(user));

        userService.resendValidationEmail(ID);

        verify(userDao).updateValidationToken(eq(ID), anyString());
        verify(emailService).sendValidationTokenEmail(eq(user), any(Locale.class));
    }

    @Test(expected = UserNotFoundException.class)
    public void testResendValidationEmailUserNotFoundThrows() {
        when(userDao.findById(ID)).thenReturn(Optional.empty());

        userService.resendValidationEmail(ID);
    }

    @Test(expected = UserAlreadyValidatedException.class)
    public void testResendValidationEmailAlreadyValidatedThrows() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, true, null, null, false, 0, null, PREFERRED_LANGUAGE);

        when(userDao.findById(ID)).thenReturn(Optional.of(user));

        userService.resendValidationEmail(ID);
    }

    // tests de sendRecoverPasswordToken
    @Test
    public void testSendRecoverPasswordTokenSuccess() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, true, null, null, false, 0, null, PREFERRED_LANGUAGE);

        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        userService.sendRecoverPasswordToken(EMAIL);

        verify(userDao).updatePasswordToken(eq(ID), anyString());
        verify(emailService).sendUserPasswordResetEmail(eq(user), any(Locale.class));
    }

    @Test
    public void testSendRecoverPasswordTokenUserNotExistsDoesNothing() {
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.empty());

        userService.sendRecoverPasswordToken(EMAIL);

        verify(userDao, never()).updatePasswordToken(anyInt(), any());
        verify(emailService, never()).sendUserPasswordResetEmail(any(), any());
    }

    // tests de getCurrentUser
    @Test
    public void testGetCurrentUserAuthNull() {
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(null);

        Optional<User> result = userService.getCurrentUser();

        assertFalse(result.isPresent());
        verify(userDao, never()).findByEmail(any());
    }

    @Test
    public void testGetCurrentUserAuthPresent() {
        Location location = new Location(LOCATION_ID_1, LOCATION_NAME_1);
        User user = new User(ID, EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME, location, IS_COMPANY, true, null, null, false, 0, null, PREFERRED_LANGUAGE);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(EMAIL);
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getCurrentUser();

        assertTrue(result.isPresent());
        assertSame(user, result.get());
        verify(userDao).findByEmail(eq(EMAIL));
    }

    // tests de findById
    @Test
    public void testFindByIdExists() {
        User user = mock(User.class);

        when(userDao.findById(ID)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(ID);

        assertTrue(result.isPresent());
        assertSame(user, result.get());
        verify(userDao).findById(eq(ID));
    }

    @Test
    public void testFindByIdNotExists() {
        when(userDao.findById(ID)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(ID);

        assertFalse(result.isPresent());
        verify(userDao).findById(eq(ID));
    }

    // tests de findByEmail
    @Test
    public void testFindByEmailExists() {
        User user = mock(User.class);

        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail(EMAIL);

        assertTrue(result.isPresent());
        assertSame(user, result.get());
        verify(userDao).findByEmail(eq(EMAIL));
    }

    @Test
    public void testFindByEmailNotExists() {
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail(EMAIL);

        assertFalse(result.isPresent());
        verify(userDao).findByEmail(eq(EMAIL));
    }

    // tests de findAdmin
    @Test
    public void testFindAdminExists() {
        User admin = mock(User.class);

        when(userDao.findAdmin()).thenReturn(Optional.of(admin));

        Optional<User> result = userService.findAdmin();

        assertTrue(result.isPresent());
        assertSame(admin, result.get());
        verify(userDao).findAdmin();
    }

    @Test
    public void testFindAdminNotExists() {
        when(userDao.findAdmin()).thenReturn(Optional.empty());

        Optional<User> result = userService.findAdmin();

        assertFalse(result.isPresent());
        verify(userDao).findAdmin();
    }

    // tests de filter
    @Test
    public void testFilterWithAllParameters() {
        int page = 0;
        String search = "search";
        boolean validated = true;

        User user1 = mock(User.class), user2 = mock(User.class);
        Paginator<User> paginator = new Paginator<>(Arrays.asList(user1, user2), page, 12, 2);

        when(userDao.filter(search, validated, page)).thenReturn(paginator);

        Paginator<User> result = userService.filter(search, validated, page);

        assertSame(paginator, result);
        verify(userDao).filter(eq(search), eq(validated), eq(page));
    }

    @Test
    public void testFilterWithNullParameters() {
        int page = 0;

        User user = mock(User.class);
        Paginator<User> paginator = new Paginator<>(Collections.singletonList(user), page, 12, 1);

        when(userDao.filter(null, null, page)).thenReturn(paginator);

        Paginator<User> result = userService.filter(null, null, page);

        assertSame(paginator, result);
        verify(userDao).filter(eq(null), eq(null), eq(page));
    }

    @Test
    public void testFilterWithEmptyResult() {
        int page = 0;

        Paginator<User> paginator = new Paginator<>();

        when(userDao.filter(null, null, page)).thenReturn(paginator);

        Paginator<User> result = userService.filter(null, null, page);

        assertSame(paginator, result);
        verify(userDao).filter(eq(null), eq(null), eq(page));
    }

}
