package ar.edu.itba.paw.webapp.dto;

import ar.edu.itba.paw.models.db.Location;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

public class LocationDto {

    private int id;
    private String name;
    private int poolsCount;

    private URI selfUri;
    private URI usersUri;
    private URI poolsUri;

    public static Function<Location, LocationDto> mapper(UriInfo uriInfo) {
        return l -> fromLocation(uriInfo, l);
    }

    public static LocationDto fromLocation(UriInfo uriInfo, Location location) {
        final LocationDto dto = new LocationDto();

        dto.id = location.getId();
        dto.name = location.getName();
        dto.poolsCount = location.getPoolsCount();

        dto.selfUri = uriInfo.getBaseUriBuilder().path("api/locations").path(String.valueOf(location.getId())).build();
        dto.usersUri = uriInfo.getBaseUriBuilder().path("api/users").queryParam("location_id", String.valueOf(location.getId())).build();
        dto.poolsUri = uriInfo.getBaseUriBuilder().path("api/pools").queryParam("location_id", String.valueOf(location.getId())).build();

        return dto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoolsCount() {
        return poolsCount;
    }

    public void setPoolsCount(int poolsCount) {
        this.poolsCount = poolsCount;
    }

    public URI getSelfUri() {
        return selfUri;
    }

    public void setSelfUri(URI selfUri) {
        this.selfUri = selfUri;
    }

    public URI getUsersUri() {
        return usersUri;
    }

    public void setUsersUri(URI usersUri) {
        this.usersUri = usersUri;
    }

    public URI getPoolsUri() {
        return poolsUri;
    }

    public void setPoolsUri(URI poolsUri) {
        this.poolsUri = poolsUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationDto that = (LocationDto) o;
        return id == that.id && poolsCount == that.poolsCount && Objects.equals(name, that.name) &&
                Objects.equals(selfUri, that.selfUri) && Objects.equals(usersUri, that.usersUri) &&
                Objects.equals(poolsUri, that.poolsUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, poolsCount, selfUri, usersUri, poolsUri);
    }

}
