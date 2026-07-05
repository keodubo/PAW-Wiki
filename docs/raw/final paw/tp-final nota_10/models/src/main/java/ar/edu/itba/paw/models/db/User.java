package ar.edu.itba.paw.models.db;

import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_seq")
    @SequenceGenerator(name = "users_id_seq", sequenceName = "users_id_seq", allocationSize = 1)
    private int id;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 30, name = "first_name")
    private String firstName;

    @Column(nullable = false, length = 30, name = "last_name")
    private String lastName;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @Column(unique = true, length = 70, name = "validation_token")
    private String validationToken;

    @Column(unique = true, length = 70, name = "password_token")
    private String passwordToken;

    @Column(nullable = false, name = "is_company")
    private boolean isCompany;

    @Column(nullable = false)
    private boolean validated;

    @Column(nullable = false)
    private boolean admin;

    @Column(name = "block_level")
    private int blockLevel;

    @Column(name = "blocked_until")
    private Date blockedUntil;

    @Column(nullable = false, length = 5, name = "preferred_language")
    private String preferredLanguage;

    @OneToOne(mappedBy = "owner")
    private Company company;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Request> requests;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Report> reports;

    @OneToMany(mappedBy = "reviewer", fetch = FetchType.LAZY)
    private Set<Review> reviews;

    @Formula("(EXISTS (SELECT 1 FROM reports re WHERE re.user_id = id))")
    private boolean hasReports;

    /* default */
    protected User() {
    }

    public User(final String email, final String password, final String firstName, final String lastName, final Location location, final boolean isCompany, final boolean validated, final String validationToken, final String passwordToken, final boolean admin, final int blockLevel, final Date blockedUntil, final String preferredLanguage) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.location = location;
        this.isCompany = isCompany;
        this.validated = validated;
        this.validationToken = validationToken;
        this.passwordToken = passwordToken;
        this.admin = admin;
        this.blockLevel = blockLevel;
        this.blockedUntil = blockedUntil;
        this.preferredLanguage = preferredLanguage;
    }

    public User(final int id, final String email, final String password, final String firstName, final String lastName, final Location location, final boolean isCompany, final boolean validated, final String validationToken, final String passwordToken, final boolean admin, final int blockLevel, final Date blockedUntil, final String preferredLanguage) {
        this(email, password, firstName, lastName, location, isCompany, validated, validationToken, passwordToken, admin, blockLevel, blockedUntil, preferredLanguage);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getValidationToken() {
        return validationToken;
    }

    public void setValidationToken(String token) {
        this.validationToken = token;
    }

    public String getPasswordToken() {
        return passwordToken;
    }

    public void setPasswordToken(String passwordToken) {
        this.passwordToken = passwordToken;
    }

    public boolean getIsCompany() {
        return isCompany;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public boolean isAdmin() {
        return admin;
    }

    public int getBlockLevel() {
        return blockLevel;
    }

    public void setBlockLevel(int blockLevel) {
        this.blockLevel = blockLevel;
    }

    public Date getBlockedUntil() {
        return blockedUntil;
    }

    public void setBlockedUntil(Date blockedUntil) {
        this.blockedUntil = blockedUntil;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(final String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public boolean hasReports() {
        return hasReports;
    }

    public boolean getIsBlocked() {
        return blockLevel == 4 || (blockLevel >= 1 && blockedUntil != null && blockedUntil.after(new Date()));
    }

    public Company getCompany() {
        return company;
    }

    public Set<Request> getRequests() {
        return requests;
    }

    public Set<Report> getReports() {
        return reports;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

}
