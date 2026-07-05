package ar.edu.itba.paw.models.db;

import org.hibernate.annotations.Formula;

import javax.persistence.*;

@Entity
@Table(name = "requests")
public class Request {

    public enum Status {PENDING, ACCEPTED, REJECTED, DELIVERED}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_id_seq")
    @SequenceGenerator(name = "request_id_seq", sequenceName = "request_id_seq", allocationSize = 1)
    private int id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private int quantity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "down_payment_document_id", referencedColumnName = "id")
    private Document downPayment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_payment_document_id", referencedColumnName = "id")
    private Document finalPayment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", referencedColumnName = "id")
    private Pool pool;

    @Formula("(SELECT (quantity * pr.price) FROM pools p JOIN products pr ON p.product_id = pr.id WHERE pool_id = p.id)")
    private Double total;

    /* default */
    protected Request() {
    }

    public Request(final int quantity, final Status status, final User user, final Pool pool) {
        this.quantity = quantity;
        this.status = status;
        this.user = user;
        this.pool = pool;
    }

    public Request(final int id, final int quantity, final Status status, final User user, final Pool pool) {
        this(quantity, status, user, pool);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Document getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(Document downPayment) {
        this.downPayment = downPayment;
    }

    public Document getFinalPayment() {
        return finalPayment;
    }

    public void setFinalPayment(Document finalPayment) {
        this.finalPayment = finalPayment;
    }

    public Pool getPool() {
        return pool;
    }

    public User getUser() {
        return user;
    }

    public double getTotal() {
        return total != null ? total : 0.0;
    }

}
