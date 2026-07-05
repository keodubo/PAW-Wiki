package ar.edu.itba.paw.webapp.dto;

import ar.edu.itba.paw.models.db.Category;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

public class CategoryDto {

    private int id;
    private String name;
    private String iconName;
    private int productsCount;
    private int poolsCount;

    private URI selfUri;
    private URI productsUri;

    public static Function<Category, CategoryDto> mapper(UriInfo uriInfo) {
        return c -> fromCategory(uriInfo, c);
    }

    public static CategoryDto fromCategory(UriInfo uriInfo, Category category) {
        final CategoryDto dto = new CategoryDto();

        dto.id = category.getId();
        dto.name = category.getName();
        dto.iconName = category.getIconName();
        dto.productsCount = category.getProductsCount();
        dto.poolsCount = category.getPoolsCount();

        dto.selfUri = uriInfo.getBaseUriBuilder().path("api/categories").path(String.valueOf(category.getId())).build();
        dto.productsUri = uriInfo.getBaseUriBuilder().path("api/products").queryParam("category_id", String.valueOf(category.getId())).build();

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

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public int getProductsCount() {
        return productsCount;
    }

    public void setProductsCount(int productsCount) {
        this.productsCount = productsCount;
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
        CategoryDto that = (CategoryDto) o;
        return id == that.id && productsCount == that.productsCount && poolsCount == that.poolsCount &&
                Objects.equals(name, that.name) && Objects.equals(iconName, that.iconName) &&
                Objects.equals(selfUri, that.selfUri) && Objects.equals(productsUri, that.productsUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, iconName, productsCount, poolsCount, selfUri, productsUri);
    }

}
