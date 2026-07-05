package ar.edu.itba.paw.persistence.dao;

import ar.edu.itba.paw.interfaces.dao.UserDao;
import ar.edu.itba.paw.interfaces.exception.UserNotFoundException;
import ar.edu.itba.paw.models.db.Location;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoJpa implements UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User create(final String email, final String password, final String firstName, final String lastName, final Location location, final boolean isCompany, final boolean validated, final String validationToken, final String passwordToken, final boolean admin, final int blockLevel, final Date blockedUntil, final String preferredLanguage) {
        final User user = new User(email, password, firstName, lastName, location, isCompany, validated, validationToken, passwordToken, admin, blockLevel, blockedUntil, preferredLanguage);
        entityManager.persist(user);
        return user;
    }

    @Override
    public void edit(int id, String firstName, String lastName, Location location, int blockLevel, Date blockedUntil) {
        User user = entityManager.find(User.class, id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setLocation(location);
        user.setBlockLevel(blockLevel);
        user.setBlockedUntil(blockedUntil);
    }

    @Override
    public void updatePasswordToken(int id, String token) {
        User user = entityManager.find(User.class, id);
        user.setPasswordToken(token);
    }

    @Override
    public void updatePassword(String email, String password) {
        User user = findByEmail(email).orElseThrow(UserNotFoundException::new);
        user.setPassword(password);
        user.setPasswordToken(null);
    }

    @Override
    public void updateValidationToken(int id, String token) {
        User user = entityManager.find(User.class, id);
        user.setValidationToken(token);
    }

    @Override
    public void updatePreferredLanguage(final int id, final String language) {
        final User user = entityManager.find(User.class, id);
        user.setPreferredLanguage(language);
    }

    @Override
    public void validateAccount(final int id) {
        User user = entityManager.find(User.class, id);
        user.setValidated(true);
        user.setValidationToken(null);
    }

    @Override
    public Optional<User> findById(int id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        TypedQuery<User> query = entityManager.createQuery("from User where email = :email", User.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<User> findAdmin() {
        TypedQuery<User> query = entityManager.createQuery("from User where admin = true", User.class);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Paginator<User> filter(final String search, final Boolean validated, final int page) {
        final String idsQuerySql = "SELECT id FROM users ";
        final String countQuerySql = "SELECT count(*) FROM users ";

        final StringBuilder where = new StringBuilder("WHERE admin = false ");

        if (search != null && !search.trim().isEmpty())
            where.append("AND (lower(CONCAT(first_name,  ' ', last_name)) LIKE lower(:search) OR lower(email) LIKE lower(:search)) ");

        if (validated != null)
            where.append("AND validated = :validated ");

        final String orderBy = "ORDER BY first_name, last_name, email, id DESC";
        final Query paginatorQuery = entityManager.createNativeQuery(idsQuerySql + where + orderBy);
        final Query countQuery = entityManager.createNativeQuery(countQuerySql + where);

        if (search != null && !search.trim().isEmpty()) {
            String searchParam = "%" + sanitize(search) + "%";
            countQuery.setParameter("search", searchParam);
            paginatorQuery.setParameter("search", searchParam);
        }

        if (validated != null) {
            countQuery.setParameter("validated", validated);
            paginatorQuery.setParameter("validated", validated);
        }

        final long count = ((Number) countQuery.getSingleResult()).longValue();
        final int pageNorm = Math.max(0, page);

        paginatorQuery.setFirstResult(pageNorm * Paginator.DEFAULT_PAGE_SIZE);
        paginatorQuery.setMaxResults(Paginator.DEFAULT_PAGE_SIZE);

        final List<User> userList = getUsersList(paginatorQuery, orderBy);
        return new Paginator<>(userList, pageNorm, Paginator.DEFAULT_PAGE_SIZE, (int) count);
    }

    private List<User> getUsersList(Query paginatorQuery, String orderBy) {
        @SuppressWarnings("unchecked")
        List<Integer> paginatedIdsList = paginatorQuery.getResultList();

        if (paginatedIdsList.isEmpty())
            return Collections.emptyList();

        String orderedQuery = "from User where id in :ids_list " + orderBy;
        TypedQuery<User> query = entityManager.createQuery(orderedQuery, User.class);
        query.setParameter("ids_list", paginatedIdsList);

        return query.getResultList();
    }

    private String sanitize(String str) {
        return str.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("'", "''")
                .replace("\"", "\"\"");
    }

}
