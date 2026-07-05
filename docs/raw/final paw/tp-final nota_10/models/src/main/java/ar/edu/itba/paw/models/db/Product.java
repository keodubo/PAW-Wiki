package ar.edu.itba.paw.models.db;

import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_id_seq")
    @SequenceGenerator(name = "product_id_seq", sequenceName = "product_id_seq", allocationSize = 1)
    private int id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(nullable = false, precision = 2)
    private double price;

    @Column(nullable = false)
    private boolean active;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", referencedColumnName = "id")
    private Document image;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<Pool> pools;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<Review> reviews;

    @Formula("(SELECT COALESCE(AVG(re.rating), 0) FROM reviews re WHERE re.product_id = id)")
    private double rating;

    @Formula("(SELECT COUNT(re.rating) FROM reviews re WHERE re.product_id = id AND re.rating = 5)")
    private int fiveStars;

    @Formula("(SELECT COUNT(re.rating) FROM reviews re WHERE re.product_id = id AND re.rating = 4)")
    private int fourStars;

    @Formula("(SELECT COUNT(re.rating) FROM reviews re WHERE re.product_id = id AND re.rating = 3)")
    private int threeStars;

    @Formula("(SELECT COUNT(re.rating) FROM reviews re WHERE re.product_id = id AND re.rating = 2)")
    private int twoStars;

    @Formula("(SELECT COUNT(re.rating) FROM reviews re WHERE re.product_id = id AND re.rating = 1)")
    private int oneStar;

    @Formula("(SELECT COUNT(*) FROM pools po WHERE po.product_id = id AND po.status = 'DELIVERING')")
    private int activePools;

    @Formula("(SELECT COUNT(*) FROM requests rq JOIN pools po ON rq.pool_id = po.id WHERE po.product_id = id AND rq.status = 'ACCEPTED')")
    private int acceptedRequests;

    /* default */
    protected Product() {
    }

    public Product(final String name, final String description, final double price, final Document image, final Company company, final Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.company = company;
        this.category = category;
        this.active = true;
    }

    public Product(final int id, final String name, final String description, final double price, final Document image, final Company company, final Category category) {
        this(name, description, price, image, company, category);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Document getImage() {
        return image;
    }

    public void setImage(Document image) {
        this.image = image;
    }

    public Company getCompany() {
        return company;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<Pool> getPools() {
        return pools;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public double getRating() {
        return rating;
    }

    public int getFiveStars() {
        return fiveStars;
    }

    public int getFourStars() {
        return fourStars;
    }

    public int getThreeStars() {
        return threeStars;
    }

    public int getTwoStars() {
        return twoStars;
    }

    public int getOneStar() {
        return oneStar;
    }

    public boolean getCanRetire() {
        return activePools == 0 && acceptedRequests == 0;
    }

}
