package com.exaltTraining;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;


import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private JwtService jwtService;

    private User user;
    private String adminToken;
    private String userToken;

    @BeforeEach
    public void setUp() {
        departmentRepository.deleteAll();

        userRepository.deleteAll();

        user = new User();
        user.setFirstName("Raghad");
        user.setLastName("Matar");
        user.setEmail("raghad123@gmail.com");
        user.setPassword(bCryptPasswordEncoder.encode("raghad123")); // 🔐 Encode password here

        userRepository.save(user);

        adminToken = jwtService.generateToken("admin@gmail.com", "admin");
        userToken = jwtService.generateToken("shifa@gmail.com", "employee");
    }

    //Get all users
    @Test
    public void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/taskManager/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$[0].lastName", is(user.getLastName())))
                .andExpect(jsonPath("$[0].email", is(user.getEmail())));
    }

    //Login
    @Test
    public void login_withValidCredentials_returnsToken() throws Exception {
        String jsonBody = """
                {
                    "email": "raghad123@gmail.com",
                    "password": "raghad123"
                }
                """;

        mockMvc.perform(post("/taskManager/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User logged in successfully")))
                .andExpect(content().string(containsString("token")));
    }

    //    @Test
//    public void login_withMissingFields_returnsBadRequest() throws Exception {
//        String jsonBody = "{}";
//
//        mockMvc.perform(post("/taskManager/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonBody))
//                .andExpect(status().isBadRequest());
//    }
    //Create new user account
    @Test
    public void createUser_success() throws Exception {
        User newUser = new User();
        newUser.setFirstName("Shifa");
        newUser.setLastName("Matar");
        newUser.setEmail("shifa@gmail.com");
        newUser.setPassword("shifa123");

        mockMvc.perform(post("/taskManager/user")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User registered successfully")));
    }
    @Test
    public void createUser_notAdmin() throws Exception {
        User newUser = new User();
        newUser.setFirstName("Shifa");
        newUser.setLastName("Matar");
        newUser.setEmail("shifa@gmail.com");
        newUser.setPassword("shifa123");

        mockMvc.perform(post("/taskManager/user")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Unauthorized: Only admin can register users.")));
    }

    @Test
    public void createUser_missedToken() throws Exception {
        User newUser = new User();
        newUser.setFirstName("Shifa");
        newUser.setLastName("Matar");
        newUser.setEmail("shifa@gmail.com");
        newUser.setPassword("shifa123");

        mockMvc.perform(post("/taskManager/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }
//    @Test
//    public void createUser_missedUserObjectFields() throws Exception {
//        User newUser = new User();
//        newUser.setFirstName("Shifa");
//        newUser.setEmail("shifa@gmail.com");
//        newUser.setPassword("shifa123");
//
//        mockMvc.perform(post("/taskManager/user")
//                        .header("Authorization", "Bearer " + adminToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(new ObjectMapper().writeValueAsString(user)))
//                .andExpect(status().isBadRequest());
//    }

    //Search user
    @Test
    public void searchAUser_success() throws Exception {
        mockMvc.perform(get("/taskManager/users/search?query=gma")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$[0].lastName", is(user.getLastName())))
                .andExpect(jsonPath("$[0].email", is(user.getEmail())));

    }
    @Test
    public void searchAUser_notAdmin() throws Exception {
        mockMvc.perform(get("/taskManager/users/search?query=gma")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isForbidden());


    }
    @Test
    public void searchAUser_noMatches() throws Exception {
        mockMvc.perform(get("/taskManager/users/search?query=shh")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

    }

}
