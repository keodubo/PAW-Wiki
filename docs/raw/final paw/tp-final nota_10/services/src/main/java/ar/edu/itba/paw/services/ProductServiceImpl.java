package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.ProductDao;
import ar.edu.itba.paw.interfaces.exception.CategoryNotFoundException;
import ar.edu.itba.paw.interfaces.exception.DocumentNotFoundException;
import ar.edu.itba.paw.interfaces.exception.InvalidDocumentVisibilityException;
import ar.edu.itba.paw.interfaces.exception.InvalidProductRetirementException;
import ar.edu.itba.paw.interfaces.exception.ProductNotFoundException;
import ar.edu.itba.paw.interfaces.service.CategoryService;
import ar.edu.itba.paw.interfaces.service.DocumentService;
import ar.edu.itba.paw.interfaces.service.EmailService;
import ar.edu.itba.paw.interfaces.service.PoolService;
import ar.edu.itba.paw.interfaces.service.ProductService;
import ar.edu.itba.paw.interfaces.utils.UriUtils;
import ar.edu.itba.paw.models.db.*;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductDao productDao;
    private final DocumentService documentService;
    private final CategoryService categoryService;
    private final PoolService poolService;
    private final EmailService emailService;

    @Autowired
    public ProductServiceImpl(final ProductDao productDao, final DocumentService documentService, final CategoryService categoryService, @Lazy final PoolService poolService, final EmailService emailService) {
        this.productDao = productDao;
        this.documentService = documentService;
        this.categoryService = categoryService;
        this.poolService = poolService;
        this.emailService = emailService;
    }

    @Transactional
    @Override
    public Product create(final String name, final String description, final double price, final String imageUri, final Company company, final String categoryUri) {
        final int imageId = UriUtils.extractIdFromUri(imageUri);
        final Document image = documentService.findById(imageId).orElseThrow(DocumentNotFoundException::new);
        if (!image.isPublic()) {
            throw new InvalidDocumentVisibilityException();
        }
        final int categoryId = UriUtils.extractIdFromUri(categoryUri);
        final Category category = categoryService.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        final Product product = productDao.create(name, description, price, image, company, category);
        LOGGER.info("Product created: {} (company: {})", product.getId(), company.getId());
        return product;
    }

    @Transactional
    @Override
    public void edit(final int id, final String name, final String description, final Double price, final String imageUri, final String categoryUri) {
        final Product product = productDao.findById(id).orElseThrow(ProductNotFoundException::new);
        final String finalName = name != null ? name : product.getName();
        final String finalDescription = description != null ? description : product.getDescription();
        final double finalPrice = price != null ? price : product.getPrice();

        Document finalImage = product.getImage();
        if (imageUri != null) {
            final int documentId = UriUtils.extractIdFromUri(imageUri);
            finalImage = documentService.findById(documentId).orElseThrow(DocumentNotFoundException::new);
            if (!finalImage.isPublic())
                throw new InvalidDocumentVisibilityException();
        }

        Category finalCategory = product.getCategory();
        if (categoryUri != null) {
            final int categoryId = UriUtils.extractIdFromUri(categoryUri);
            finalCategory = categoryService.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        }

        productDao.edit(id, finalName, finalDescription, finalPrice, finalImage, finalCategory);
        LOGGER.info("Product edited: {}", id);
    }

    @Transactional
    @Override
    public void retire(final int id) {
        final Product product = productDao.findById(id).orElseThrow(ProductNotFoundException::new);
        if (!product.getCanRetire())
            throw new InvalidProductRetirementException();

        poolService.cancelAvailablePoolsAndRejectRequests(id);
        productDao.retire(id);

        final User owner = product.getCompany().getOwner();
        final Category category = product.getCategory();

        emailService.sendProductRetiredEmail(
                owner.getEmail(),
                owner.getPreferredLanguage(),
                product.getName(),
                product.getPrice(),
                category != null ? category.getName() : "",
                LocaleContextHolder.getLocale()
        );
        LOGGER.info("Product retired: {}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Product> findById(final int id) {
        return productDao.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Paginator<Product> filter(final String search, final Integer categoryId, final Integer companyId, final Double priceMin, final Double priceMax, final Boolean active, final int page, final String orderBy, final boolean desc) {
        return productDao.filter(search, categoryId, companyId, priceMin, priceMax, active, Math.max(page, 0), orderBy, desc);
    }

}
