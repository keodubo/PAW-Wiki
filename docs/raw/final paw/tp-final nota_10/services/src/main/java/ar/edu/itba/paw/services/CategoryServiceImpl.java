package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.CategoryDao;
import ar.edu.itba.paw.interfaces.service.CategoryService;
import ar.edu.itba.paw.models.db.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;

    @Autowired
    public CategoryServiceImpl(final CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Category> findById(final int id) {
        return categoryDao.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Category> getAll() {
        return categoryDao.getAll();
    }

}
