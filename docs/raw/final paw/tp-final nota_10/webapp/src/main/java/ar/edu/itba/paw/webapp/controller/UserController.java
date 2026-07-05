package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.interfaces.exception.UserNotFoundException;
import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.webapp.CustomMediaType;
import ar.edu.itba.paw.webapp.dto.ReviewerDto;
import ar.edu.itba.paw.webapp.dto.UserDto;
import ar.edu.itba.paw.webapp.auth.filters.UserIntentFilter;
import ar.edu.itba.paw.webapp.form.UserPatchForm;
import ar.edu.itba.paw.webapp.form.UserCreateForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/api/users")
@Component
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Context
    private UriInfo uriInfo;

    private final UserService userService;

    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @GET
    @Produces(CustomMediaType.APPLICATION_USER_JSON)
    public Response listUsers(
            @QueryParam("search") final String search,
            @QueryParam("validated") final Boolean validated,
            @QueryParam("page") @DefaultValue("0") final int page
    ) {
        Paginator<User> paginator = userService.filter(search, validated, page);
        List<UserDto> users = paginator.getList().stream().map(UserDto.mapper(uriInfo)).toList();
        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
        Response.ResponseBuilder r = Response.ok(new GenericEntity<>(users) {
                }).header("X-Total-Count", paginator.getTotalItems())
                .link(uriBuilder.clone().replaceQueryParam("page", 0).build(), "first")
                .link(uriBuilder.clone().replaceQueryParam("page", paginator.getLastPage()).build(), "last");

        if (paginator.getPage() > 0)
            r.link(uriBuilder.clone().replaceQueryParam("page", paginator.getPage() - 1).build(), "prev");
        if (paginator.getPage() < paginator.getLastPage())
            r.link(uriBuilder.clone().replaceQueryParam("page", paginator.getPage() + 1).build(), "next");

        return r.build();
    }

    @HEAD
    @Produces(CustomMediaType.APPLICATION_USER_JSON)
    public Response headUsers() {
        return Response.noContent().build();
    }

    @GET
    @Path("/{user_id:\\d+}")
    @Produces(CustomMediaType.APPLICATION_USER_JSON)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('COMPANY_VERIFIED') and (@accessValidator.isUserSelf(principal, #userId) or @accessValidator.isUserInCompanyProductPool(principal, #userId))) or (authenticated and @accessValidator.isUserSelf(principal, #userId))")
    public Response getUser(@PathParam("user_id") final int userId, @Context final Request request) {
        final User user = userService.findById(userId).orElseThrow(UserNotFoundException::new);
        return Response.ok(UserDto.fromUser(uriInfo, user)).build();
    }

    @GET
    @Path("/{user_id:\\d+}")
    @Produces(CustomMediaType.APPLICATION_USER_REVIEWER_JSON)
    public Response getReviewer(@PathParam("user_id") final int userId) {
        final User user = userService.findById(userId).orElseThrow(UserNotFoundException::new);
        return Response.ok(ReviewerDto.fromReviewer(uriInfo, user)).build();
    }

    @POST
    @Consumes(CustomMediaType.APPLICATION_USER_JSON)
    public Response createUser(@Valid @NotNull final UserCreateForm form) {
        String requestedLang = form.getPreferredLanguage();
        if (requestedLang == null || !(requestedLang.equals("es") || requestedLang.equals("en")))
            requestedLang = LocaleContextHolder.getLocale().getLanguage();
        if (!(requestedLang.equals("es") || requestedLang.equals("en")))
            requestedLang = "en";
        final User user = userService.create(form.getEmail(), form.getPassword(), form.getFirstName(), form.getLastName(), form.getLocationUri(), form.getIsCompany(), requestedLang);
        LOGGER.info("User created: {}", user.getId());
        return Response.created(uriInfo.getBaseUriBuilder().path("api/users").path(String.valueOf(user.getId())).build()).build();
    }

    @PATCH
    @Path("/{user_id:\\d+}")
    @Consumes(CustomMediaType.APPLICATION_USER_JSON)
    @PreAuthorize("@accessValidator.canUpdateUser(principal, #userId, #form)")
    public Response updateUser(
            @PathParam("user_id") final int userId,
            @Valid final UserPatchForm form,
            @Context final HttpServletRequest request
    ) {
        // Analizamos acá el user intent para aprovechar el chequeo de seguridad
        final String intent = (String) request.getAttribute(UserIntentFilter.ACTION_ATTRIBUTE);
        if (UserIntentFilter.ACTION_RESEND_VALIDATION.equals(intent)) {
            userService.resendValidationEmail(userId);
            LOGGER.info("Validation email resent for user: {}", userId);
            return Response.noContent().build();
        }

        if (form == null)
            throw new BadRequestException("Request body is required for user update");

        userService.edit(userId, form.getFirstName(), form.getLastName(), form.getLocationUri(), form.getPassword(), form.getPreferredLanguage(), form.getBlockLevel());
        LOGGER.info("User patched: {}", userId);
        return Response.noContent().build();
    }


}
