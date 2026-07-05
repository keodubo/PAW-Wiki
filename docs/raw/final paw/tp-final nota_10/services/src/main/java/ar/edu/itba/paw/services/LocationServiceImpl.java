package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.LocationDao;
import ar.edu.itba.paw.interfaces.service.LocationService;
import ar.edu.itba.paw.models.db.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationDao locationDao;

    @Autowired
    public LocationServiceImpl(final LocationDao locationDao) {
        this.locationDao = locationDao;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Location> findById(final int id) {
        return locationDao.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Location> getAll() {
        return locationDao.getAll();
    }

}
