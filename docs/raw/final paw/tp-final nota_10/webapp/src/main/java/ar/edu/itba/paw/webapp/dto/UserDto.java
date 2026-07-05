package ar.edu.itba.paw.webapp.dto;

import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Location;
import ar.edu.itba.paw.models.db.User;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

public class UserDto {

    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean isCompany;
    private boolean validated;
    private boolean admin;
    private int blockLevel;
    private Date blockedUntil;
    private String preferredLanguage;
    private boolean hasReports;
    private boolean isBlocked;

    private URI selfUri;
    private URI locationUri;
    private URI companyUri;
    private URI requestsUri;
    private URI reportsUri;

    public static Function<User, UserDto> mapper(UriInfo uriInfo) {
        return u -> fromUser(uriInfo, u);
    }

    public static UserDto fromUser(UriInfo uriInfo, User user) {
        final UserDto dto = new UserDto();

        dto.id = user.getId();
        dto.email = user.getEmail();
        dto.firstName = user.getFirstName();
        dto.lastName = user.getLastName();
        dto.isCompany = user.getIsCompany();
        dto.validated = user.isValidated();
        dto.admin = user.isAdmin();
        dto.blockLevel = user.getBlockLevel();
        dto.blockedUntil = user.getBlockedUntil();
        dto.preferredLanguage = user.getPreferredLanguage();
        dto.hasReports = user.hasReports();
        dto.isBlocked = user.getIsBlocked();

        dto.selfUri = uriInfo.getBaseUriBuilder().path("api/users").path(String.valueOf(user.getId())).build();

        Location location = user.getLocation();
        if (location != null)
            dto.locationUri = uriInfo.getBaseUriBuilder().path("api/locations").path(String.valueOf(location.getId())).build();

        Company company = user.getCompany();
        if (company != null)
            dto.companyUri = uriInfo.getBaseUriBuilder().path("api/companies").path(String.valueOf(company.getId())).build();

        dto.requestsUri = uriInfo.getBaseUriBuilder().path("api/requests").queryParam("user_id", String.valueOf(user.getId())).build();
        dto.reportsUri = uriInfo.getBaseUriBuilder().path("api/users").path(String.valueOf(user.getId())).path("reports").build();

        return dto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean getIsCompany() {
        return isCompany;
    }

    public void setIsCompany(boolean isCompany) {
        this.isCompany = isCompany;
    }

    public boolean getValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public boolean getAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public int getBlockLevel() {
        return blockLevel;
    }

    public void setBlockLevel(int blockLevel) {
        this.blockLevel = blockLevel;
    }

    public Date getBlockedUntil() {
        return blockedUntil;
    }

    public void setBlockedUntil(Date blockedUntil) {
        this.blockedUntil = blockedUntil;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(final String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public boolean getHasReports() {
        return hasReports;
    }

    public void setHasReports(boolean hasReports) {
        this.hasReports = hasReports;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setIsBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public URI getSelfUri() {
        return selfUri;
    }

    public void setSelfUri(URI selfUri) {
        this.selfUri = selfUri;
    }

    public URI getLocationUri() {
        return locationUri;
    }

    public void setLocationUri(URI locationUri) {
        this.locationUri = locationUri;
    }

    public URI getCompanyUri() {
        return companyUri;
    }

    public void setCompanyUri(URI companyUri) {
        this.companyUri = companyUri;
    }

    public URI getRequestsUri() {
        return requestsUri;
    }

    public void setRequestsUri(URI requestsUri) {
        this.requestsUri = requestsUri;
    }

    public URI getReportsUri() {
        return reportsUri;
    }

    public void setReportsUri(URI reportsUri) {
        this.reportsUri = reportsUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return id == userDto.id && isCompany == userDto.isCompany && validated == userDto.validated &&
                admin == userDto.admin && blockLevel == userDto.blockLevel && hasReports == userDto.hasReports &&
                isBlocked == userDto.isBlocked && Objects.equals(email, userDto.email) &&
                Objects.equals(firstName, userDto.firstName) && Objects.equals(lastName, userDto.lastName) &&
                Objects.equals(blockedUntil, userDto.blockedUntil) &&
                Objects.equals(preferredLanguage, userDto.preferredLanguage) &&
                Objects.equals(selfUri, userDto.selfUri) && Objects.equals(locationUri, userDto.locationUri) &&
                Objects.equals(companyUri, userDto.companyUri) && Objects.equals(requestsUri, userDto.requestsUri) &&
                Objects.equals(reportsUri, userDto.reportsUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, firstName, lastName, isCompany, validated, admin, blockLevel, blockedUntil, preferredLanguage, hasReports, isBlocked, selfUri, locationUri, companyUri, requestsUri, reportsUri);
    }

}
