package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.interfaces.exception.CompanyNotFoundException;
import ar.edu.itba.paw.interfaces.exception.UserNotFoundException;
import ar.edu.itba.paw.interfaces.service.CompanyService;
import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.webapp.CustomMediaType;
import ar.edu.itba.paw.webapp.dto.CompanyDto;
import ar.edu.itba.paw.webapp.form.CompanyPatchForm;
import ar.edu.itba.paw.webapp.form.CompanyCreateForm;
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

@Path("/api/companies")
@Component
public class CompanyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyController.class);

    @Context
    private UriInfo uriInfo;

    private final CompanyService companyService;
    private final UserService userService;

    @Autowired
    public CompanyController(final CompanyService companyService, final UserService userService) {
        this.companyService = companyService;
        this.userService = userService;
    }

    @GET
    @Produces(CustomMediaType.APPLICATION_COMPANY_JSON)
    public Response listCompanies(
            @QueryParam("search") final String search,
            @QueryParam("owner_id") final Integer ownerId,
            @QueryParam("validated") final Boolean validated,
            @QueryParam("page") @DefaultValue("0") final int page
    ) {

        Paginator<Company> paginator = companyService.filter(search, ownerId, validated, page);
        List<CompanyDto> companies = paginator.getList().stream().map(CompanyDto.mapper(uriInfo)).toList();
        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
        Response.ResponseBuilder r = Response.ok(new GenericEntity<>(companies) {
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
    @Path("/{company_id:\\d+}")
    @Produces(CustomMediaType.APPLICATION_COMPANY_JSON)
    public Response getCompany(@PathParam("company_id") final int companyId) {
        final Company company = companyService.findById(companyId).orElseThrow(CompanyNotFoundException::new);
        return Response.ok(CompanyDto.fromCompany(uriInfo, company)).build();
    }

    @POST
    @Consumes(CustomMediaType.APPLICATION_COMPANY_JSON)
    public Response createCompany(@Valid @NotNull final CompanyCreateForm form) {
        final User user = userService.getCurrentUser().orElseThrow(UserNotFoundException::new);
        final Company company = companyService.create(form.getName(), form.getAddress(), form.getEmail(), form.getPhone(), form.getCbu(), form.getImageUri(), user);
        LOGGER.info("Company created: {}", company.getId());
        return Response.created(uriInfo.getBaseUriBuilder().path("api/companies").path(String.valueOf(company.getId())).build()).build();
    }

    @PATCH
    @Path("/{company_id:\\d+}")
    @Consumes(CustomMediaType.APPLICATION_COMPANY_JSON)
    @PreAuthorize("@accessValidator.canUpdateCompany(principal, #companyId, #form)")
    public Response updateCompany(@PathParam("company_id") final int companyId, @Valid @NotNull final CompanyPatchForm form) {
        companyService.edit(companyId, form.getAddress(), form.getEmail(), form.getPhone(), form.getCbu(), form.getImageUri(), form.getValidated());
        LOGGER.info("Company patched: {}", companyId);
        return Response.noContent().build();
    }

}
