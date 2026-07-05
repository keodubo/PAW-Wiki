package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.interfaces.exception.CategoryNotFoundException;
import ar.edu.itba.paw.interfaces.service.CategoryService;
import ar.edu.itba.paw.models.db.Category;
import ar.edu.itba.paw.webapp.CustomMediaType;
import ar.edu.itba.paw.webapp.dto.CategoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/api/categories")
@Component
public class CategoryController {

    @Context
    private UriInfo uriInfo;

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(final CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GET
    @Produces(CustomMediaType.APPLICATION_CATEGORY_JSON)
    public Response listCategories() {
        List<Category> categories = categoryService.getAll();
        List<CategoryDto> categoryDtos = categories.stream().map(CategoryDto.mapper(uriInfo)).toList();
        return Response.ok(new GenericEntity<>(categoryDtos) {
        }).build();
    }

    @GET
    @Produces(CustomMediaType.APPLICATION_CATEGORY_JSON)
    @Path("/{category_id:\\d+}")
    public Response getCategory(@PathParam("category_id") final int categoryId) {
        final Category category = categoryService.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        return Response.ok(CategoryDto.fromCategory(uriInfo, category)).build();
    }

}
