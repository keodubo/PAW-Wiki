package ar.edu.itba.paw.persistence.dao;

import ar.edu.itba.paw.interfaces.dao.ProductDao;
import ar.edu.itba.paw.models.db.Category;
import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.Product;
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
public class ProductDaoJpa implements ProductDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Product create(final String name, final String description, final double price, final Document image, final Company company, final Category category) {
        final Product product = new Product(name, description, price, image, company, category);
        entityManager.persist(product);
        return product;
    }

    @Override
    public void edit(final int id, final String name, final String description, final double price, final Document image, final Category category) {
        Product product = entityManager.find(Product.class, id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImage(image);
        product.setCategory(category);
    }

    @Override
    public void retire(int id) {
        Product product = entityManager.find(Product.class, id);
        product.setActive(false);
    }

    @Override
    public Optional<Product> findById(int id) {
        return Optional.ofNullable(entityManager.find(Product.class, id));
    }

    @Override
    public Paginator<Product> filter(final String search, final Integer categoryId, final Integer companyId, final Double priceMin, final Double priceMax, final Boolean active, final int page, final String order, final boolean desc) {
        String idsQuerySql = "SELECT id FROM products ";
        String countQuerySql = "SELECT count(*) FROM products ";

        if (order.equals("rating")) {
            idsQuerySql = "SELECT id FROM (SELECT p.*, (SELECT COALESCE(AVG(re.rating), 0) FROM reviews re "
                    + "WHERE re.product_id = id) rating FROM products p) products ";
        }

        final StringBuilder where = new StringBuilder();

        if (search != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" lower(name) LIKE lower(:search) ");

        if (categoryId != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" category_id = :category_id ");

        if (companyId != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" company_id = :company_id ");

        if (priceMin != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" price >= :price_min ");

        if (priceMax != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" price <= :price_max ");

        if (active != null)
            where.append(where.isEmpty() ? "WHERE" : "AND").append(" active = :active ");

        String orderBy, field;
        if (order != null && (field = getOrderByField(order)) != null) {
            orderBy = "ORDER BY " + field + (desc ? " DESC " : " ASC ") + ", id " + (desc ? "DESC" : "ASC");
        } else {
            orderBy = "ORDER BY id " + (desc ? "DESC" : "ASC");
        }

        final Query paginatorQuery = entityManager.createNativeQuery(idsQuerySql + where + orderBy);
        final Query countQuery = entityManager.createNativeQuery(countQuerySql + where);

        if (search != null) {
            String searchParam = "%" + sanitize(search) + "%";
            countQuery.setParameter("search", searchParam);
            paginatorQuery.setParameter("search", searchParam);
        }

        if (categoryId != null) {
            countQuery.setParameter("category_id", categoryId);
            paginatorQuery.setParameter("category_id", categoryId);
        }

        if (companyId != null) {
            countQuery.setParameter("company_id", companyId);
            paginatorQuery.setParameter("company_id", companyId);
        }

        if (priceMin != null) {
            countQuery.setParameter("price_min", priceMin);
            paginatorQuery.setParameter("price_min", priceMin);
        }

        if (priceMax != null) {
            countQuery.setParameter("price_max", priceMax);
            paginatorQuery.setParameter("price_max", priceMax);
        }

        if (active != null) {
            countQuery.setParameter("active", active);
            paginatorQuery.setParameter("active", active);
        }

        final long count = ((Number) countQuery.getSingleResult()).longValue();
        final int pageNorm = Math.max(0, page);

        paginatorQuery.setFirstResult(pageNorm * Paginator.DEFAULT_PAGE_SIZE);
        paginatorQuery.setMaxResults(Paginator.DEFAULT_PAGE_SIZE);

        final List<Product> productList = getProductList(paginatorQuery, orderBy);
        return new Paginator<>(productList, pageNorm, Paginator.DEFAULT_PAGE_SIZE, (int) count);
    }

    private List<Product> getProductList(Query paginatorQuery, String orderBy) {
        @SuppressWarnings("unchecked")
        List<Integer> paginatedIdsList = paginatorQuery.getResultList();

        if (paginatedIdsList.isEmpty())
            return Collections.emptyList();

        String orderedQuery = "from Product where id in :ids_list " + orderBy;
        TypedQuery<Product> query = entityManager.createQuery(orderedQuery, Product.class);
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

    private String getOrderByField(final String order) {
        return switch (order) {
            case "name" -> "name";
            case "price" -> "price";
            case "rating" -> "rating";
            default -> null;
        };
    }

}
