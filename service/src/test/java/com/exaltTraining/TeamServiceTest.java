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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {
    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;


    @InjectMocks
    private TeamServiceImpl teamService;

    private Team team;
    private User user;
    private Department department;


    @BeforeEach
    void setUp() {

        team = new Team();

        team.setName("Team 1");


        user = new User();
        user.setFirstName("Ahmad");
        user.setLastName("Matar");
        user.setPassword(new BCryptPasswordEncoder().encode("ahmad123"));
        user.setEmail("ahmad.matar@gmail.com");

        department = new Department();
        department.setId(1);
        department.setName("Department 1");



    }
    //Create team
    @Test
    void addTeam_success() {

        when(teamRepository.saveAndFlush(team)).thenReturn(team);

        Team result = teamService.createTeam(team,department);
        assertNotNull(result);
        assertEquals(team.getName(), result.getName());
        verify(teamRepository,times(1)).saveAndFlush(team);
    }
    @Test
    void addTeam_throwException() {

        when(teamRepository.saveAndFlush(team)).thenThrow(new RuntimeException("Something went wrong"));

        Team result = teamService.createTeam(team,department);
        assertNull(result);

        verify(teamRepository,times(1)).saveAndFlush(team);
    }
    //Assign team leader
    @Test
    void assignTeamLeader_success() {

        team.setDepartment(department);
        user.setDepartment(department);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(notificationRepository.save(any(Notification.class))).thenReturn(any(Notification.class));

        Boolean result = teamService.assignTeamLeader(user.getId(),team.getId());
        assertTrue(result);
        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());
        verify(notificationRepository,times(1)).save(any(Notification.class));

    }
    @Test
    void assignTeamLeader_throwException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("Something went wrong"));

        Boolean result = teamService.assignTeamLeader(user.getId(),team.getId());
        assertFalse(result);
        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());
        verify(notificationRepository,times(1)).save(any(Notification.class));

    }
    @Test
    void assignTeamLeader_userNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));


        Boolean result = teamService.assignTeamLeader(user.getId(),team.getId());
        assertFalse(result);
        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());


    }
    @Test
    void assignTeamLeader_teamNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.empty());


        Boolean result = teamService.assignTeamLeader(user.getId(),team.getId());
        assertFalse(result);
        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());
    }
    @Test
    void assignTeamLeader_userBelongsToDifferentDepartment() {
        Department department2 = new Department();
        department.setName("Department 2");
        user.setDepartment(department2);
        team.setDepartment(department);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));


        Boolean result = teamService.assignTeamLeader(user.getId(),team.getId());
        assertFalse(result);
        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());
    }
    @Test
    void assignTeamLeader_teamAlreadyHasALeader() {

        user.setDepartment(department);
        team.setDepartment(department);
        team.setTeamLeader(user);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));


        Boolean result = teamService.assignTeamLeader(user.getId(),team.getId());
        assertFalse(result);
        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());
    }
    //Assign team member
    @Test
    void assignTeamMember_success() {
        user.setDepartment(department);
        team.setDepartment(department);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(notificationRepository.save(any(Notification.class))).thenReturn(any(Notification.class));

        String result = teamService.assignTeamMember(team.getId(),user.getId(),department.getId());
        assertNotNull(result);
        assertEquals("The user has been assigned to the team", result);
        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());
        verify(notificationRepository,times(1)).save(any(Notification.class));
    }
    @Test
    void assignTeamMember_throwException() {
        user.setDepartment(department);
        team.setDepartment(department);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("Something went wrong"));

        String result = teamService.assignTeamMember(team.getId(),user.getId(),department.getId());
        assertNull(result);

        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());
        verify(notificationRepository,times(1)).save(any(Notification.class));
    }
    @Test
    void assignTeamMember_userNotFound() {
        user.setDepartment(department);
        team.setDepartment(department);

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));


        String result = teamService.assignTeamMember(team.getId(),user.getId(),department.getId());
        assertNull(result);
        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());

    }
    @Test
    void assignTeamMember_teamNotFound() {
        user.setDepartment(department);
        team.setDepartment(department);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.empty());

        String result = teamService.assignTeamMember(team.getId(),user.getId(),department.getId());
        assertNull(result);

        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());

    }
    @Test
    void assignTeamMember_userBelongsToDifferentDepartment() {
        Department department2 = new Department();
        department2.setId(2);
        department2.setName("Department 2");

        user.setDepartment(department2);
        team.setDepartment(department);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));


        String result = teamService.assignTeamMember(team.getId(),user.getId(),department.getId());
        assertNotNull(result);
        assertEquals("The User is from another department!", result);

        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());

    }
    @Test
    void assignTeamMember_userBelongsToAnotherTeam() {
        Team team2 = new Team();
        team2.setId(2);
        team2.setName("Team 2");
        user.setDepartment(department);
        team.setDepartment(department);
        user.setTeam(team2);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));


        String result = teamService.assignTeamMember(team.getId(),user.getId(),department.getId());
        assertNotNull(result);
        assertEquals("The user is already assigned to a team", result);

        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());

    }
    @Test
    void assignTeamMember_userIsNotBelongToDepartmentYet() {

        user.setDepartment(null);
        team.setDepartment(department);


        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));


        String result = teamService.assignTeamMember(team.getId(),user.getId(),department.getId());
        assertNotNull(result);
        assertEquals("The User is not assigned to a department yet!", result);

        verify(userRepository,times(1)).findById(user.getId());
        verify(teamRepository,times(1)).findById(team.getId());

    }
    //Get department teams
    @Test
    void getDepartmentTeams(){
        Team team2 = new Team();
        team2.setId(2);
        team2.setName("Team 2");
        team.setDepartment(department);
        team2.setDepartment(department);

        List<Team> teams = Arrays.asList(team2, team);
        department.setTeams(teams);
        List<Team> result = teamService.getAllTeams(department);
        assertNotNull(result);
        assertEquals(2, result.size());


    }
    @Test
    void getDepartmentTeams_returnEmptyList(){
        Team team2 = new Team();
        team2.setId(2);
        team2.setName("Team 2");


        List<Team> teams = Arrays.asList(team2, team);
        department.setTeams(null);
        List<Team> result = teamService.getAllTeams(department);
        assertNull(result);

    }
    //Delete a team
    @Test
    void deleteATeam_success(){
        team.setDepartment(department);
        user.setDepartment(department);
        user.setRole(User.Role.DEPARTMENT_MANAGER);
        department.setManager(user);
        List<Team> teams = Collections.singletonList(team);
        department.setTeams(teams);

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        String result = teamService.deleteTeam(team.getId(),user);

        assertNotNull(result);
        assertEquals("Team has been deleted", result);

        verify(teamRepository,times(1)).findById(team.getId());
    }
    @Test
    void deleteATeam_throwsException(){
        team.setDepartment(department);
        user.setDepartment(department);
        user.setRole(User.Role.DEPARTMENT_MANAGER);
        department.setManager(user);
        List<Team> teams = Collections.singletonList(team);
        department.setTeams(teams);

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        doThrow(new RuntimeException())
                .when(teamRepository)
                .deleteById(team.getId());

        String result = teamService.deleteTeam(team.getId(),user);

        assertNotNull(result);
        assertEquals("Error while deleting team", result);

        verify(teamRepository,times(1)).findById(team.getId());
    }
    @Test
    void deleteATeam_teamIsNotOwnedByTheDepartment(){
        Department department2 = new Department();
        department2.setId(2);
        department2.setName("Department 2");
        team.setDepartment(department);
        user.setDepartment(department2);
        user.setRole(User.Role.DEPARTMENT_MANAGER);
        department2.setManager(user);
        List<Team> teams = Collections.singletonList(team);
        department.setTeams(teams);

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        String result = teamService.deleteTeam(team.getId(),user);

        assertNotNull(result);
        assertEquals("The team is not assigned with this department!", result);

        verify(teamRepository,times(1)).findById(team.getId());
    }
    @Test
    void deleteATeam_teamIsNotFound(){


        when(teamRepository.findById(team.getId())).thenReturn(Optional.empty());
        String result = teamService.deleteTeam(team.getId(),user);

        assertNotNull(result);
        assertEquals("The team not found!", result);

        verify(teamRepository,times(1)).findById(team.getId());
    }
    //Get a team available members
    @Test
    void getTeamAvailableMembers_success(){
        User user2 = new User();
        user2.setId(2);
        user2.setRole(User.Role.EMPLOYEE);
        user2.setTeam(team);
        user2.setStatus(User.Status.Busy);

        User user3 = new User();
        user3.setId(3);
        user3.setRole(User.Role.TEAM_MANAGER);
        user3.setTeam(team);
        user3.setStatus(User.Status.Available);

        user.setTeam(team);
        user.setStatus(User.Status.Available);

        List<User> users = Arrays.asList(user2, user,user3);
        team.setTeamMembers(users);
        when(teamRepository.findTeamByTeamLeader(user3)).thenReturn(team);

        List<User> result = teamService.getAvailableTeamMembers(user3);
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(teamRepository,times(1)).findTeamByTeamLeader(user3);
    }








}
