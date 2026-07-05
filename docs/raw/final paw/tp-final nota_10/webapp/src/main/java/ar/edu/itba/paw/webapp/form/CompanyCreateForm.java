package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CompanyCreateForm {

    @NotNull
    @Size(min = 1, max = 30)
    private String name;

    @NotNull
    @Size(min = 1, max = 100)
    private String address;

    @NotNull
    @Email
    @Size(min = 1, max = 30)
    private String email;

    @NotNull
    @Size(min = 1, max = 15)
    @Pattern(regexp = "\\+?\\d{1,15}")
    private String phone;

    @NotNull
    @Size(min = 1, max = 22)
    @Pattern(regexp = "\\d{1,22}")
    private String cbu;

    @NotNull
    @Pattern(regexp = "^.*/api/documents/\\d+$")
    private String imageUri;

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

    public String getCbu() {
        return cbu;
    }

    public void setCbu(String cbu) {
        this.cbu = cbu;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

}

