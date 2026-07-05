package ar.edu.itba.paw.interfaces.service;

import ar.edu.itba.paw.models.db.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Optional<Category> findById(final int id);

    List<Category> getAll();

}
