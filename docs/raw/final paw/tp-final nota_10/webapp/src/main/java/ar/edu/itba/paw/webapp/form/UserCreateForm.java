package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.webapp.form.validations.EmailDoesNotExist;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserCreateForm {

    @NotNull
    @Email
    @EmailDoesNotExist
    @Size(min = 1, max = 30)
    private String email;

    @NotNull
    @Size(min = 8, max = 100)
    private String password;

    @NotNull
    @Size(min = 1, max = 30)
    private String firstName;

    @NotNull
    @Size(min = 1, max = 30)
    private String lastName;

    @NotNull
    @Pattern(regexp = "^.*/api/locations/\\d+$")
    private String locationUri;

    @NotNull
    private Boolean isCompany;

    @Pattern(regexp = "en|es")
    private String preferredLanguage;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getLocationUri() {
        return locationUri;
    }

    public void setLocationUri(final String locationUri) {
        this.locationUri = locationUri;
    }

    public Boolean getIsCompany() {
        return isCompany;
    }

    public void setIsCompany(Boolean isCompany) {
        this.isCompany = isCompany;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(final String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

}
