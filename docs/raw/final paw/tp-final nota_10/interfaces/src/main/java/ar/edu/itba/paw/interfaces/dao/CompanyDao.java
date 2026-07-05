package ar.edu.itba.paw.interfaces.dao;

import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.Document;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Optional;

public interface CompanyDao {

    Company create(final String name, final String address, final String email, final String phone, final boolean validated, final String cbu, final Document image, final User owner);

    void edit(final int id, final String address, final String email, final String phone, final boolean validated, final String cbu, final Document image);

    Optional<Company> findById(final int id);

    Paginator<Company> filter(final String search, final Integer ownerId, final Boolean validated, final int page);

}
