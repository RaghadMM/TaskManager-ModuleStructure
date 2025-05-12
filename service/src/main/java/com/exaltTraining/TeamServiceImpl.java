package com.exaltTraining;


import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TeamServiceImpl implements TeamService {

    private TeamRepository teamRepository;
    private UserRepository userRepository;
    private NotificationRepository notificationRepository;

    public TeamServiceImpl(TeamRepository teamRepository,UserRepository userRepository,NotificationRepository notificationRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    //Create a new team by the department manager
    @Override
    public Team createTeam(Team team, Department department) {
        try{
            System.out.println(department);
            team.setDepartment(department);
            teamRepository.saveAndFlush(team);
            return team;
        }
        catch(Exception e){
            return null;
        }
    }

    //Set a leader for a team by department manager
    //The process is restricted for the team department manager
    @Override
    public Boolean assignTeamLeader(int teamId, int UserId) {
        Optional<User> tempUser= userRepository.findById(UserId);
        Optional<Team> tempTeam= teamRepository.findById(teamId);
        try{
            if(tempUser.isPresent() && tempTeam.isPresent()){
                User user = tempUser.get();
                Team team = tempTeam.get();

                if(team.getTeamLeader()==null && team.getDepartment() == user.getDepartment()){
                    team.setTeamLeader(user);
                    user.setRole(User.Role.TEAM_MANAGER);

                    teamRepository.save(team);
                    userRepository.save(user);
                    notificationRepository.save(new Notification("Team leader", "You are assigned as a team leader for "+ team.getName() + "team", false, team.getTeamLeader()));

                    return true;
                }
            }
        }
        catch(Exception e){
            return false;
        }

        return false;
    }

    //Add members to a team by the department manager
    //Check that the user is not related to another team and for department matching between the user and the team
    @Override
    public String assignTeamMember(int teamId, int UserId, int departmentId) {
        Optional<User> tempUser= userRepository.findById(UserId);
        Optional<Team> tempTeam= teamRepository.findById(teamId);
        if(tempUser.isPresent() && tempTeam.isPresent()) {
            User user = tempUser.get();
            Team team = tempTeam.get();
            //check for department matching and not assigned to a team
            if(user.getDepartment()!=null){

                System.out.println(user.getDepartment());
                System.out.println(departmentId);
                if(user.getDepartment().getId() == departmentId) {

                    if (user.getTeam() == null) {
                        user.setTeam(team);
                        userRepository.save(user);
                        try{
                            notificationRepository.save(new Notification("Task Member assignment", "You are assigned as "+ team.getName() + "member", false, user));

                            return "The user has been assigned to the team";
                        }
                        catch(Exception e){
                            return null;
                        }


                    } else {
                        return "The user is already assigned to a team";
                    }
                }
                else{
                    return "The User is from another department!";
                }
            }
            else{
                return "The User is not assigned to a department yet!";
            }


        }
        return null;


    }
    //Get all teams
    @Override
    public List<Team> getAllTeams(Department department) {
        try{
            return department.getTeams();
        }
        catch(Exception e){
            return null;
        }

    }

    //To delete a team by the department manager
    //The function checks that the team is really assigned with the department manager department
    @Override
    public String deleteTeam(int teamId, User departmentManager) {
        Optional <Team> tempTeam = teamRepository.findById(teamId);
        if(tempTeam.isPresent()){
            Team team = tempTeam.get();
            if(team.getDepartment()== departmentManager.getDepartment())
            {
                try{
                    teamRepository.deleteById(teamId);
                    return "Team has been deleted";
                }
                catch(Exception e){
                    return "Error while deleting team";
                }
            }
            else{
                return "The team is not assigned with this department!";
            }
        }
        return "The team not found!";

    }

    //Get a team info by team id
    @Override
    public Team getTeam(int teamId) {
        Team team = teamRepository.findById(teamId).get();
        if(team!=null){
            return team;
        }
        else{
            return null;
        }

    }

    //Get available members in a team by the team leader
    @Override
    public List<User> getAvailableTeamMembers(User teamLeader) {
        Team team = teamRepository.findTeamByTeamLeader(teamLeader);
        List<User> members = team.getTeamMembers();
        List<User> availableMembers = new ArrayList<>();
        for(User user : members){
            if(user.getStatus() == User.Status.Available){
                availableMembers.add(user);

            }
        }
        return availableMembers;
    }

    @Override
    public List<Team> searchTeams(String query) {
        List<Team> teams=teamRepository.searchTeams(query);
        return teams;
    }

    @Override
    public String teamCount() {
        return "The total number of teams is " + teamRepository.count() + " teams \n";
    }
}
