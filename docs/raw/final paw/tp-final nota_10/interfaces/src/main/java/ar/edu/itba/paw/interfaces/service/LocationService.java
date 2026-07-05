package ar.edu.itba.paw.interfaces.service;

import ar.edu.itba.paw.models.db.Location;

import java.util.List;
import java.util.Optional;

public interface LocationService {

    Optional<Location> findById(final int id);

    List<Location> getAll();

}
