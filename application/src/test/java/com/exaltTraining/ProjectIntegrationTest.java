package com.exaltTraining;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class ProjectIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private JwtService jwtService;

    private Department testDepartment;
    private Company testCompany;
    private Project testProject;
    private Team testTeam;

    private String companyToken;
    private String userToken;
    private String adminToken;

    @BeforeEach
    public void setUp() {
        projectRepository.deleteAll();
        companyRepository.deleteAll();
        departmentRepository.deleteAll();

        testProject = new Project();
        testProject.setTitle("Test Project");
        testProject.setDescription("Test Project");
        projectRepository.save(testProject);

        testDepartment = new Department();
        testDepartment.setName("Test Department");
        departmentRepository.save(testDepartment);

        testCompany = new Company();
        testCompany.setName("Test Company");
        testCompany.setEmail("company@gmail.com");
        companyRepository.save(testCompany);

        testTeam = new Team();
        testTeam.setName("Test Team");
        testTeam.setDepartment(testDepartment);
        teamRepository.save(testTeam);

        companyToken = jwtService.generateToken("company@gmail.com", "company");
        userToken = jwtService.generateToken("user@gmail.com", "employee");
        adminToken = jwtService.generateToken("admin@gmail.com", "admin");


    }
    @Test
    public void addProject_success() throws Exception {
        testCompany.setApproved(true);
        companyRepository.save(testCompany);

        String jsonBody = """
        {
            "title": "Broche store",
            "description": "Designing website fo a clothes market",
            "startDate": "2025-04-25T00:00:00",
            "endDate": "2025-06-24T00:00:00"
        }
        """;
        mockMvc.perform(post("/taskManager/project/{departmentId}", testDepartment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + companyToken)
                        .content(jsonBody))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Project has been added successfully!")));

    }
    @Test
    public void addProject_unapprovedCompany() throws Exception {
        testCompany.setApproved(false);
        companyRepository.save(testCompany);

        String jsonBody = """
        {
            "title": "Broche store",
            "description": "Designing website fo a clothes market",
            "startDate": "2025-04-25T00:00:00",
            "endDate": "2025-06-24T00:00:00"
        }
        """;
        mockMvc.perform(post("/taskManager/project/{departmentId}", testDepartment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + companyToken)
                        .content(jsonBody))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("The company is not approved yet")));

    }
    @Test
    public void addProject_notACompany() throws Exception {

        String jsonBody = """
        {
            "title": "Broche store",
            "description": "Designing website fo a clothes market",
            "startDate": "2025-04-25T00:00:00",
            "endDate": "2025-06-24T00:00:00"
        }
        """;
        mockMvc.perform(post("/taskManager/project/{departmentId}", testDepartment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(jsonBody))

                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Unauthorized: Only Companies accounts can add projects.")));

    }
    //Search a project
    @Test
    public void searchAProject_success() throws Exception {
        testProject.setCompany(testCompany);
        testProject.setDepartment(testDepartment);
        testProject.setAssignedTeam(testTeam);
        projectRepository.save(testProject);

        mockMvc.perform(get("/taskManager/projects/search?query=test")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId", is(testProject.getId())))
                .andExpect(jsonPath("$[0].name", is(testProject.getTitle())));
    }
    @Test
    public void searchATeam_notAdmin() throws Exception {
        mockMvc.perform(get("/taskManager/projects/search?query=tea")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());


    }
    @Test
    public void searchACompany_noMatches() throws Exception {
        mockMvc.perform(get("/taskManager/projects/search?query=shh")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

    }

}
