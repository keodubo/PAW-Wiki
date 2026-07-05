package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.interfaces.exception.UserNotAssociatedWithCompanyException;
import ar.edu.itba.paw.interfaces.exception.UserNotFoundException;
import ar.edu.itba.paw.interfaces.service.ReportService;
import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.models.db.Report;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import ar.edu.itba.paw.webapp.CustomMediaType;
import ar.edu.itba.paw.webapp.dto.ReportDto;
import ar.edu.itba.paw.webapp.form.ReportForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/api/users/{user_id:\\d+}/reports")
@Component
public class ReportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

    @Context
    private UriInfo uriInfo;

    private final ReportService reportService;
    private final UserService userService;

    @Autowired
    public ReportController(final ReportService reportService, final UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @GET
    @Produces(CustomMediaType.APPLICATION_REPORT_JSON)
    public Response listReports(
            @PathParam("user_id") final int userId,
            @QueryParam("page") @DefaultValue("0") final int page
    ) {
        Paginator<Report> paginator = reportService.filter(userId, page);
        List<ReportDto> reports = paginator.getList().stream().map(ReportDto.mapper(uriInfo, userId)).toList();
        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
        Response.ResponseBuilder r = Response.ok(new GenericEntity<>(reports) {
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
    @Consumes(CustomMediaType.APPLICATION_REPORT_JSON)
    public Response createReport(@PathParam("user_id") final int userId, @Valid @NotNull final ReportForm form) {
        final User currentUser = userService.getCurrentUser().orElseThrow(UserNotFoundException::new);

        if (currentUser.getCompany() == null)
            throw new UserNotAssociatedWithCompanyException();

        final Report report = reportService.create(form.getDescription(), currentUser.getCompany(), userId);
        LOGGER.info("Report created: {}", report.getId());
        return Response.created(
                uriInfo.getBaseUriBuilder()
                        .path("api/users")
                        .path(String.valueOf(userId))
                        .path("reports")
                        .path(String.valueOf(report.getId()))
                        .build()
        ).build();
    }

}

