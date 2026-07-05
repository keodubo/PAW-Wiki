package ar.edu.itba.paw.webapp.dto;

import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Document;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

public class CompanyDto {

    private int id;
    private String name;
    private String address;
    private String email;
    private String phone;
    private boolean validated;
    private String cbu;

    private URI selfUri;
    private URI imageUri;
    private URI ownerUri;
    private URI productsUri;

    public static Function<Company, CompanyDto> mapper(UriInfo uriInfo) {
        return c -> fromCompany(uriInfo, c);
    }

    public static CompanyDto fromCompany(UriInfo uriInfo, Company company) {
        final CompanyDto dto = new CompanyDto();

        dto.id = company.getId();
        dto.name = company.getName();
        dto.address = company.getAddress();
        dto.email = company.getEmail();
        dto.phone = company.getPhone();
        dto.validated = company.isValidated();
        dto.cbu = company.getCbu();

        dto.selfUri = uriInfo.getBaseUriBuilder().path("api/companies").path(String.valueOf(company.getId())).build();

        Document image = company.getImage();
        if (image != null)
            dto.imageUri = uriInfo.getBaseUriBuilder().path("api/documents").path(String.valueOf(image.getId())).build();

        dto.ownerUri = uriInfo.getBaseUriBuilder().path("api/users").path(String.valueOf(company.getOwner().getId())).build();
        dto.productsUri = uriInfo.getBaseUriBuilder().path("api/products").queryParam("company_id", String.valueOf(company.getId())).build();

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public String getCbu() {
        return cbu;
    }

    public void setCbu(String cbu) {
        this.cbu = cbu;
    }

    public URI getSelfUri() {
        return selfUri;
    }

    public void setSelfUri(URI selfUri) {
        this.selfUri = selfUri;
    }

    public URI getImageUri() {
        return imageUri;
    }

    public void setImageUri(URI imageUri) {
        this.imageUri = imageUri;
    }

    public URI getOwnerUri() {
        return ownerUri;
    }

    public void setOwnerUri(URI ownerUri) {
        this.ownerUri = ownerUri;
    }

    public URI getProductsUri() {
        return productsUri;
    }

    public void setProductsUri(URI productsUri) {
        this.productsUri = productsUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyDto that = (CompanyDto) o;
        return id == that.id && validated == that.validated && Objects.equals(name, that.name) &&
                Objects.equals(address, that.address) && Objects.equals(email, that.email) &&
                Objects.equals(phone, that.phone) && Objects.equals(cbu, that.cbu) &&
                Objects.equals(selfUri, that.selfUri) && Objects.equals(imageUri, that.imageUri) &&
                Objects.equals(ownerUri, that.ownerUri) && Objects.equals(productsUri, that.productsUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, address, email, phone, validated, cbu, selfUri, imageUri, ownerUri, productsUri);
    }

}
