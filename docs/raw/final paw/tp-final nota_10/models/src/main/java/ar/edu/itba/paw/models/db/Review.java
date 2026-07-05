package ar.edu.itba.paw.models.db;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reviews_id_seq")
    @SequenceGenerator(name = "reviews_id_seq", sequenceName = "reviews_id_seq", allocationSize = 1)
    private int id;

    @Column(nullable = false, precision = 2)
    private double rating;

    @Column(nullable = false, length = 1024)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", referencedColumnName = "id")
    private User reviewer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    /* default */
    protected Review() {
    }

    public Review(final User reviewer, final Product product, final String description, final double rating, final Date createdAt) {
        this.reviewer = reviewer;
        this.product = product;
        this.description = description;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public Review(final int id, final User reviewer, final Product product, final String description, final double rating, final Date createdAt) {
        this(reviewer, product, description, rating, createdAt);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public User getReviewer() {
        return reviewer;
    }

    public Product getProduct() {
        return product;
    }

}
