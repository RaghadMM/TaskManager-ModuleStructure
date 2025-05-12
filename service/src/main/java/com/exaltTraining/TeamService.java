package com.exaltTraining;



import java.util.List;

public interface TeamService {
    Team createTeam(Team team, Department department);
    Boolean assignTeamLeader(int teamId, int UserId);
    String assignTeamMember(int teamId, int UserId, int departmentId);
    List<Team> getAllTeams(Department department);
    String deleteTeam(int teamId, User departmentManager);
    Team getTeam(int teamId);
    List<User> getAvailableTeamMembers(User teamLeader);
    List<Team> searchTeams(String query);
    String teamCount();

}
