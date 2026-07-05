package ar.edu.itba.paw.persistence.dao;

import ar.edu.itba.paw.interfaces.dao.LocationDao;
import ar.edu.itba.paw.models.db.Location;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class LocationDaoJpa implements LocationDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Location create(String name) {
        final Location location = new Location(name);
        entityManager.persist(location);
        return location;
    }

    @Override
    public Optional<Location> findById(int id) {
        return Optional.ofNullable(entityManager.find(Location.class, id));
    }

    @Override
    public List<Location> getAll() {
        return entityManager.createQuery("from Location where name != 'Desconocido' order by name", Location.class).getResultList();
    }

}
