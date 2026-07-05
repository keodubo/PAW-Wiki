package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.DocumentDao;
import ar.edu.itba.paw.interfaces.service.DocumentService;
import ar.edu.itba.paw.models.db.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private final DocumentDao documentDao;

    @Autowired
    public DocumentServiceImpl(final DocumentDao documentDao) {
        this.documentDao = documentDao;
    }

    @Transactional
    @Override
    public Document create(final String name, final String fileType, final byte[] bytes, final boolean isPublic) {
        final Document document = documentDao.create(name, fileType, bytes, isPublic);
        LOGGER.info("Document created: {} (type: {}, public: {})", document.getId(), fileType, isPublic);
        return document;
    }

    @Transactional
    @Override
    public void delete(final int id) {
        documentDao.delete(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Document> findById(final int id) {
        return documentDao.findById(id);
    }

}
