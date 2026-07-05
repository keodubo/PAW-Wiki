package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.interfaces.exception.CompanyInfoNotFilledException;
import ar.edu.itba.paw.interfaces.exception.CompanyNotValidatedException;
import ar.edu.itba.paw.interfaces.exception.UserNotFoundException;
import ar.edu.itba.paw.interfaces.service.ProductService;
import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Product;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.webapp.CustomMediaType;
import ar.edu.itba.paw.webapp.dto.ProductDto;
import ar.edu.itba.paw.webapp.form.ProductCreateForm;
import ar.edu.itba.paw.webapp.form.ProductPatchForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/api/products")
@Component
public class ProductController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    @Context
    private UriInfo uriInfo;

    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public ProductController(final ProductService productService, final UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GET
    @Produces(CustomMediaType.APPLICATION_PRODUCT_JSON)
    public Response listProducts(
            @QueryParam("search") final String search,
            @QueryParam("category_id") final Integer categoryId,
            @QueryParam("company_id") final Integer companyId,
            @QueryParam("price_min") final Double priceMin,
            @QueryParam("price_max") final Double priceMax,
            @QueryParam("active") final Boolean active,
            @QueryParam("page") @DefaultValue("0") final int page,
            @QueryParam("order_by") @DefaultValue("price") final String orderBy,
            @QueryParam("desc") @DefaultValue("true") final Boolean desc
    ) {

        Paginator<Product> paginator = productService.filter(search, categoryId, companyId, priceMin, priceMax, active, page, orderBy, desc);
        List<ProductDto> products = paginator.getList().stream().map(ProductDto.mapper(uriInfo)).toList();
        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
        Response.ResponseBuilder r = Response.ok(new GenericEntity<>(products) {
                })
                .header("X-Total-Count", String.valueOf(paginator.getTotalItems()))
                .link(uriBuilder.clone().replaceQueryParam("page", 0).build(), "first")
                .link(uriBuilder.clone().replaceQueryParam("page", paginator.getLastPage()).build(), "last");

        if (paginator.getPage() > 0)
            r.link(uriBuilder.clone().replaceQueryParam("page", paginator.getPage() - 1).build(), "prev");
        if (paginator.getPage() < paginator.getLastPage())
            r.link(uriBuilder.clone().replaceQueryParam("page", paginator.getPage() + 1).build(), "next");

        return r.build();
    }

    @GET
    @Path("/{product_id:\\d+}")
    @Produces(CustomMediaType.APPLICATION_PRODUCT_JSON)
    public Response getProduct(@PathParam("product_id") final int productId) {
        final Product product = productService.findById(productId).orElseThrow(NotFoundException::new);
        return Response.ok(ProductDto.fromProduct(uriInfo, product)).build();
    }

    @POST
    @Consumes(CustomMediaType.APPLICATION_PRODUCT_JSON)
    public Response createProduct(@Valid @NotNull final ProductCreateForm form) {
        final User user = userService.getCurrentUser().orElseThrow(UserNotFoundException::new);
        final Company company = user.getCompany();

        if (company == null)
            throw new CompanyInfoNotFilledException();
        if (!company.isValidated())
            throw new CompanyNotValidatedException();

        final Product product = productService.create(form.getName(), form.getDescription(), form.getPrice(), form.getImageUri(), company, form.getCategoryUri());
        LOGGER.info("Product created: {}", product.getId());
        return Response.created(uriInfo.getBaseUriBuilder().path("api/products").path(String.valueOf(product.getId())).build()).build();
    }

    @PATCH
    @Path("/{product_id:\\d+}")
    @Consumes(CustomMediaType.APPLICATION_PRODUCT_JSON)
    @PreAuthorize("@accessValidator.canUpdateProduct(#productId, #form)")
    public Response updateProduct(@PathParam("product_id") final int productId, @Valid @NotNull final ProductPatchForm form) {
        if (form.getActive() != null && !form.getActive()) {
            productService.retire(productId);
            LOGGER.info("Product retired: {}", productId);
        } else {
            productService.edit(productId, form.getName(), form.getDescription(), form.getPrice(), form.getImageUri(), form.getCategoryUri());
            LOGGER.info("Product updated: {}", productId);
        }
        return Response.noContent().build();
    }

}
