package ar.edu.itba.paw.webapp.dto;

import ar.edu.itba.paw.models.db.User;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

public class ReviewerDto {

    private int id;
    private String firstName;
    private String lastName;

    private URI selfUri;

    public static Function<User, ReviewerDto> mapper(UriInfo uriInfo) {
        return r -> fromReviewer(uriInfo, r);
    }

    public static ReviewerDto fromReviewer(UriInfo uriInfo, User reviewer) {
        final ReviewerDto dto = new ReviewerDto();

        dto.id = reviewer.getId();
        dto.firstName = reviewer.getFirstName();
        dto.lastName = reviewer.getLastName();

        dto.selfUri = uriInfo.getBaseUriBuilder().path("api/users").path(String.valueOf(reviewer.getId())).build();

        return dto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public URI getSelfUri() {
        return selfUri;
    }

    public void setSelfUri(URI selfUri) {
        this.selfUri = selfUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewerDto that = (ReviewerDto) o;
        return id == that.id && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) &&
                Objects.equals(selfUri, that.selfUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, selfUri);
    }

}
