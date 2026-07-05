package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.LocationDao;
import ar.edu.itba.paw.models.db.Location;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocationServiceImplTest {

    private static final int ID = 1;

    @InjectMocks
    private LocationServiceImpl locationService;

    @Mock
    private LocationDao locationDao;

    // tests para findById
    @Test
    public void testFindByIdExists() {
        Location location = mock(Location.class);

        when(locationDao.findById(ID)).thenReturn(Optional.of(location));

        Optional<Location> result = locationService.findById(ID);

        assertTrue(result.isPresent());
        assertEquals(location, result.get());
        verify(locationDao).findById(eq(ID));
    }

    @Test
    public void testFindByIdNotExists() {
        when(locationDao.findById(ID)).thenReturn(Optional.empty());

        Optional<Location> result = locationService.findById(ID);

        assertFalse(result.isPresent());
        verify(locationDao).findById(eq(ID));
    }

    // tests para getAll
    @Test
    public void testGetAllWithMultipleLocations() {
        Location location1 = mock(Location.class);
        Location location2 = mock(Location.class);
        Location location3 = mock(Location.class);
        List<Location> locations = Arrays.asList(location1, location2, location3);

        when(locationDao.getAll()).thenReturn(locations);

        List<Location> result = locationService.getAll();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(location1, result.get(0));
        assertEquals(location2, result.get(1));
        assertEquals(location3, result.get(2));
        verify(locationDao).getAll();
    }

    @Test
    public void testGetAllWithSingleLocation() {
        Location location = mock(Location.class);
        List<Location> locations = Collections.singletonList(location);

        when(locationDao.getAll()).thenReturn(locations);

        List<Location> result = locationService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(location, result.getFirst());
        verify(locationDao).getAll();
    }

    @Test
    public void testGetAllWithEmptyList() {
        when(locationDao.getAll()).thenReturn(Collections.emptyList());

        List<Location> result = locationService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(locationDao).getAll();
    }

}
