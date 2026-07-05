package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.interfaces.exception.PoolNotFoundException;
import ar.edu.itba.paw.interfaces.service.PoolService;
import ar.edu.itba.paw.models.db.Pool;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.webapp.CustomMediaType;
import ar.edu.itba.paw.webapp.dto.PoolDto;
import ar.edu.itba.paw.webapp.form.PoolCreateForm;
import ar.edu.itba.paw.webapp.form.PoolPatchForm;
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

@Path("/api/pools")
@Component
public class PoolController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolController.class);

    @Context
    private UriInfo uriInfo;

    private final PoolService poolService;

    @Autowired
    public PoolController(final PoolService poolService) {
        this.poolService = poolService;
    }

    @GET
    @Produces(CustomMediaType.APPLICATION_POOL_JSON)
    public Response listPools(
            @QueryParam("product_id") final Integer productId,
            @QueryParam("search") final String search,
            @QueryParam("status") final String statusStr,
            @QueryParam("company_id") final Integer companyId,
            @QueryParam("price_min") final Double priceMin,
            @QueryParam("price_max") final Double priceMax,
            @QueryParam("location_id") final Integer locationId,
            @QueryParam("category_id") final Integer categoryId,
            @QueryParam("page") @DefaultValue("0") final int page,
            @QueryParam("order_by") @DefaultValue("products_requested_count") final String orderBy,
            @QueryParam("desc") @DefaultValue("true") final Boolean desc
    ) {
        Paginator<Pool> paginator = poolService.filter(productId, search, statusStr, companyId, priceMin, priceMax, locationId, categoryId, page, orderBy, desc);
        List<PoolDto> pools = paginator.getList().stream().map(PoolDto.mapper(uriInfo)).toList();
        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
        Response.ResponseBuilder r = Response.ok(new GenericEntity<>(pools) {
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
    @Path("/{pool_id:\\d+}")
    @Produces(CustomMediaType.APPLICATION_POOL_JSON)
    public Response getPool(@PathParam("pool_id") final int poolId, @Context final Request request) {
        final Pool pool = poolService.findById(poolId).orElseThrow(PoolNotFoundException::new);
        return Response.ok(PoolDto.fromPool(uriInfo, pool)).build();
    }

    @POST
    @Consumes(CustomMediaType.APPLICATION_POOL_JSON)
    @PreAuthorize("@accessValidator.isProductOwnerUri(principal, #form.productUri) and @accessValidator.productIsActive(#form.productUri)")
    public Response createPool(@Valid @NotNull final PoolCreateForm form) {
        final Pool pool = poolService.create(form.getMinQuantity(), form.getDownPayment(), form.getProductUri(), form.getLocationUri());
        LOGGER.info("Pool created: {}", pool.getId());
        return Response.created(uriInfo.getBaseUriBuilder().path("api/pools").path(String.valueOf(pool.getId())).build()).build();
    }

    @PATCH
    @Path("/{pool_id:\\d+}")
    @Consumes(CustomMediaType.APPLICATION_POOL_JSON)
    @PreAuthorize("@accessValidator.canUpdatePool(#poolId, #form)")
    public Response updatePool(@PathParam("pool_id") final int poolId, @Valid @NotNull final PoolPatchForm form) {
        poolService.edit(poolId, form.getMinQuantity(), form.getStatus());
        LOGGER.info("Pool patched: {}", poolId);
        return Response.noContent().build();
    }

}
