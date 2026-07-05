package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.interfaces.exception.InvalidUriException;
import ar.edu.itba.paw.interfaces.service.*;
import ar.edu.itba.paw.interfaces.utils.UriUtils;
import ar.edu.itba.paw.models.db.*;
import ar.edu.itba.paw.webapp.form.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AccessValidator {

    private final UserService userService;
    private final ProductService productService;
    private final PoolService poolService;
    private final RequestService requestService;
    private final ReviewService reviewService;
    private final CompanyService companyService;
    private final DocumentService documentService;

    @Autowired
    public AccessValidator(final UserService userService, final ProductService productService, final PoolService poolService, final RequestService requestService, final ReviewService reviewService, final CompanyService companyService, DocumentService documentService) {
        this.userService = userService;
        this.productService = productService;
        this.poolService = poolService;
        this.requestService = requestService;
        this.reviewService = reviewService;
        this.companyService = companyService;
        this.documentService = documentService;
    }

    public boolean isUserSelf(final AuthUser authUser, final String userId) {
        return authUser.getUserId() == Integer.parseInt(userId);
    }

    public boolean canUpdateUser(final AuthUser authUser, final String userId, final UserPatchForm form) {
        if (form == null)
            return true;

        final Optional<User> user = userService.findById(Integer.parseInt(userId));
        if (user.isEmpty())
            return false;

        final User originalUser = user.get();

        final boolean changingFirstName = form.getFirstName() != null && !form.getFirstName().equals(originalUser.getFirstName());
        final boolean changingLastName = form.getLastName() != null && !form.getLastName().equals(originalUser.getLastName());
        final boolean changingLocation = form.getLocationUri() != null && !isSameLocation(originalUser.getLocation(), form.getLocationUri());
        final boolean changingPassword = form.getPassword() != null;
        final boolean changingPreferredLanguage = form.getPreferredLanguage() != null && !form.getPreferredLanguage().equals(originalUser.getPreferredLanguage());
        final boolean changingBlockLevel = form.getBlockLevel() != null && !form.getBlockLevel().equals(originalUser.getBlockLevel());

        final boolean isAdmin = authUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        final boolean isSelf = isUserSelf(authUser, userId);

        if (isAdmin && !isSelf)
            return !changingFirstName && !changingLastName && !changingLocation && !changingPassword && !changingPreferredLanguage;

        return !changingBlockLevel;
    }

    public boolean isCompanyOwner(final AuthUser authUser, final String companyId) {
        final Optional<User> user = userService.findById(authUser.getUserId());
        return user.isPresent() && user.get().getCompany() != null && user.get().getCompany().getId() == Integer.parseInt(companyId);
    }

    public boolean canUpdateCompany(final AuthUser authUser, final String companyId, final CompanyPatchForm form) {
        if (form == null)
            return true;

        final boolean isAdmin = authUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isCompanyOwner(authUser, companyId) && !isAdmin)
            return false;

        final Optional<Company> company = companyService.findById(Integer.parseInt(companyId));
        if (company.isEmpty())
            return false;

        final Company originalCompany = company.get();

        final boolean changingAddress = form.getAddress() != null && !form.getAddress().equals(originalCompany.getAddress());
        final boolean changingEmail = form.getEmail() != null && !form.getEmail().equals(originalCompany.getEmail());
        final boolean changingPhone = form.getPhone() != null && !form.getPhone().equals(originalCompany.getPhone());
        final boolean changingCbu = form.getCbu() != null && !form.getCbu().equals(originalCompany.getCbu());
        final boolean changingImage = form.getImageUri() != null && !isSameImage(originalCompany.getImage(), form.getImageUri());
        final boolean changingValidated = form.getValidated() != null && !form.getValidated().equals(originalCompany.isValidated());

        if (isAdmin)
            return !changingAddress && !changingEmail && !changingPhone && !changingCbu && !changingImage;

        return !changingValidated;
    }

    public boolean isProductOwnerUri(final AuthUser authUser, final String productUri) {
        try {
            final int productId = UriUtils.extractIdFromUri(productUri);
            return isProductOwnerId(authUser, String.valueOf(productId));
        } catch (InvalidUriException e) {
            return false;
        }
    }

    public boolean isProductOwnerId(final AuthUser authUser, final String productId) {
        final Optional<Product> product = productService.findById(Integer.parseInt(productId));
        final Optional<User> user = userService.findById(authUser.getUserId());
        return product.isPresent() && user.isPresent() && product.get().getCompany().getId() == user.get().getCompany().getId();
    }

    public boolean productIsActive(final String productUri) {
        if (productUri == null || productUri.isEmpty())
            return false;
        final Optional<Product> product = productService.findById(UriUtils.extractIdFromUri(productUri));
        return product.isPresent() && product.get().getActive();
    }

    public boolean canUpdateProduct(final String productId, final ProductPatchForm form) {
        if (form == null)
            return true;
        final Optional<Product> product = productService.findById(Integer.parseInt(productId));
        if (product.isEmpty() || !product.get().getActive())
            return false;
        if (form.getActive() != null && !form.getActive())
            return product.get().getCanRetire();
        return true;
    }

    public boolean canUpdatePool(final String poolId, final PoolPatchForm form) {
        if (form == null)
            return true;

        final Optional<Pool> pool = poolService.findById(Integer.parseInt(poolId));
        if (pool.isEmpty())
            return false;

        final Pool originalPool = pool.get();
        final boolean changingStatus = form.getStatus() != null && !form.getStatus().equals(originalPool.getStatus().name());

        if (changingStatus)
            return canChangePoolStatus(originalPool, form.getStatus());

        return List.of(Pool.Status.AVAILABLE, Pool.Status.PAUSED).contains(originalPool.getStatus());
    }

    public boolean canCreateRequest(final String poolUri) {
        try {
            final int poolId = UriUtils.extractIdFromUri(poolUri);
            final Optional<Pool> pool = poolService.findById(poolId);
            return pool.isPresent() && pool.get().getStatus() == Pool.Status.AVAILABLE;
        } catch (InvalidUriException e) {
            return false;
        }
    }

    public boolean canUpdateRequest(final AuthUser authUser, final String requestId, final RequestPatchForm form) {
        if (form == null)
            return true;

        final Optional<Request> request = requestService.findById(Integer.parseInt(requestId));
        if (request.isEmpty())
            return false;

        final Request originalRequest = request.get();

        final boolean changingQuantity = form.getQuantity() != null && !form.getQuantity().equals(originalRequest.getQuantity());
        final boolean changingStatus = form.getStatus() != null && !form.getStatus().equals(originalRequest.getStatus().name());
        final boolean changingDownPayment = form.getDownPaymentUri() != null && !isSameDocument(originalRequest.getDownPayment(), form.getDownPaymentUri());
        final boolean changingFinalPayment = form.getFinalPaymentUri() != null && !isSameDocument(originalRequest.getFinalPayment(), form.getFinalPaymentUri());

        final boolean isCompanyVerified = authUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_COMPANY_VERIFIED"));
        final boolean isClient = authUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"));

        if (isCompanyVerified) {
            if (changingQuantity || changingDownPayment || changingFinalPayment)
                return false;
            if (changingStatus)
                return canChangeRequestStatus(originalRequest, form.getStatus());
            return true;
        }

        if (isClient) {
            if (changingStatus)
                return false;
            if (changingQuantity)
                return List.of(Pool.Status.AVAILABLE, Pool.Status.PAUSED).contains(request.get().getPool().getStatus()) && request.get().getStatus() == Request.Status.PENDING && request.get().getDownPayment() == null;
            return true;
        }

        return false;
    }

    public boolean canDeleteRequest(final AuthUser authUser, final String requestId) {
        final Optional<Request> request = requestService.findById(Integer.parseInt(requestId));
        return request.isPresent()
                && request.get().getUser().getId() == authUser.getUserId()
                && List.of(Pool.Status.AVAILABLE, Pool.Status.PAUSED).contains(request.get().getPool().getStatus())
                && request.get().getStatus() == Request.Status.PENDING;
    }

    public boolean hasInteractedWithProduct(final AuthUser authUser, final String productId) {
        final Optional<Request> request = requestService.findFinishedByProductAndUser(Integer.parseInt(productId), authUser.getUserId());
        return request.isPresent();
    }

    public boolean hasNotReviewedProduct(final AuthUser authUser, final String productId) {
        final Optional<Review> review = reviewService.findByUserAndProduct(authUser.getUserId(), Integer.parseInt(productId));
        return review.isEmpty();
    }

    public boolean isPoolOwner(final AuthUser authUser, final String poolId) {
        final Optional<Pool> pool = poolService.findById(Integer.parseInt(poolId));
        return pool.isPresent() && isProductOwnerId(authUser, String.valueOf(pool.get().getProduct().getId()));
    }

    public boolean isRequestOwner(final AuthUser authUser, final String requestId) {
        final Optional<Request> request = requestService.findById(Integer.parseInt(requestId));
        final Optional<User> user = userService.findById(authUser.getUserId());
        return request.isPresent() && user.isPresent() && request.get().getUser().getId() == user.get().getId();
    }

    public boolean isRequestPoolOwner(final AuthUser authUser, final String requestId) {
        final Optional<Request> request = requestService.findById(Integer.parseInt(requestId));
        return request.isPresent() && isPoolOwner(authUser, String.valueOf(request.get().getPool().getId()));
    }

    public boolean isReviewOwner(final AuthUser authUser, final String reviewId) {
        final Optional<Review> review = reviewService.findById(Integer.parseInt(reviewId));
        final Optional<User> user = userService.findById(authUser.getUserId());
        return review.isPresent() && user.isPresent()
                && (review.get().getReviewer().getId() == authUser.getUserId()
                || review.get().getProduct().getCompany().getId() == user.get().getCompany().getId());
    }

    public boolean isNotReceipt(final Authentication auth, final String documentId) {
        final Optional<Document> document = documentService.findById(Integer.parseInt(documentId));
        if (document.isEmpty() || document.get().isPublic())
            return true;

        final Optional<Request> requestOptional = requestService.findByPayment(Integer.parseInt(documentId));
        if (requestOptional.isEmpty())
            return true;

        if (!(auth.getPrincipal() instanceof AuthUser u))
            return false;

        final Optional<User> userOptional = userService.findById(u.getUserId());
        if (userOptional.isEmpty())
            return false;

        final Request request = requestOptional.get();
        final User user = userOptional.get();
        final User requester = request.getUser();
        final Company company = request.getPool().getProduct().getCompany();
        final Company userCompany = user.getCompany();

        return requester.getId() == user.getId() || (userCompany != null && company.getId() == userCompany.getId());
    }

    public boolean isUserInCompanyProductPool(final AuthUser authUser, final String userId) {
        final Optional<User> currentUser = userService.findById(authUser.getUserId());
        if (currentUser.isEmpty() || currentUser.get().getCompany() == null)
            return false;

        final int companyId = currentUser.get().getCompany().getId();
        final Optional<Request> request = requestService.findByCompanyAndUser(companyId, Integer.parseInt(userId));
        return request.isPresent();
    }

    // PRIVATE UTILS
    private boolean canChangePoolStatus(final Pool pool, final String statusStr) {
        if (statusStr == null || statusStr.isEmpty())
            return false;

        final Pool.Status newStatus;
        try {
            newStatus = Pool.Status.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return switch (newStatus) {
            case AVAILABLE -> canAwaitPool(pool);
            case PAUSED -> canPausePool(pool);
            case CANCELLED -> canCancelPool(pool);
            case DELIVERING -> canDeliverPool(pool);
            case FINISHED -> canFinishPool(pool);
        };
    }

    private boolean canAwaitPool(final Pool pool) {
        if (pool.getStatus() == Pool.Status.DELIVERING && pool.getDeliveredRequestsCount() == 0)
            return true;
        return List.of(Pool.Status.AVAILABLE, Pool.Status.PAUSED).contains(pool.getStatus());
    }

    private boolean canPausePool(final Pool pool) {
        return pool.getStatus() == Pool.Status.AVAILABLE;
    }

    private boolean canCancelPool(final Pool pool) {
        return List.of(Pool.Status.AVAILABLE, Pool.Status.PAUSED).contains(pool.getStatus())
                && pool.getAcceptedRequestsCount() == 0 && pool.getDeliveredRequestsCount() == 0;
    }

    private boolean canDeliverPool(final Pool pool) {
        return List.of(Pool.Status.AVAILABLE, Pool.Status.PAUSED).contains(pool.getStatus())
                && pool.getAcceptedRequestsSum() >= pool.getMinQuantity();
    }

    private boolean canFinishPool(final Pool pool) {
        return pool.getStatus() == Pool.Status.DELIVERING && pool.getAcceptedRequestsCount() == 0;
    }

    private boolean canChangeRequestStatus(final Request request, final String statusStr) {
        if (statusStr == null || statusStr.isEmpty())
            return false;

        final Request.Status newStatus;
        try {
            newStatus = Request.Status.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return switch (newStatus) {
            case PENDING -> canAwaitRequest(request);
            case ACCEPTED -> canAcceptRequest(request);
            case REJECTED -> canRejectRequest(request);
            case DELIVERED -> canDeliverRequest(request);
        };
    }

    private boolean canAwaitRequest(final Request request) {
        return request.getPool().getStatus() == Pool.Status.AVAILABLE
                && request.getStatus() == Request.Status.ACCEPTED;
    }

    private boolean canAcceptRequest(final Request request) {
        return List.of(Pool.Status.AVAILABLE, Pool.Status.DELIVERING).contains(request.getPool().getStatus())
                && List.of(Request.Status.PENDING, Request.Status.DELIVERED).contains(request.getStatus())
                && (request.getPool().getDownPayment() == 0 || request.getDownPayment() != null);
    }

    private boolean canRejectRequest(final Request request) {
        return request.getPool().getStatus() == Pool.Status.AVAILABLE
                && request.getStatus() == Request.Status.PENDING;
    }

    private boolean canDeliverRequest(final Request request) {
        return request.getPool().getStatus() == Pool.Status.DELIVERING
                && request.getStatus() == Request.Status.ACCEPTED
                && request.getFinalPayment() != null;
    }

    private boolean isSameLocation(final Location location, final String locationUri) {
        if (location == null && (locationUri == null || locationUri.isEmpty()))
            return true;
        if (location == null || locationUri == null || locationUri.isEmpty())
            return false;

        try {
            final int locationIdFromUri = UriUtils.extractIdFromUri(locationUri);
            return location.getId() == locationIdFromUri;
        } catch (InvalidUriException e) {
            return false;
        }
    }

    private boolean isSameImage(final Document image, final String imageUri) {
        if (image == null && (imageUri == null || imageUri.isEmpty()))
            return true;
        if (image == null || imageUri == null || imageUri.isEmpty())
            return false;

        try {
            final int imageIdFromUri = UriUtils.extractIdFromUri(imageUri);
            return image.getId() == imageIdFromUri;
        } catch (InvalidUriException e) {
            return false;
        }
    }

    private boolean isSameDocument(final Document document, final String documentUri) {
        if (document == null && (documentUri == null || documentUri.isEmpty()))
            return true;
        if (document == null || documentUri == null || documentUri.isEmpty())
            return false;

        try {
            final int documentIdFromUri = UriUtils.extractIdFromUri(documentUri);
            return document.getId() == documentIdFromUri;
        } catch (InvalidUriException e) {
            return false;
        }
    }

}
