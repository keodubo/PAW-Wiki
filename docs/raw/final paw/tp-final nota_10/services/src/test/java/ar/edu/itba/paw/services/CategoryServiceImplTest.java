package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.CategoryDao;
import ar.edu.itba.paw.models.db.Category;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CategoryServiceImplTest {

    private static final int ID = 1;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryDao categoryDao;

    // tests para findById
    @Test
    public void testFindByIdExists() {
        Category category = mock(Category.class);

        when(categoryDao.findById(ID)).thenReturn(Optional.of(category));

        Optional<Category> result = categoryService.findById(ID);

        assertTrue(result.isPresent());
        assertEquals(category, result.get());
        verify(categoryDao).findById(eq(ID));
    }

    @Test
    public void testFindByIdNotExists() {
        when(categoryDao.findById(anyInt())).thenReturn(Optional.empty());

        Optional<Category> result = categoryService.findById(ID);

        assertFalse(result.isPresent());
        verify(categoryDao).findById(eq(ID));
    }

    // tests para getAll
    @Test
    public void testGetAllWithMultipleCategories() {
        Category category1 = mock(Category.class);
        Category category2 = mock(Category.class);
        Category category3 = mock(Category.class);
        List<Category> categories = Arrays.asList(category1, category2, category3);

        when(categoryDao.getAll()).thenReturn(categories);

        List<Category> result = categoryService.getAll();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(category1, result.get(0));
        assertEquals(category2, result.get(1));
        assertEquals(category3, result.get(2));
        verify(categoryDao).getAll();
    }

    @Test
    public void testGetAllWithSingleCategory() {
        Category category = mock(Category.class);
        List<Category> categories = Collections.singletonList(category);

        when(categoryDao.getAll()).thenReturn(categories);

        List<Category> result = categoryService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(category, result.getFirst());
        verify(categoryDao).getAll();
    }

    @Test
    public void testGetAllWithEmptyList() {
        List<Category> categories = Collections.emptyList();

        when(categoryDao.getAll()).thenReturn(categories);

        List<Category> result = categoryService.getAll();

        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
        verify(categoryDao).getAll();
    }

}
