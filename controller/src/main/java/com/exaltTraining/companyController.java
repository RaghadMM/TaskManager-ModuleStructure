package com.exaltTraining;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("taskManager")
public class companyController {
    private CompanyService companyService;
    @Autowired
    private JwtService jwtService;
    public companyController(CompanyService companyService) {
        this.companyService = companyService;
    }


    //Create company API
    @PostMapping("/company")
    public String createCompanyAccount(@RequestBody Company company) {
        System.out.println(company.getPassword() + "passed");
        Company newCompany = companyService.createCompanyAccount(company);
        if(newCompany != null) {
            return "Comany added successfully";
        }
        else {
            return "Company creation failed";
        }
    }

    //Approve company account API
    @PutMapping("/companyApproval/{companyId}/{decision}")
    public String approveCompanyAccount(@PathVariable int companyId,@PathVariable String decision,@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return "Unauthorized: Only admin can approve companies.";
        }
        Boolean isApproved= companyService.approveCompany(companyId,decision);
        System.out.println(isApproved);
        if(isApproved) {
            return "Company approved successfully";
        }
        else {
            return "Error approving company";
        }

    }

    //Companies log in API
    @PostMapping("/companyLogin")
    public String login(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        Company authenticatedCompany =companyService.login(email,password);
        if(authenticatedCompany != null){
            String token = jwtService.generateToken(email,"company");
            return "Company logged in successfully \n Here is the token: " + token;
        }
        else{
            return "Company is not authenticated";
        }

    }
    //Get all companies requests API
    @GetMapping("/companies")
    public List<companyPrinted> getCompanies(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return null;
        }

        List<Company> companies=companyService.findAllCompanies();
        if(companies==null){
            return null;
        }

        return printCompanies(companies);
    }

    //Get approved companies API
    @GetMapping("/approvedCompanies")
    public List<companyPrinted> getApprovedCompanies(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return null;
        }
        List<Company> companies=companyService.getApprovedCompanies();
        if(companies==null){
            return null;
        }
        return printCompanies(companies);
    }
    //Delete a company with projects
    @DeleteMapping("/company/{companyId}")
    public String deleteCompanyAccount(@PathVariable int companyId,@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return "Unauthorized: Only admin can delete companies.";
        }
        Boolean isDeleted = companyService.deleteCompany(companyId);
        if(isDeleted) {
            return "Company deleted successfully";
        }
        else {
            return "Company not deleted";
        }

    }

    //Search companies API
    @GetMapping("/companies/search")
    public ResponseEntity<List<companyPrinted>> searchCompanies(@RequestParam("query") String query, @RequestHeader ("Authorization") String authHeader ) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // or UNAUTHORIZED

        }
        List<Company> companies = companyService.searchCompanies(query);
        return ResponseEntity.ok(printCompanies(companies));
    }

    // A helper function to form the list of companies returned
    private List<companyPrinted> printCompanies(List <Company> companies) {
        List<companyPrinted> companyP = companies.stream()
                .map(company -> new companyPrinted(
                        company.getId(),
                        company.getName(),
                        company.getEmail(),
                        company.getApproved()
                ))
                .collect(Collectors.toList());
        return companyP;

    }


}
