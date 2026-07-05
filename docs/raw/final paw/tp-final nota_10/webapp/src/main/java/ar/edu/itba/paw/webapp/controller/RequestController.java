package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.interfaces.exception.RequestNotFoundException;
import ar.edu.itba.paw.interfaces.exception.UserNotFoundException;
import ar.edu.itba.paw.interfaces.service.RequestService;
import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.models.db.Request;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.webapp.CustomMediaType;
import ar.edu.itba.paw.webapp.dto.RequestDto;
import ar.edu.itba.paw.webapp.form.RequestCreateForm;
import ar.edu.itba.paw.webapp.form.RequestPatchForm;
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

@Path("/api/requests")
@Component
public class RequestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestController.class);

    @Context
    private UriInfo uriInfo;

    private final RequestService requestService;
    private final UserService userService;

    @Autowired
    public RequestController(final RequestService requestService, final UserService userService) {
        this.requestService = requestService;
        this.userService = userService;
    }

    @GET
    @Produces(CustomMediaType.APPLICATION_REQUEST_JSON)
    public Response listRequests(
            @QueryParam("search") final String search,
            @QueryParam("company_id") final Integer companyId,
            @QueryParam("product_id") final Integer productId,
            @QueryParam("pool_status") final String poolStatusStr,
            @QueryParam("status") final String requestStatusStr,
            @QueryParam("pool_id") final Integer poolId,
            @QueryParam("user_id") final Integer userId,
            @QueryParam("page") @DefaultValue("0") final int page,
            @QueryParam("order_by") @DefaultValue("id") final String orderBy,
            @QueryParam("desc") @DefaultValue("false") final Boolean desc
    ) {
        Paginator<Request> paginator = requestService.filter(search, companyId, productId, poolStatusStr, requestStatusStr, poolId, userId, page, orderBy, desc);
        List<RequestDto> requests = paginator.getList().stream().map(RequestDto.mapper(uriInfo)).toList();
        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
        Response.ResponseBuilder r = Response.ok(new GenericEntity<>(requests) {
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

    @POST
    @Consumes(CustomMediaType.APPLICATION_REQUEST_JSON)
    @PreAuthorize("@accessValidator.canCreateRequest(#form.poolUri)")
    public Response createRequest(@Valid @NotNull final RequestCreateForm form) {
        final User user = userService.getCurrentUser().orElseThrow(UserNotFoundException::new);
        final Request request = requestService.create(form.getQuantity(), user, form.getPoolUri());
        LOGGER.info("Request created: {}", request.getId());
        return Response.created(uriInfo.getBaseUriBuilder().path("api/requests").path(String.valueOf(request.getId())).build()).build();
    }

    @PATCH
    @Path("/{request_id:\\d+}")
    @Consumes(CustomMediaType.APPLICATION_REQUEST_JSON)
    @PreAuthorize("@accessValidator.canUpdateRequest(principal, #requestId, #form)")
    public Response updateRequest(@PathParam("request_id") final int requestId, @Valid @NotNull final RequestPatchForm form) {
        requestService.edit(requestId, form.getQuantity(), form.getStatus(), form.getDownPaymentUri(), form.getFinalPaymentUri());
        LOGGER.info("Request patched: {}", requestId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{request_id:\\d+}")
    public Response deleteRequest(@PathParam("request_id") final int requestId) {
        requestService.findById(requestId).orElseThrow(RequestNotFoundException::new);
        requestService.delete(requestId);
        LOGGER.info("Request deleted: {}", requestId);
        return Response.noContent().build();
    }

}
