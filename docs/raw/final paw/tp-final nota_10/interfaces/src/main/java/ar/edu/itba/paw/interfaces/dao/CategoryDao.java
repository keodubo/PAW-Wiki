package ar.edu.itba.paw.interfaces.dao;

import ar.edu.itba.paw.models.db.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryDao {

    Category create(final String name, final String iconName);

    Optional<Category> findById(final int id);

    List<Category> getAll();

}
