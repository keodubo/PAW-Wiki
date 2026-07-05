package ar.edu.itba.paw.models.db;

import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "categories_id_seq")
    @SequenceGenerator(name = "categories_id_seq", sequenceName = "categories_id_seq", allocationSize = 1)
    private int id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(nullable = false, length = 30, name = "icon_name")
    private String iconName;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Product> products;

    @Formula("(SELECT COUNT(*) FROM products p WHERE p.category_id = id AND p.active = true)")
    private int productsCount;

    @Formula("(SELECT COUNT(*) FROM pools po JOIN products p ON po.product_id = p.id WHERE p.category_id = id AND p.active = true AND po.status = 'AVAILABLE')")
    private int poolsCount;

    /* default */
    protected Category() {
    }

    public Category(final String name, final String iconName) {
        this.name = name;
        this.iconName = iconName;
    }

    public Category(final int id, final String name, final String iconName) {
        this(name, iconName);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconName() {
        return iconName;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public int getProductsCount() {
        return productsCount;
    }

    public int getPoolsCount() {
        return poolsCount;
    }

}

