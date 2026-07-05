package ar.edu.itba.paw.interfaces.dao;

import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.Pool;
import ar.edu.itba.paw.models.db.Request;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Optional;

public interface RequestDao {

    Request create(final int quantity, final User user, final Pool pool);

    void edit(final int id, final int quantity, final Document downPayment, final Document finalPayment);

    void delete(final int id);

    void setStatus(final int id, final Request.Status status);

    void rejectPendingRequests(final int productId);

    Optional<Request> findById(final int id);

    Optional<Request> findFinishedByProductAndUser(final int productId, final int userId);

    Optional<Request> findByCompanyAndUser(final int companyId, final int userId);

    Optional<Request> findByPayment(final int paymentId);

    Paginator<Request> filter(final String search, final Integer companyId, final Integer productId, final String poolStatusStr, final String requestStatusStr, final Integer poolId, final Integer userId, final int page, final String orderBy, final boolean desc);

}
