package com.exaltTraining;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private NotificationRepository notificationRepository;


    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project project;
    private User user;
    private Company company;
    private Department department;
    private Team team;
    private Team team2;
    private Task task;
    private Task task2;

    @BeforeEach
    void setUp() {

        project = new Project();
        project.setTitle("UI");
        project.setDescription("website");

        user = new User();
        user.setFirstName("Ahmad");
        user.setLastName("Matar");
        user.setPassword(new BCryptPasswordEncoder().encode("ahmad123"));
        user.setEmail("ahmad.matar@gmail.com");

        department = new Department();
        department.setId(1);
        department.setName("Department 1");

        team = new Team();
        team.setId(1);
        team.setName("Team 1");

        team2 = new Team();
        team2.setId(2);
        team2.setName("Team 2");

        company = new Company();
        company.setId(1);
        company.setName("Company 1");
        company.setEmail("company1@gmail.com");

        task = new Task();
        task.setId(1);
        task.setTitle("Task 1");
        task2 = new Task();
        task2.setId(2);
        task2.setTitle("Task 2");

    }

    @Test
    void addProject_successfullyAdded() {
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(projectRepository.save(project)).thenReturn(project);
        when(emailService.sendSimpleMail(ArgumentMatchers.any(Email.class))).thenReturn("Email sent successfully");

        Team team = new Team();
        team.setProjects(new ArrayList<>());
        team.setTeamLeader(new User());

        department.setTeams(List.of(team));
        project.setStartDate(LocalDateTime.now().plusDays(1));
        project.setEndDate(LocalDateTime.now().plusDays(5));
        project.setTitle("New Project");
        project.setApproved(false);

        String result = projectService.addProject(project, department.getId(), company);

        assertEquals("Project has been added successfully!", result);
        verify(emailService).sendSimpleMail(ArgumentMatchers.any(Email.class));
        verify(projectRepository, times(1)).save(project);
    }
    @Test
    void addProject_emailSendFails() {
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(projectRepository.save(project)).thenReturn(project);

        Team team = new Team();
        team.setProjects(new ArrayList<>());
        team.setTeamLeader(new User());
        department.setTeams(List.of(team));

        project.setStartDate(LocalDateTime.now().plusDays(1));
        project.setEndDate(LocalDateTime.now().plusDays(5));
        project.setTitle("Test Project");

        when(emailService.sendSimpleMail(ArgumentMatchers.any(Email.class))).thenThrow(new RuntimeException("Email Failed"));

        String result = projectService.addProject(project, department.getId(), company);

        assertNull(result);
    }
    @Test
    void addProject_noAvailableTeam(){
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(projectRepository.save(project)).thenReturn(project);
        Team busyTeam = new Team();
        busyTeam.setTeamLeader(new User());

        Project ongoingProject = new Project();
        ongoingProject.setStartDate(LocalDateTime.now().minusDays(1));
        ongoingProject.setEndDate(LocalDateTime.now().plusDays(3));

        busyTeam.setProjects(List.of(ongoingProject));
        department.setTeams(List.of(busyTeam));

        project.setStartDate(LocalDateTime.now());
        project.setEndDate(LocalDateTime.now().plusDays(2));
        project.setTitle("Blocked Project");

        String result = projectService.addProject(project, department.getId(), company);

        assertEquals("Project has not been added yet, check for the email please!s", result);
        verify(emailService, times(1)).sendSimpleMail(ArgumentMatchers.any(Email.class));
        verify(projectRepository, times(1)).save(project);
    }
    @Test
    void addProject_invalidDepartment() {
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.empty());

        String result = projectService.addProject(project, department.getId(), company);
        assertNull(result);

        verify(departmentRepository, times(1)).findById(department.getId());
        verify(projectRepository, never()).save(project);
        verify(emailService, never()).sendSimpleMail(ArgumentMatchers.any(Email.class));

    }

    //Cancel or delay a project
    @Test
    void delayProject_successfullyDelayed() {

        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);
        when(notificationRepository.save(ArgumentMatchers.any(Notification.class))).thenReturn(new Notification());

        Team busyTeam = new Team();
        busyTeam.setTeamLeader(new User());

        Project ongoingProject = new Project();
        ongoingProject.setStartDate(LocalDateTime.now().minusDays(1));
        ongoingProject.setEndDate(LocalDateTime.now().plusDays(3));

        busyTeam.setProjects(List.of(ongoingProject));
        department.setTeams(List.of(busyTeam));

        project.setStartDate(LocalDateTime.now());
        project.setEndDate(LocalDateTime.now().plusDays(2));
        project.setTitle("Blocked Project");
        project.setCompany(company);

        projectService.addProject(project, department.getId(), company);

        String result = projectService.cancelOrDelayProject(project.getId(),"delay", company);
        assertNotNull(result);
        assertEquals("The project added successfully" , result);
        verify(projectRepository, times(3)).save(project);
        verify(emailService, times(1)).sendSimpleMail(ArgumentMatchers.any(Email.class));
    }

    @Test
    void cancelProject_successfullyCanceled() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        project.setCompany(company);

        String result = projectService.cancelOrDelayProject(project.getId(),"cancel", company);
        assertNotNull(result);
        assertEquals("Project Canceled" , result);

        verify(projectRepository, times(1)).delete(project);

    }

    @Test
    void cancelOrDelayProject_projectNotOwnedByTheCompany() {
        Company company2 = new Company();
        company2.setEmail("copany2@gmail.com");

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        project.setCompany(company);
        String result = projectService.cancelOrDelayProject(project.getId(),"cancel", company2);

        assertNotNull(result);
        assertEquals("This company does not own this project" , result);
        verify(projectRepository, never()).delete(project);

    }
    @Test
    void cancelOrDelayProject_projectNotOwnedByTheDepartment() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        String result = projectService.cancelOrDelayProject(project.getId(),"cancel", company);

        assertNotNull(result);
        assertEquals("This project does not exist" , result);

    }
    //Get company pending projects
    @Test
    void getCompanyPendingProjects() {
        Project project2 = new Project();
        project2.setTitle("Blocked Project");
        project2.setCompany(company);
        project.setCompany(company);

        project.setApproved(false);
        project2.setApproved(false);

        List<Project> projects = Arrays.asList(project, project2);
        company.setProjects(projects);

        List<Project> result = projectService.getCompanyPendingProjects(company);
        assertNotNull(result);
        assertEquals(2,result.size());
    }
    @Test
    void getCompanyPendingProjects_emptyList() {
        Project project2 = new Project();
        project2.setTitle("Blocked Project");
        project2.setCompany(company);
        project.setCompany(company);

        project.setApproved(true);
        project2.setApproved(true);

        List<Project> projects = Arrays.asList(project, project2);
        company.setProjects(projects);

        List<Project> result = projectService.getCompanyPendingProjects(company);
        assertNotNull(result);
        assertEquals(0,result.size());
    }
    //Get project tasks
    @Test
    void getProjectTasks(){
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        List<Task> tasks = Arrays.asList(task, task2);
        project.setTasks(tasks);
        project.setCompany(company);
        project.setApproved(true);

        List<Task> result = projectService.getProjectTasks(project.getId(),company);
        assertNotNull(result);
        assertEquals(2,result.size());
    }
    @Test
    void getProjectTasks_emptyList() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        List<Task> tasks = Arrays.asList(task, task2);
        project.setTasks(null);
        project.setCompany(company);
        project.setApproved(true);

        List<Task> result = projectService.getProjectTasks(project.getId(),company);
        assertNull(result);

    }
    @Test
    void getProjectTasks_projectNotOwnedByTheCompany() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        Company company2 = new Company();
        company2.setEmail("copany2@gmail.com");
        project.setCompany(company2);
        List<Task> tasks = Arrays.asList(task, task2);
        project.setTasks(tasks);
        List<Task> result = projectService.getProjectTasks(project.getId(),company);
        assertNull(result);
    }
    @Test
    void getProjectTasks_projectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        List<Task> result = projectService.getProjectTasks(project.getId(),company);
        assertNull(result);
    }
    //Get team projects by status
    @Test
    void getTeamProjectsByStatus_pendingList() {
        Project project2 = new Project();
        project2.setTitle("Blocked Project");

        project.setApproved(true);
        project2.setApproved(true);
        project.setAssignedTeam(team);
        project2.setAssignedTeam(team);
        project.setStatus("Pending");
        project2.setStatus("Pending");

        List<Project> projects = Arrays.asList(project, project2);
        team.setProjects(projects);


        when(teamRepository.findTeamByTeamLeader(user)).thenReturn(team);

        List<Project> result = projectService.getTeamProjects(user,"Pending");
        assertNotNull(result);
        assertEquals(2,result.size());


    }
    @Test
    void getTeamProjectsByStatus_pendingEmpty() {
        Project project2 = new Project();
        project2.setTitle("Blocked Project");

        project.setApproved(true);
        project2.setApproved(true);
        project.setAssignedTeam(team);
        project2.setAssignedTeam(team);
        project.setStatus("In process");
        project2.setStatus("in process");

        List<Project> projects = Arrays.asList(project, project2);
        team.setProjects(projects);


        when(teamRepository.findTeamByTeamLeader(user)).thenReturn(team);

        List<Project> result = projectService.getTeamProjects(user,"Pending");
        assertNotNull(result);
        assertEquals(0,result.size());


    }
    @Test
    void getTeamProjectsByStatus_inProcessList() {
        Project project2 = new Project();
        project2.setTitle("Blocked Project");

        project.setApproved(true);
        project2.setApproved(true);
        project.setAssignedTeam(team);
        project2.setAssignedTeam(team);
        project.setStatus("in process");
        project2.setStatus("in process");

        List<Project> projects = Arrays.asList(project, project2);
        team.setProjects(projects);


        when(teamRepository.findTeamByTeamLeader(user)).thenReturn(team);

        List<Project> result = projectService.getTeamProjects(user,"in process");
        assertNotNull(result);
        assertEquals(2,result.size());


    }
    @Test
    void getTeamProjectsByStatus_inProcessEmpty() {
        Project project2 = new Project();
        project2.setTitle("Blocked Project");

        project.setApproved(true);
        project2.setApproved(true);
        project.setAssignedTeam(team);
        project2.setAssignedTeam(team);
        project.setStatus("pending");
        project2.setStatus("pending");

        List<Project> projects = Arrays.asList(project, project2);
        team.setProjects(projects);


        when(teamRepository.findTeamByTeamLeader(user)).thenReturn(team);

        List<Project> result = projectService.getTeamProjects(user,"in process");
        assertNotNull(result);
        assertEquals(0,result.size());


    }
    @Test
    void getTeamProjectsByStatus_finishedList() {
        Project project2 = new Project();
        project2.setTitle("Blocked Project");

        project.setApproved(true);
        project2.setApproved(true);
        project.setAssignedTeam(team);
        project2.setAssignedTeam(team);
        project.setStatus("finished");
        project2.setStatus("finished");

        List<Project> projects = Arrays.asList(project, project2);
        team.setProjects(projects);


        when(teamRepository.findTeamByTeamLeader(user)).thenReturn(team);

        List<Project> result = projectService.getTeamProjects(user,"finished");
        assertNotNull(result);
        assertEquals(2,result.size());


    }
    @Test
    void getTeamProjectsByStatus_finishedEmpty() {
        Project project2 = new Project();
        project2.setTitle("Blocked Project");

        project.setApproved(true);
        project2.setApproved(true);
        project.setAssignedTeam(team);
        project2.setAssignedTeam(team);
        project.setStatus("In process");
        project2.setStatus("in process");

        List<Project> projects = Arrays.asList(project, project2);
        team.setProjects(projects);


        when(teamRepository.findTeamByTeamLeader(user)).thenReturn(team);

        List<Project> result = projectService.getTeamProjects(user,"finished");
        assertNotNull(result);
        assertEquals(0,result.size());
    }





}



