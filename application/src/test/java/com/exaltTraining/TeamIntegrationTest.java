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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class TeamIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Team team;
    private User user;
    private Department department;
    private String adminToken;
    private String userToken;
    private String departmentManagerToken;


    @BeforeEach
    public void setUp() {

        teamRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();

        team = new Team();
        team.setName("Team");
        teamRepository.save(team);

        user = new User();
        user.setEmail("raghad23@gmail.com");
        user.setFirstName("Raghad");
        user.setLastName("Matar");
        user.setPassword(new BCryptPasswordEncoder().encode("raghad123"));
        userRepository.save(user);


        department = new Department();
        department.setName("Department 1");
        department.setManager(user);
        departmentRepository.save(department);

        user.setDepartment(department);
        userRepository.saveAndFlush(user);

        adminToken = jwtService.generateToken("admin@gmail.com", "admin");
        userToken = jwtService.generateToken("shifa@gmail.com", "employee");
        departmentManagerToken = jwtService.generateToken("raghad23@gmail.com", "department_manager");
    }
    @Test
    public void addTeam_success() throws Exception {
        String jsonBody = """
        {
            "name": "team2"
        }
        """;
        mockMvc.perform(post("/taskManager/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + departmentManagerToken)
                        .content(jsonBody))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Team created successfully")));

    }
    @Test
    public void addTeam_notDepartmentManager() throws Exception {
        String jsonBody = """
        {
            "name": "team2"
        }
        """;
        mockMvc.perform(post("/taskManager/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(jsonBody))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Unauthorized: Only department managers can add teams.")));

    }
    //search a team
    @Test
    public void searchATeam_success() throws Exception {

        team.setTeamLeader(user);
        team.setDepartment(department);
        teamRepository.save(team);

        mockMvc.perform(get("/taskManager/teams/search?query=tea")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamId", is(team.getId())))
                .andExpect(jsonPath("$[0].teamName", is(team.getName())));
    }
    @Test
    public void searchATeam_notAdmin() throws Exception {
        mockMvc.perform(get("/taskManager/teams/search?query=tea")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());


    }
    @Test
    public void searchACompany_noMatches() throws Exception {
        mockMvc.perform(get("/taskManager/teams/search?query=shh")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

    }
}
