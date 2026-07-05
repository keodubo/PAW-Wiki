package ar.edu.itba.paw.webapp.form.validations;

import ar.edu.itba.paw.interfaces.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailDoesNotExistValidator implements ConstraintValidator<EmailDoesNotExist, String> {

    @Autowired
    private UserService userService;

    @Override
    public void initialize(EmailDoesNotExist constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if (userService == null)
            throw new IllegalStateException("UserService not injected in EmailDoesNotExistValidator");
        if (email == null)
            return false;
        return userService.findByEmail(email).isEmpty();
    }

}
