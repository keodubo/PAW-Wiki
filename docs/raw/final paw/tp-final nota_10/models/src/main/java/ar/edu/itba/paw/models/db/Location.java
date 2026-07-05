package ar.edu.itba.paw.models.db;

import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locations_id_seq")
    @SequenceGenerator(name = "locations_id_seq", sequenceName = "locations_id_seq", allocationSize = 1)
    private int id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    private Set<User> users;

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    private Set<Pool> pools;

    @Formula("(SELECT COUNT(*) FROM pools p WHERE p.location_id = id AND p.status = 'AVAILABLE')")
    private int poolsCount;

    /* default */
    protected Location() {
    }

    public Location(final String name) {
        this.name = name;
    }

    public Location(final int id, final String name) {
        this(name);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Set<Pool> getPools() {
        return pools;
    }

    public int getPoolsCount() {
        return poolsCount;
    }

}

