package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.interfaces.exception.LocationNotFoundException;
import ar.edu.itba.paw.interfaces.service.LocationService;
import ar.edu.itba.paw.models.db.Location;
import ar.edu.itba.paw.webapp.CustomMediaType;
import ar.edu.itba.paw.webapp.dto.LocationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/api/locations")
@Component
public class LocationController {

    @Context
    private UriInfo uriInfo;

    private final LocationService locationService;

    @Autowired
    public LocationController(final LocationService locationService) {
        this.locationService = locationService;
    }

    @GET
    @Produces(CustomMediaType.APPLICATION_LOCATION_JSON)
    public Response listLocations() {
        List<Location> locations = locationService.getAll();
        List<LocationDto> locationDtos = locations.stream().map(LocationDto.mapper(uriInfo)).toList();
        return Response.ok(new GenericEntity<>(locationDtos) {
        }).build();
    }

    @GET
    @Produces(CustomMediaType.APPLICATION_LOCATION_JSON)
    @Path("/{location_id:\\d+}")
    public Response getLocation(@PathParam("location_id") final int locationId) {
        final Location location = locationService.findById(locationId).orElseThrow(LocationNotFoundException::new);
        return Response.ok(LocationDto.fromLocation(uriInfo, location)).build();
    }

}
