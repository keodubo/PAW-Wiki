package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.*;

public class ProductCreateForm {

    @NotNull
    @Size(min = 1, max = 31)
    private String name;

    @NotNull
    @Size(min = 1, max = 1024)
    private String description;

    @NotNull
    @Min(value = 1)
    @Digits(integer = 8, fraction = 2)
    private Double price;

    @NotNull
    @Pattern(regexp = "^.*/api/categories/\\d+$")
    private String categoryUri;

    @NotNull
    @Pattern(regexp = "^.*/api/documents/\\d+$")
    private String imageUri;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(final Double price) {
        this.price = price;
    }

    public String getCategoryUri() {
        return categoryUri;
    }

    public void setCategoryUri(final String categoryUri) {
        this.categoryUri = categoryUri;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(final String imageUri) {
        this.imageUri = imageUri;
    }

}
