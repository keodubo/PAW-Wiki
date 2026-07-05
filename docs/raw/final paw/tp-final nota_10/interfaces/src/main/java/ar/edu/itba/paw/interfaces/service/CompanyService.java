package ar.edu.itba.paw.interfaces.service;

import ar.edu.itba.paw.models.db.Company;
import ar.edu.itba.paw.models.db.User;
import ar.edu.itba.paw.models.paginator.Paginator;

import java.util.Optional;

public interface CompanyService {

    Company create(final String name, final String address, final String email, final String phone, final String cbu, final String imageUri, final User owner);

    void edit(final int id, final String address, final String email, final String phone, final String cbu, final String imageUri, final Boolean validated);

    Optional<Company> findById(final int id);

    Paginator<Company> filter(final String search, final Integer ownerId, final Boolean validated, final int page);

}
