package ar.edu.itba.paw.models.db;

import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "pools")
public class Pool {

    public enum Status {AVAILABLE, DELIVERING, PAUSED, CANCELLED, FINISHED}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pool_id_seq")
    @SequenceGenerator(name = "pool_id_seq", sequenceName = "pool_id_seq", allocationSize = 1)
    private int id;

    @Column(nullable = false, name = "min_quantity")
    private int minQuantity;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false, name = "created_at")
    private Date createdAt;

    @Column(nullable = false, name = "down_payment")
    private int downPayment;

    @Column(nullable = false, precision = 2)
    private double price;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @OneToMany(mappedBy = "pool", fetch = FetchType.LAZY)
    private Set<Request> requests;

    @Formula("(SELECT COALESCE(SUM(r.quantity), 0) FROM requests r WHERE r.pool_id = id AND (r.status = 'ACCEPTED' OR r.status = 'DELIVERED'))")
    private int acceptedOrDeliveredRequestsSum;

    @Formula("(SELECT COALESCE(SUM(r.quantity), 0) FROM requests r WHERE r.pool_id = id AND (r.status = 'PENDING' OR r.status = 'ACCEPTED' OR r.status = 'DELIVERED'))")
    private int productsRequestedCount;

    @Formula("(SELECT COALESCE(COUNT(r.id), 0) FROM requests r WHERE r.pool_id = id AND r.status = 'PENDING')")
    private int pendingRequestsCount;

    @Formula("(SELECT COALESCE(SUM(r.quantity), 0) FROM requests r WHERE r.pool_id = id AND r.status = 'PENDING')")
    private int pendingRequestsSum;

    @Formula("(SELECT COALESCE(COUNT(r.id), 0) FROM requests r WHERE r.pool_id = id AND r.status = 'ACCEPTED')")
    private int acceptedRequestsCount;

    @Formula("(SELECT COALESCE(SUM(r.quantity), 0) FROM requests r WHERE r.pool_id = id AND r.status = 'ACCEPTED')")
    private int acceptedRequestsSum;

    @Formula("(SELECT COALESCE(COUNT(r.id), 0) FROM requests r WHERE r.pool_id = id AND r.status = 'REJECTED')")
    private int rejectedRequestsCount;

    @Formula("(SELECT COALESCE(COUNT(r.id), 0) FROM requests r WHERE r.pool_id = id AND r.status = 'DELIVERED')")
    private int deliveredRequestsCount;

    @Formula("(SELECT COALESCE(SUM(r.quantity), 0) FROM requests r WHERE r.pool_id = id AND r.status = 'DELIVERED')")
    private int deliveredRequestsSum;

    /* default */
    protected Pool() {
    }

    public Pool(final int minQuantity, final Status status, final int downPayment, final double price, final Product product, final Location location, final Date createdAt) {
        this.minQuantity = minQuantity;
        this.status = status;
        this.downPayment = downPayment;
        this.price = price;
        this.product = product;
        this.location = location;
        this.createdAt = createdAt;
    }

    public Pool(final int id, final int minQuantity, final Status status, final int downPayment, final double price, final Product product, final Location location, final Date createdAt) {
        this(minQuantity, status, downPayment, price, product, location, createdAt);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public int getDownPayment() {
        return downPayment;
    }

    public double getPrice() {
        return price;
    }

    public Product getProduct() {
        return product;
    }

    public Location getLocation() {
        return location;
    }

    public Set<Request> getRequests() {
        return requests;
    }

    public int getAcceptedOrDeliveredRequestsSum() {
        return acceptedOrDeliveredRequestsSum;
    }

    public int getProductsRequestedCount() {
        return productsRequestedCount;
    }

    public int getPendingRequestsCount() {
        return pendingRequestsCount;
    }

    public int getPendingRequestsSum() {
        return pendingRequestsSum;
    }

    public int getAcceptedRequestsCount() {
        return acceptedRequestsCount;
    }

    public int getAcceptedRequestsSum() {
        return acceptedRequestsSum;
    }

    public int getRejectedRequestsCount() {
        return rejectedRequestsCount;
    }

    public int getDeliveredRequestsCount() {
        return deliveredRequestsCount;
    }

    public int getDeliveredRequestsSum() {
        return deliveredRequestsSum;
    }

}
