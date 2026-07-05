package ar.edu.itba.paw.persistence.dao;

import ar.edu.itba.paw.interfaces.dao.CompanyDao;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class CompanyDaoJpa implements CompanyDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Company create(final String name, final String address, final String email, final String phone, final boolean validated, final String cbu, final Document image, final User owner) {
        final Company company = new Company(name, address, email, phone, validated, cbu, image, owner);
        entityManager.persist(company);
        return company;
    }

    @Override
    public void edit(final int id, final String address, final String email, final String phone, final boolean validated, final String cbu, final Document image) {
        Company company = entityManager.find(Company.class, id);
        company.setAddress(address);
        company.setEmail(email);
        company.setPhone(phone);
        company.setValidated(validated);
        company.setCbu(cbu);
        company.setImage(image);
    }

    @Override
    public Optional<Company> findById(int id) {
        return Optional.ofNullable(entityManager.find(Company.class, id));
    }

    @Override
    public Paginator<Company> filter(
            final String search,
            final Integer ownerId,
            final Boolean validated,
            final int page
    ) {
        final String idsQuerySql = "SELECT id FROM companies ";
        final String countQuerySql = "SELECT count(*) FROM companies ";

        final StringBuilder where = new StringBuilder();

        if (search != null) {
            where.append(where.isEmpty() ? "WHERE" : "AND")
                    .append(" (lower(name) LIKE lower(:search) OR lower(email) LIKE lower(:search)) ");
        }

        if (ownerId != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" owner_id = :owner_id ");

        if (validated != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" validated = :validated ");

        final String orderBy = "ORDER BY name, email, id DESC";
        final Query paginatorQuery = entityManager.createNativeQuery(idsQuerySql + where + orderBy);
        final Query countQuery = entityManager.createNativeQuery(countQuerySql + where);

        if (search != null) {
            String searchParam = "%" + sanitize(search) + "%";
            countQuery.setParameter("search", searchParam);
            paginatorQuery.setParameter("search", searchParam);
        }

        if (ownerId != null) {
            countQuery.setParameter("owner_id", ownerId);
            paginatorQuery.setParameter("owner_id", ownerId);
        }

        if (validated != null) {
            countQuery.setParameter("validated", validated);
            paginatorQuery.setParameter("validated", validated);
        }

        final long count = ((Number) countQuery.getSingleResult()).longValue();
        final int pageNorm = Math.max(0, page);

        paginatorQuery.setFirstResult(pageNorm * Paginator.DEFAULT_PAGE_SIZE);
        paginatorQuery.setMaxResults(Paginator.DEFAULT_PAGE_SIZE);

        final List<Company> companyList = getCompanyList(paginatorQuery, orderBy);
        return new Paginator<>(companyList, pageNorm, Paginator.DEFAULT_PAGE_SIZE, (int) count);
    }

    private List<Company> getCompanyList(Query paginatorQuery, String orderBy) {
        @SuppressWarnings("unchecked")
        List<Integer> paginatedIdsList = paginatorQuery.getResultList();

        if (paginatedIdsList.isEmpty())
            return Collections.emptyList();

        String orderedQuery = "FROM Company WHERE id IN :ids_list " + orderBy;
        TypedQuery<Company> query = entityManager.createQuery(orderedQuery, Company.class);
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
