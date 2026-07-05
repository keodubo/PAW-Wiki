package ar.edu.itba.paw.models.db;

import javax.persistence.*;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "documents_id_seq")
    @SequenceGenerator(sequenceName = "documents_id_seq", name = "documents_id_seq", allocationSize = 1)
    private int id;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(nullable = false, length = 100, name = "filetype")
    private String fileType;

    @Column(nullable = false)
    @Basic(fetch = FetchType.LAZY)
    private byte[] bytes;

    @Column(nullable = false, name = "is_public")
    private boolean isPublic;

    @OneToOne(mappedBy = "image")
    private Company company;

    @OneToOne(mappedBy = "image")
    private Product product;

    @OneToOne(mappedBy = "downPayment")
    private Request downPaymentRequest;

    @OneToOne(mappedBy = "finalPayment")
    private Request finalPaymentRequest;

    /* default */
    protected Document() {
    }

    public Document(final String name, final String fileType, final byte[] bytes, final boolean isPublic) {
        this.name = name;
        this.fileType = fileType;
        this.bytes = bytes;
        this.isPublic = isPublic;
    }

    public Document(final int id, final String name, final String fileType, final byte[] bytes, final boolean isPublic) {
        this(name, fileType, bytes, isPublic);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFileType() {
        return fileType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public Company getCompany() {
        return company;
    }

    public Product getProduct() {
        return product;
    }

    public Request getDownPaymentRequest() {
        return downPaymentRequest;
    }

    public Request getFinalPaymentRequest() {
        return finalPaymentRequest;
    }

}
