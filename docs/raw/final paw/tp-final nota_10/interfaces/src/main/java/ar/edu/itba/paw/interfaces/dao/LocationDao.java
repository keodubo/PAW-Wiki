package ar.edu.itba.paw.interfaces.dao;

import ar.edu.itba.paw.models.db.Location;

import java.util.List;
import java.util.Optional;

public interface LocationDao {

    Location create(final String name);

    Optional<Location> findById(final int id);

    List<Location> getAll();

}
