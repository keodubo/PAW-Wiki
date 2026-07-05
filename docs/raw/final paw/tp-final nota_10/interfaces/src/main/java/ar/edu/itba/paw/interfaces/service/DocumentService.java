package ar.edu.itba.paw.interfaces.service;

import ar.edu.itba.paw.models.db.Document;

import java.util.Optional;

public interface DocumentService {

    Document create(final String name, final String fileType, final byte[] bytes, final boolean isPublic);

    void delete(int id);

    Optional<Document> findById(final int id);

}
