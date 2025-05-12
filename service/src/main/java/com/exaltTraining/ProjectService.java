package com.exaltTraining;



import java.util.List;

public interface ProjectService {
    String addProject(Project project,int depId,Company company);
    List<Task> getProjectTasks(int depId, Company company);
    List<Project> getPendingProjects();
    //String approveProject(int projectId);
    Boolean checkForProjectAvailability(int departmentId, Project project, Company company,Boolean isDelay);
    String cancelOrDelayProject(int projectId, String decision, Company company);
    List<Project> getCompanyPendingProjects(Company company);
    List<Project> getTeamProjects(User teamLeader, String status);
    List<Project> searchProjects(String query);
    String changeProjectStatus(int projectId, String status, User teamLeader);
    String projectCount();


}
