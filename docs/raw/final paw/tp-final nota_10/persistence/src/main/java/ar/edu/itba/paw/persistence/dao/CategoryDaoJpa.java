package ar.edu.itba.paw.persistence.dao;

import ar.edu.itba.paw.interfaces.dao.CategoryDao;
import ar.edu.itba.paw.models.db.Category;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class CategoryDaoJpa implements CategoryDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Category create(String name, String iconName) {
        final Category category = new Category(name, iconName);
        entityManager.persist(category);
        return category;
    }

    @Override
    public Optional<Category> findById(int id) {
        return Optional.ofNullable(entityManager.find(Category.class, id));
    }

    @Override
    public List<Category> getAll() {
        return entityManager.createQuery("from Category order by id desc", Category.class).getResultList();
    }

}
