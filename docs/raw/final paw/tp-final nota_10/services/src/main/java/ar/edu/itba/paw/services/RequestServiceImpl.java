package ar.edu.itba.paw.services;

import ar.edu.itba.paw.interfaces.dao.RequestDao;
import ar.edu.itba.paw.interfaces.exception.*;
import ar.edu.itba.paw.interfaces.service.DocumentService;
import ar.edu.itba.paw.interfaces.service.EmailService;
import ar.edu.itba.paw.interfaces.service.PoolService;
import ar.edu.itba.paw.interfaces.service.RequestService;
import ar.edu.itba.paw.interfaces.utils.UriUtils;
import ar.edu.itba.paw.models.db.*;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class RequestServiceImpl implements RequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestServiceImpl.class);

    private final RequestDao requestDao;
    private final PoolService poolService;
    private final DocumentService documentService;
    private final EmailService emailService;

    @Autowired
    public RequestServiceImpl(final RequestDao requestDao, final PoolService poolService, final DocumentService documentService, final EmailService emailService) {
        this.requestDao = requestDao;
        this.poolService = poolService;
        this.documentService = documentService;
        this.emailService = emailService;
    }

    @Transactional
    @Override
    public Request create(final int quantity, final User user, final String poolUri) {
        final int poolId = UriUtils.extractIdFromUri(poolUri);
        final Pool pool = poolService.findById(poolId).orElseThrow(PoolNotFoundException::new);
        final Request request = requestDao.create(quantity, user, pool);

        final Product product = pool.getProduct();
        final Category category = product.getCategory();
        final Location location = pool.getLocation();

        emailService.sendRequestMadeEmail(
                user.getEmail(),
                user.getPreferredLanguage(),
                pool.getId(),
                product.getName(),
                product.getPrice(),
                category != null ? category.getName() : "",
                location != null ? location.getName() : "",
                quantity,
                pool.getDownPayment(),
                LocaleContextHolder.getLocale()
        );
        LOGGER.info("Request created: {} (user: {}, pool: {})", request.getId(), user.getId(), poolId);
        return request;
    }

    @Transactional
    @Override
    public void edit(final int id, final Integer quantity, final String statusStr, final String downPaymentUri, final String finalPaymentUri) {
        final Request request = requestDao.findById(id).orElseThrow(RequestNotFoundException::new);

        if (quantity != null) {
            requestDao.edit(id, quantity, request.getDownPayment(), request.getFinalPayment());
        }

        if (statusStr != null) {
            final Request.Status status = Request.Status.valueOf(statusStr);
            setStatus(id, status);
        }

        if (downPaymentUri != null) {
            final int documentId = UriUtils.extractIdFromUri(downPaymentUri);
            final Document downPayment = documentService.findById(documentId).orElseThrow(DocumentNotFoundException::new);
            if (downPayment.isPublic()) {
                throw new InvalidDocumentVisibilityException();
            }
            requestDao.edit(id, request.getQuantity(), downPayment, request.getFinalPayment());
        }

        if (finalPaymentUri != null) {
            final int documentId = UriUtils.extractIdFromUri(finalPaymentUri);
            final Document finalPayment = documentService.findById(documentId).orElseThrow(DocumentNotFoundException::new);
            if (finalPayment.isPublic()) {
                throw new InvalidDocumentVisibilityException();
            }
            requestDao.edit(id, request.getQuantity(), request.getDownPayment(), finalPayment);
        }
    }

    @Transactional
    @Override
    public void delete(final int id) {
        final Request request = requestDao.findById(id).orElseThrow(RequestNotFoundException::new);
        final Pool pool = request.getPool();
        final Product product = pool.getProduct();
        final Category category = product.getCategory();
        final Location location = pool.getLocation();
        final User user = request.getUser();

        emailService.sendRequestDeletedEmail(
                user.getEmail(),
                user.getPreferredLanguage(),
                pool.getId(),
                product.getName(),
                product.getPrice(),
                category != null ? category.getName() : "",
                location != null ? location.getName() : "",
                request.getQuantity(),
                pool.getDownPayment(),
                LocaleContextHolder.getLocale()
        );
        requestDao.delete(id);
        LOGGER.info("Request deleted: {}", id);
    }

    @Transactional
    @Override
    public Request.Status setStatus(final int id, final Request.Status status) {
        final Request request = requestDao.findById(id).orElseThrow(RequestNotFoundException::new);
        final Request.Status oldStatus = request.getStatus();
        requestDao.setStatus(id, status);

        final Pool pool = request.getPool();
        final Product product = pool.getProduct();
        final Category category = product.getCategory();
        final Location location = pool.getLocation();
        final User user = request.getUser();

        switch (status) {
            case ACCEPTED: {
                emailService.sendRequestAcceptedEmail(
                        user.getEmail(),
                        user.getPreferredLanguage(),
                        pool.getId(),
                        product.getName(),
                        product.getPrice(),
                        category != null ? category.getName() : "",
                        location != null ? location.getName() : "",
                        request.getQuantity(),
                        pool.getDownPayment(),
                        LocaleContextHolder.getLocale()
                );

                if (pool.getAcceptedRequestsSum() < pool.getMinQuantity() && pool.getAcceptedRequestsSum() + request.getQuantity() >= pool.getMinQuantity()) {
                    final Company company = product.getCompany();
                    final List<List<String>> recipientsData = new ArrayList<>(pool.getRequests().stream()
                            .filter(r -> r.getStatus() == Request.Status.ACCEPTED)
                            .map(p -> {
                                final User u = p.getUser();
                                return List.of(u.getEmail(), u.getPreferredLanguage());
                            })
                            .toList());

                    final User owner = company.getOwner();
                    recipientsData.add(List.of(owner.getEmail(), owner.getPreferredLanguage()));

                    emailService.sendPoolFullEmail(
                            pool.getId(),
                            product.getName(),
                            company.getName(),
                            category != null ? category.getName() : "",
                            location != null ? location.getName() : "",
                            recipientsData,
                            LocaleContextHolder.getLocale()
                    );
                }
            }
            break;

            case REJECTED: {
                emailService.sendRequestRejectedEmail(
                        user.getEmail(),
                        user.getPreferredLanguage(),
                        pool.getId(),
                        product.getName(),
                        product.getPrice(),
                        category != null ? category.getName() : "",
                        location != null ? location.getName() : "",
                        request.getQuantity(),
                        pool.getDownPayment(),
                        LocaleContextHolder.getLocale()
                );
            }
            break;

            case DELIVERED: {
                emailService.sendRequestDeliveredEmail(
                        user.getEmail(),
                        user.getPreferredLanguage(),
                        pool.getId(),
                        product.getName(),
                        product.getPrice(),
                        category != null ? category.getName() : "",
                        location != null ? location.getName() : "",
                        request.getQuantity(),
                        pool.getDownPayment(),
                        LocaleContextHolder.getLocale()
                );
            }
            break;
        }

        LOGGER.info("Request status changed: {} (old: {}, new: {})", id, oldStatus, status);
        return oldStatus;
    }

    @Transactional
    @Override
    public void rejectPendingRequests(final int productId) {
        requestDao.rejectPendingRequests(productId);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Request> findById(final int id) {
        return requestDao.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Request> findFinishedByProductAndUser(final int productId, final int userId) {
        return requestDao.findFinishedByProductAndUser(productId, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Request> findByCompanyAndUser(final int companyId, final int userId) {
        return requestDao.findByCompanyAndUser(companyId, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Request> findByPayment(final int paymentId) {
        return requestDao.findByPayment(paymentId);
    }

    @Transactional(readOnly = true)
    @Override
    public Paginator<Request> filter(final String search, final Integer companyId, final Integer productId, final String poolStatus, final String requestStatus, final Integer poolId, final Integer userId, final int page, final String orderBy, final boolean desc) {
        if (poolStatus != null) {
            try {
                Pool.Status.valueOf(poolStatus);
            } catch (Exception e) {
                throw new InvalidPoolStatusException();
            }
        }

        if (requestStatus != null) {
            try {
                Request.Status.valueOf(requestStatus);
            } catch (Exception e) {
                throw new InvalidRequestStatusException();
            }
        }

        return requestDao.filter(search, companyId, productId, poolStatus, requestStatus, poolId, userId, page, orderBy, desc);
    }

}
