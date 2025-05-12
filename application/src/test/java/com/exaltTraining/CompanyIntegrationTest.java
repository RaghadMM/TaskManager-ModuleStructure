package com.exaltTraining;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class CompanyIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private JwtService jwtService;

    private Company testCompany;
    private String adminToken;
    private String userToken;

    @BeforeEach
    public void setUp() {
        companyRepository.deleteAll();
        testCompany = new Company();
        testCompany.setName("Exalt");
        testCompany.setEmail("exalt@gmail.com");
        testCompany.setPassword(bCryptPasswordEncoder.encode("exalt123"));
        companyRepository.save(testCompany);

        adminToken = jwtService.generateToken("admin@gmail.com", "admin");
        userToken = jwtService.generateToken("shifa@gmail.com", "employee");
    }

    //Approve company
    @Test
    public void approveCompany_success() throws Exception {

        mockMvc.perform(
                        put("/taskManager/companyApproval/{companyId}/{decision}", testCompany.getId(), "approve")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Company approved successfully"));
    }
    @Test
    public void approveCompany_notAdmin() throws Exception {

        mockMvc.perform(
                        put("/taskManager/companyApproval/{companyId}/{decision}", testCompany.getId(), "approve")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Unauthorized: Only admin can approve companies."));
    }
    @Test
    public void approveCompany_failed() throws Exception {

        mockMvc.perform(
                        put("/taskManager/companyApproval/{companyId}/{decision}", 3, "approve")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Error approving company"));
    }
    //Login
    @Test
    public void loginCompany_success() throws Exception {

        String jsonBody = """
                {
                    "email": "exalt@gmail.com",
                    "password": "exalt123"
                }
                """;

        mockMvc.perform(post("/taskManager/companyLogin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Company logged in successfully \n Here is the token: ")));

    }
    @Test
    public void loginCompany_failed() throws Exception {

        String jsonBody = """
                {
                    "email": "exalt@gmail.com",
                    "password": "exalt13"
                }
                """;

        mockMvc.perform(post("/taskManager/companyLogin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Company is not authenticated")));

    }
    //Delete a company
    @Test
    public void deleteCompany_success() throws Exception {

        mockMvc.perform(
                        delete("/taskManager/company/{companyId}", testCompany.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Company deleted successfully"));
    }
    @Test
    public void deleteCompany_notAdmin() throws Exception {

        mockMvc.perform(
                        delete("/taskManager/company/{companyId}", testCompany.getId())
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Unauthorized: Only admin can delete companies."));
    }
    @Test
    public void deleteCompany_failed() throws Exception {

        mockMvc.perform(
                        delete("/taskManager/company/{companyId}", 6)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Company not deleted"));
    }
    //search a company
    @Test
    public void searchACompany_success() throws Exception {
        mockMvc.perform(get("/taskManager/companies/search?query=gma")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyId", is(testCompany.getId())))
                .andExpect(jsonPath("$[0].companyName", is(testCompany.getName())))
                .andExpect(jsonPath("$[0].companyEmail", is(testCompany.getEmail())));

    }
    @Test
    public void searchACompany_notAdmin() throws Exception {
        mockMvc.perform(get("/taskManager/companies/search?query=gma")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());


    }
    @Test
    public void searchACompany_noMatches() throws Exception {
        mockMvc.perform(get("/taskManager/companies/search?query=shh")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

    }


}
