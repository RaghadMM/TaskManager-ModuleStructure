package com.exaltTraining;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Integer> {
    List<Company> findAllByApproved(Boolean approved);
    @Query("SELECT c FROM Company c WHERE " +
            "c.name LIKE CONCAT('%',:query, '%')" +
            "Or c.email LIKE CONCAT('%', :query, '%')")
    List<Company> searchCompanies(String query);

    Company findCompanyByEmail(String mail);
}
