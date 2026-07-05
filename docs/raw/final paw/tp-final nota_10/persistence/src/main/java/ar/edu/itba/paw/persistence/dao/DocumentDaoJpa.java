package ar.edu.itba.paw.persistence.dao;

import ar.edu.itba.paw.interfaces.dao.DocumentDao;
import ar.edu.itba.paw.models.db.Document;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Repository
public class DocumentDaoJpa implements DocumentDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Document create(String name, String fileType, byte[] bytes, boolean isPublic) {
        final Document document = new Document(name, fileType, bytes, isPublic);
        entityManager.persist(document);
        return document;
    }

    @Override
    public void delete(final int id) {
        findById(id).ifPresent(document -> entityManager.remove(document));
    }

    @Override
    public Optional<Document> findById(int id) {
        return Optional.ofNullable(entityManager.find(Document.class, id));
    }

}
