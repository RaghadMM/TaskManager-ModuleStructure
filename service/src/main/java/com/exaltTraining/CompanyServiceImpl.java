package com.exaltTraining;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private CompanyRepository companyRepository;
    private EmailService emailService;


    public CompanyServiceImpl(CompanyRepository companyRepository, EmailService emailService,BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.companyRepository = companyRepository;
        this.emailService = emailService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;

    }

    // Create a company account by the company itself, this account will be bending till admin approval
    @Override
    public Company createCompanyAccount(Company company) {
        try{

            company.setPassword(bCryptPasswordEncoder.encode(company.getPassword()));
            return companyRepository.save(company);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Approve company account by the admin
    //Send email to the company
    @Override
    public Boolean approveCompany(int companyId, String decision) {
        Optional<Company> tempCompany= companyRepository.findById(companyId);
        if(tempCompany.isPresent()){
            Company company = tempCompany.get();
            if(decision.equals("approve")){
                company.setApproved(true);
                companyRepository.save(company);

                //Sending an email for the company
                String body = "Dear " + company.getName() + ",\n\n" +
                        "We are pleased to inform you that your collaboration request has been approved. " +
                        "We are excited to have the opportunity to work together and look forward to seeing the great projects we'll accomplish.\n\n" +
                        "Welcome aboard!\n\n" +
                        "Best regards,\nTask Manager Team";

                try {
                    Email email = new Email(company.getEmail(), body, "Request Approved — Welcome Aboard!");
                    String result =emailService.sendSimpleMail(email);
                    System.out.println(result);
                    return true;
                }
                catch(Exception e){
                    return false;
                }

            }
            else if(decision.equals("reject")){
                company.setApproved(false);
                companyRepository.save(company);
                //Sending an email for the company
                String body = "Dear " + company.getName() + ",\n\n" +
                        "Thank you for your interest in collaborating with us. After careful consideration, " +
                        "we regret to inform you that we are unable to proceed with the request at this time.\n\n" +
                        "We truly appreciate your effort and hope to have the opportunity to work together in the future.\n\n" +
                        "Kind regards,\n Task Manager Team";

                Email email = new Email(company.getEmail(), body, "Collaboration Request Update");
                String result =emailService.sendSimpleMail(email);
                System.out.println(result);
                return true;
            }
            else return false;

        }
        return false;
    }

    //Special log in for a company
    //put the "company" role in the generated JWT token
    @Override
    public Company login(String email, String password) {
        List<Company> companies=companyRepository.findAll();
        for(Company company:companies) {
            if(company.getEmail().equals(email) && bCryptPasswordEncoder.matches(password,company.getPassword())) {
                return company;
            }
        }
        System.out.println("company not found");
        return null; // or throw custom exception
    }

    //Helper function to find a company by its email
    @Override
    public Company findCompanyByEmail(String email) {
        List<Company> companies=companyRepository.findAll();
        for(Company company:companies) {
            if(company.getEmail().equals(email)) {
                return company;
            }
        }
        System.out.println("company not found");
        return null;
    }

    //Get all companies requests by the admin
    @Override
    public List<Company> findAllCompanies() {
        try{
            return companyRepository.findAll();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }
    //Get the approved companies by the admin
    @Override
    public List<Company> getApprovedCompanies() {
        try {
            return companyRepository.findAllByApproved(true);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }

    }

    //Delete a company by the admin
    //By cascading delete the company projects and tasks
    @Override
    public Boolean deleteCompany(int companyId) {
        try{
            Optional<Company> tempCompany= companyRepository.findById(companyId);
            if(tempCompany.isPresent()){
                companyRepository.delete(tempCompany.get());
                return true;
            }
            return false;
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    //Search a company by its name or email
    @Override
    public List<Company> searchCompanies(String query) {
        List <Company> companies=companyRepository.searchCompanies(query);
        return companies;
    }

    @Override
    public String companiesCount() {
        return "The total number of collaborators is:  " + companyRepository.count() + " companies \n";
    }

}
