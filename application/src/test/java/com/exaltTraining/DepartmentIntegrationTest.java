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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class DepartmentIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JwtService jwtService;

    private Department testDepartment;
    private User user;
    private String adminToken;
    private String userToken;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        departmentRepository.deleteAll();
        testDepartment = new Department();
        testDepartment.setName("Microsoft");
        testDepartment.setType(Department.Type.SOFTWARE);

        departmentRepository.save(testDepartment);

        userRepository.deleteAll();
        user = new User();
        user.setEmail("raghad23@gmail.com");
        user.setFirstName("Raghad");
        user.setLastName("Matar");
        user.setPassword(new BCryptPasswordEncoder().encode("raghad123"));
        userRepository.save(user);


        adminToken = jwtService.generateToken("admin@gmail.com", "admin");
        userToken = jwtService.generateToken("shifa@gmail.com", "employee");
    }
    //add department
    @Test
    public void addDepartment_success() throws Exception {
        String jsonBody = """
        {
            "name": "Accounting",
            "type": "HARDWARE"
        }
        """;
        mockMvc.perform(post("/taskManager/department")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Department added successfully")));
    }
    @Test
    public void addDepartment_notAdmin() throws Exception {
        String jsonBody = """
        {
            "name": "Accounting",
            "type": "HARDWARE"
        }
        """;
        mockMvc.perform(post("/taskManager/department")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(jsonBody))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Unauthorized: Only admin can add departments.")));
    }
    //Assign department Manager
    @Test
    public void assignDepartmentManager_success() throws Exception {

        mockMvc.perform(post("/taskManager/department/{departmentId}/setManager/{userId}" , testDepartment.getId(),user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Manager assigned successfully")));
    }
    @Test
    public void assignDepartmentManager_failed() throws Exception {

        mockMvc.perform(post("/taskManager/department/{departmentId}/setManager/{userId}" , testDepartment.getId(),100)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cant assign the manager to department")));
    }
    @Test
    public void assignDepartmentManager_notAdmin() throws Exception {

        mockMvc.perform(post("/taskManager/department/{departmentId}/setManager/{userId}" , testDepartment.getId(),user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Unauthorized: Only admin can add departments.")));
    }
    //Delete a department
    @Test
    public void deleteADepartment_success() throws Exception {

        mockMvc.perform(delete("/taskManager//department/{departmentId}" , testDepartment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Department deleted successfully")));
    }
    @Test
    public void deleteADepartment_failed() throws Exception {

        mockMvc.perform(delete("/taskManager//department/{departmentId}" , 200)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cant delete the department")));
    }
    @Test
    public void deleteADepartment_notAdmin() throws Exception {

        mockMvc.perform(delete("/taskManager//department/{departmentId}" , testDepartment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Unauthorized: Only admin can delete department.")));
    }
    //search a department
    @Test
    public void searchADepartment_success() throws Exception {
        testDepartment.setManager(user);
        departmentRepository.saveAndFlush(testDepartment);
        mockMvc.perform(get("/taskManager/departments/search?query=Mic")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(testDepartment.getId())))
                .andExpect(jsonPath("$[0].name", is(testDepartment.getName())));
    }
    @Test
    public void searchACompany_notAdmin() throws Exception {
        mockMvc.perform(get("/taskManager/departments/search?query=gma")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());


    }
    @Test
    public void searchACompany_noMatches() throws Exception {
        mockMvc.perform(get("/taskManager/departments/search?query=shh")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

    }


}
