package com.exaltTraining;



import java.util.List;

public interface CompanyService {
    Company createCompanyAccount(Company company);
    Boolean approveCompany(int companyId,String decision);
    Company login(String email, String password);
    Company findCompanyByEmail(String email);
    List<Company> findAllCompanies();
    List<Company> getApprovedCompanies();
    Boolean deleteCompany(int companyId);
    List<Company> searchCompanies(String query);
    String companiesCount();
}
