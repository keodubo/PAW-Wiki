package ar.edu.itba.paw.models.db;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reports_id_seq")
    @SequenceGenerator(name = "reports_id_seq", sequenceName = "reports_id_seq", allocationSize = 1)
    private int id;

    @Column(nullable = false, length = 1024)
    private String description;

    @Column(nullable = false, name = "created_at")
    private Date createdAt;

    @Column(nullable = false, name = "user_reported")
    private boolean userReported;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    /* default */
    protected Report() {
    }

    public Report(final String description, final Date createdAt, final boolean userReported, final Company company, final User user) {
        this.description = description;
        this.createdAt = createdAt;
        this.userReported = userReported;
        this.company = company;
        this.user = user;
    }

    public Report(final int id, final String description, final Date createdAt, final boolean userReported, final Company company, final User user) {
        this(description, createdAt, userReported, company, user);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean isUserReported() {
        return userReported;
    }

    public Company getCompany() {
        return company;
    }

    public User getUser() {
        return user;
    }

}
