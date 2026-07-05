package ar.edu.itba.paw.models.db;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_id_seq")
    @SequenceGenerator(name = "company_id_seq", sequenceName = "company_id_seq", allocationSize = 1)
    private int id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(nullable = false, length = 100)
    private String address;

    @Column(nullable = false, length = 30)
    private String email;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false)
    private boolean validated;

    @Column(nullable = false, length = 22)
    private String cbu;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", referencedColumnName = "id")
    private Document image;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private Set<Product> products;

    /* default */
    protected Company() {
    }

    public Company(final String name, final String address, final String email, final String phone, final boolean validated, final String cbu, final Document image, final User owner) {
        this.name = name;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.validated = validated;
        this.cbu = cbu;
        this.image = image;
        this.owner = owner;
    }

    public Company(final int id, final String name, final String address, final String email, final String phone, final boolean validated, final String cbu, final Document image, final User owner) {
        this(name, address, email, phone, validated, cbu, image, owner);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public String getCbu() {
        return cbu;
    }

    public void setCbu(String cbu) {
        this.cbu = cbu;
    }

    public Document getImage() {
        return image;
    }

    public void setImage(Document image) {
        this.image = image;
    }

    public User getOwner() {
        return owner;
    }

    public Set<Product> getProducts() {
        return products;
    }

}