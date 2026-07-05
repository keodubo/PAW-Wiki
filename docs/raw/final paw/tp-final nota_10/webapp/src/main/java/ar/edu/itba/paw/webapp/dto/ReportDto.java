package ar.edu.itba.paw.webapp.dto;

import ar.edu.itba.paw.models.db.Report;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

public class ReportDto {

    private int id;
    private String description;
    private Date createdAt;
    private boolean userReported;

    private URI selfUri;
    private URI userUri;
    private URI companyUri;

    public static Function<Report, ReportDto> mapper(UriInfo uriInfo, int userId) {
        return r -> fromReport(uriInfo, userId, r);
    }

    public static ReportDto fromReport(UriInfo uriInfo, int userId, Report report) {
        final ReportDto dto = new ReportDto();

        dto.id = report.getId();
        dto.description = report.getDescription();
        dto.createdAt = report.getCreatedAt();
        dto.userReported = report.isUserReported();

        dto.selfUri = uriInfo.getBaseUriBuilder().path("api/users").path(String.valueOf(userId)).path("reports").path(String.valueOf(report.getId())).build();
        dto.userUri = uriInfo.getBaseUriBuilder().path("api/users").path(String.valueOf(report.getUser().getId())).build();
        dto.companyUri = uriInfo.getBaseUriBuilder().path("api/companies").path(String.valueOf(report.getCompany().getId())).build();

        return dto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isUserReported() {
        return userReported;
    }

    public void setUserReported(boolean userReported) {
        this.userReported = userReported;
    }

    public URI getSelfUri() {
        return selfUri;
    }

    public void setSelfUri(URI selfUri) {
        this.selfUri = selfUri;
    }

    public URI getUserUri() {
        return userUri;
    }

    public void setUserUri(URI userUri) {
        this.userUri = userUri;
    }

    public URI getCompanyUri() {
        return companyUri;
    }

    public void setCompanyUri(URI companyUri) {
        this.companyUri = companyUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportDto reportDto = (ReportDto) o;
        return id == reportDto.id && userReported == reportDto.userReported &&
                Objects.equals(description, reportDto.description) && Objects.equals(createdAt, reportDto.createdAt) &&
                Objects.equals(selfUri, reportDto.selfUri) && Objects.equals(userUri, reportDto.userUri) &&
                Objects.equals(companyUri, reportDto.companyUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, createdAt, userReported, selfUri, userUri, companyUri);
    }

}

