package com.exaltTraining;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {
    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company company;
    private Project project;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1);
        company.setName("Exalt");
        company.setEmail("exalt@gmail.com");
        company.setPassword(bCryptPasswordEncoder.encode("exalt123"));


    }
    //Create company account
    @Test
    void createCompany_success() {

        when(companyRepository.save(company)).thenReturn(company);


        Company result = companyService.createCompanyAccount(company);

        assertNotNull(result);
        assertEquals("exalt@gmail.com", result.getEmail());

        verify(companyRepository, times(1)).save(company);
    }
    @Test
    void createCompany_invalidEmail() {
        company.setEmail("exaltgmail.com");
        Company result = companyService.createCompanyAccount(company);
        assertNull(result);
    }

    //Approve company
    @Test
    void approveCompany_success() {

        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(emailService.sendSimpleMail(any(Email.class))).thenReturn("Email sent successfully");

        Boolean result = companyService.approveCompany(1,"approve");
        assertTrue(result);
        verify(companyRepository, times(1)).save(company);
        verify(emailService, times(1)).sendSimpleMail(any(Email.class));

    }
    @Test
    void rejectCompany_success() {

        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(emailService.sendSimpleMail(any(Email.class))).thenReturn("Email sent successfully");

        Boolean result = companyService.approveCompany(1,"reject");
        assertTrue(result);
        verify(companyRepository, times(1)).save(company);
        verify(emailService, times(1)).sendSimpleMail(any(Email.class));

    }
    @Test
    void approveCompany_companyNotFound_shouldReturnFalse() {

        when(companyRepository.findById(1)).thenReturn(Optional.empty());

        Boolean result = companyService.approveCompany(1,"approve");
        assertFalse(result);

        verify(companyRepository, never()).save(company);
        verify(emailService, never()).sendSimpleMail(any(Email.class));

    }
    @Test
    void rejectCompany_companyNotFound_shouldReturnFalse() {

        when(companyRepository.findById(1)).thenReturn(Optional.empty());

        Boolean result = companyService.approveCompany(1,"reject");
        assertFalse(result);

        verify(companyRepository, never()).save(company);
        verify(emailService, never()).sendSimpleMail(any(Email.class));

    }
    @Test
    void approveCompany_invalidDecision_shouldReturnFalse() {

        when(companyRepository.findById(1)).thenReturn(Optional.of(company));

        Boolean result = companyService.approveCompany(1,"pending");
        assertFalse(result);

        verify(companyRepository, never()).save(company);
        verify(emailService, never()).sendSimpleMail(any(Email.class));

    }
    @Test
    void approveCompany_errorSendingEmail_shouldReturnFalse() {

        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(emailService.sendSimpleMail(any(Email.class))).thenThrow(new RuntimeException());

        Boolean result = companyService.approveCompany(1,"approve");
        assertFalse(result);
        verify(companyRepository, times(1)).save(company);
        verify(emailService, times(1)).sendSimpleMail(any(Email.class));

    }
    //company login
    @Test
    void companyLogin_successful() {
        String hashedPassword = "$2a$12$r.ErjHkDFTmjo90D8ijQdema66/GKPUY6ENfUNCI78YeG/ppSgbBu";
        company.setPassword(hashedPassword);

        List<Company> companies = List.of(company);

        when(companyRepository.findAll()).thenReturn(companies);
        when(bCryptPasswordEncoder.matches("exalt123", hashedPassword)).thenReturn(true);

        Company result = companyService.login("exalt@gmail.com", "exalt123");

        assertNotNull(result);
        assertEquals("exalt@gmail.com", result.getEmail());
    }
    @Test
    void companyLogin_incorrectPassword() {
        String hashedPassword = "$2a$12$r.ErjHkDFTmjo90D8ijQdema66/GKPUY6ENfUNCI78YeG/ppSgbBu";
        company.setPassword(hashedPassword);

        List<Company> companies = List.of(company);

        when(companyRepository.findAll()).thenReturn(companies);
        when(bCryptPasswordEncoder.matches("exalt12", hashedPassword)).thenReturn(false);

        Company result = companyService.login("exalt@gmail.com", "exalt12");

        assertNull(result);

    }
    @Test
    void companyLogin_userNotFound() {
        String hashedPassword = "$2a$12$r.ErjHkDFTmjo90D8ijQdema66/GKPUY6ENfUNCI78YeG/ppSgbBu";
        company.setPassword(hashedPassword);

        List<Company> companies = List.of(company);

        when(companyRepository.findAll()).thenReturn(companies);


        Company result = companyService.login("exalt1@gmail.com", "exalt123");

        assertNull(result);

    }
    //Get all companies
    @Test
    void testGetAllCompanies_returnAList() {

        Company company2 = new Company();
        company2.setId(2);
        company2.setEmail("harri@gmail.com");
        company2.setPassword("harri123");
        company2.setName("Harri");


        List<Company> companies = Arrays.asList(company, company2);

        when(companyRepository.findAll()).thenReturn(companies);

        List<Company> result = companyService.findAllCompanies();

        assertEquals(2, result.size());
        verify(companyRepository, times(1)).findAll();

    }
    @Test
    void testGetAllCompanies_noCompanies() {
        when(companyRepository.findAll()).thenReturn(null);

        List<Company> result = companyService.findAllCompanies();

        assertNull(result);
        verify(companyRepository, times(1)).findAll();

    }
    @Test
    void testGetAllCompanies_throwException() {

        Company company2 = new Company();
        company2.setId(2);
        company2.setEmail("harri@gmail.com");
        company2.setPassword("harri123");
        company2.setName("Harri");


        List<Company> companies = Arrays.asList(company, company2);

        when(companyRepository.findAll()).thenThrow(new RuntimeException());

        List<Company> result = companyService.findAllCompanies();

        assertNull(result);
        verify(companyRepository, times(1)).findAll();

    }
    //Find company by email
    @Test
    void findCompanyByEmail_successful() {


        when(companyRepository.findAll()).thenReturn(List.of(company));

        Company result = companyService.findCompanyByEmail("exalt@gmail.com");
        System.out.println(result);
        assertNotNull(result);
        assertEquals("exalt@gmail.com", result.getEmail());


        verify(companyRepository, times(1)).findAll();


    }
    @Test
    void findCompanyByEmail_notFound() {


        Company result = companyService.findCompanyByEmail("exalt1@gmail.com");
        assertNull(result);

    }
    //Get approved companies
    @Test
    void testGetApprovedCompanies_returnAList() {

        company.setApproved(true);
        Company company2 = new Company();
        company2.setId(2);
        company2.setEmail("harri@gmail.com");
        company2.setPassword("harri123");
        company2.setName("Harri");
        company2.setApproved(false);


        List<Company> approved = Collections.singletonList(company);

        when(companyRepository.findAllByApproved(true)).thenReturn(approved);

        List<Company> result = companyService.getApprovedCompanies();

        assertEquals(1, result.size());
        assertEquals("Exalt", result.get(0).getName());
        verify(companyRepository, times(1)).findAllByApproved(true);

    }
    @Test
    void testGetApprovedCompanies_returnEmptyList() {

        company.setApproved(false);
        Company company2 = new Company();
        company2.setId(2);
        company2.setEmail("harri@gmail.com");
        company2.setPassword("harri123");
        company2.setName("Harri");
        company2.setApproved(false);

        List<Company> result = companyService.getApprovedCompanies();

        assertEquals(0, result.size());

    }
    //Delete a company
    @Test
    void testDeleteCompany_success() {

        when(companyRepository.findById(1)).thenReturn(Optional.of(company));

        Boolean result = companyService.deleteCompany(1);
        assertTrue(result);

    }
    @Test
    void testDeleteCompany_companyNotFound() {

        when(companyRepository.findById(1)).thenReturn(Optional.empty());

        Boolean result = companyService.deleteCompany(1);
        assertFalse(result);

    }
    @Test
    void testDeleteCompany_throwException() {

        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        doThrow(new RuntimeException("Delete failed"))
                .when(companyRepository)
                .delete(any(Company.class));

        Boolean result = companyService.deleteCompany(1);
        assertFalse(result);
        verify(companyRepository, times(1)).delete(any(Company.class));

    }







}
