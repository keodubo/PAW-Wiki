package ar.edu.itba.paw.webapp.form;


import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ProductPatchForm {

    @Size(min = 1, max = 31)
    private String name;

    @Size(min = 1, max = 1024)
    private String description;

    @Min(value = 1)
    @Digits(integer = 8, fraction = 2)
    private Double price;

    @Pattern(regexp = "^.*/api/categories/\\d+$")
    private String categoryUri;

    @Pattern(regexp = "^.*/api/documents/\\d+$")
    private String imageUri;

    private Boolean active;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategoryUri() {
        return categoryUri;
    }

    public void setCategoryUri(String categoryUri) {
        this.categoryUri = categoryUri;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

}

