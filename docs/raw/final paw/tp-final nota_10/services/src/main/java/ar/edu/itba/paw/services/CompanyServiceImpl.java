package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.CompanyDao;
import ar.edu.itba.paw.interfaces.exception.CompanyNotFoundException;
import ar.edu.itba.paw.interfaces.exception.DocumentNotFoundException;
import ar.edu.itba.paw.interfaces.exception.InvalidDocumentVisibilityException;
import ar.edu.itba.paw.interfaces.service.CompanyService;
import ar.edu.itba.paw.interfaces.service.DocumentService;
import ar.edu.itba.paw.interfaces.service.EmailService;
import ar.edu.itba.paw.interfaces.service.UserService;
import ar.edu.itba.paw.interfaces.utils.UriUtils;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CompanyServiceImpl implements CompanyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyServiceImpl.class);

    private final CompanyDao companyDao;
    private final UserService userService;
    private final DocumentService documentService;
    private final EmailService emailService;

    @Autowired
    public CompanyServiceImpl(final CompanyDao companyDao, final UserService userService, final DocumentService documentService, final EmailService emailService) {
        this.companyDao = companyDao;
        this.userService = userService;
        this.documentService = documentService;
        this.emailService = emailService;
    }

    @Transactional
    @Override
    public Company create(final String name, final String address, final String email, final String phone, final String cbu, final String imageUri, final User owner) {
        final int imageId = UriUtils.extractIdFromUri(imageUri);
        final Document image = documentService.findById(imageId).orElseThrow(DocumentNotFoundException::new);
        if (!image.isPublic())
            throw new InvalidDocumentVisibilityException();

        final Company company = companyDao.create(name, address, email, phone, false, cbu, image, owner);
        final User admin = userService.findAdmin().orElse(null);

        emailService.sendCompanyRegisterEmail(owner, company, admin, LocaleContextHolder.getLocale());
        LOGGER.info("Company created: {} (owner: {})", company.getId(), owner.getId());
        return company;
    }

    @Transactional
    @Override
    public void edit(final int id, final String address, final String email, final String phone, final String cbu, final String imageUri, final Boolean validated) {
        Company company = companyDao.findById(id).orElseThrow(CompanyNotFoundException::new);
        final String finalAddress = address != null ? address : company.getAddress();
        final String finalEmail = email != null ? email : company.getEmail();
        final String finalPhone = phone != null ? phone : company.getPhone();
        final String finalCbu = cbu != null ? cbu : company.getCbu();

        Document finalImage = company.getImage();
        if (imageUri != null) {
            final int documentId = UriUtils.extractIdFromUri(imageUri);
            finalImage = documentService.findById(documentId).orElseThrow(DocumentNotFoundException::new);
            if (!finalImage.isPublic())
                throw new InvalidDocumentVisibilityException();
        }

        boolean finalValidated = company.isValidated();
        if (validated != null && validated && !company.isValidated()) {
            finalValidated = true;
            LOGGER.info("Company validated: {}", id);
        }

        companyDao.edit(id, finalAddress, finalEmail, finalPhone, finalValidated, finalCbu, finalImage);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Company> findById(final int id) {
        return companyDao.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Paginator<Company> filter(final String search, final Integer ownerId, final Boolean validated, final int page) {
        return companyDao.filter(search, ownerId, validated, page);
    }

}
