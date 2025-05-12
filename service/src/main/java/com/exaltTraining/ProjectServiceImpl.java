package com.exaltTraining;


import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {

    private ProjectRepository projectRepository;
    private DepartmentRepository departmentRepository;
    private TeamRepository teamRepository;
    private EmailService emailService;
    private NotificationRepository notificationRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, DepartmentRepository departmentRepository, EmailService emailService,TeamRepository teamRepository, NotificationRepository notificationRepository) {
        this.projectRepository = projectRepository;
        this.departmentRepository = departmentRepository;
        this.emailService = emailService;
        this.teamRepository = teamRepository;
        this.notificationRepository = notificationRepository;
    }

    //Add a new project by external approved companies
    //The project will be pending till it approved by the admin
    @Override
    public String addProject(Project project, int depId, Company company) {
        Optional<Department> tempDepartment= departmentRepository.findById(depId);
        if(tempDepartment.isPresent()){
            Department department = tempDepartment.get();
            try{
                project.setDepartment(department);
                Boolean available=checkForProjectAvailability(depId,project,company,false);
                if(available){

                    return "Project has been added successfully!";
                }
                else{
                    return "Project has not been added yet, check for the email please!s";
                }
            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    //Get project related tasks
    @Override
    public List<Task> getProjectTasks(int depId, Company company) {
        Optional<Project> tempProject= projectRepository.findById(depId);
        if(tempProject.isPresent()){
            if(tempProject.get().getCompany().equals(company)){
                List<Task> tasks=tempProject.get().getTasks();
                if(tasks!=null){
                    return tasks;
                }
                else{
                    return null;
                }
            }

        }
        return null;

    }

    //Get pending projects to check them by the admin
    @Override
    public List<Project> getPendingProjects() {
        try{
            return projectRepository.getProjectsByApproved(false);

        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //Approve a project by the admin, accept or reject
//    @Override
//    public String approveProject(int projectId) {
//        Optional<Project> tempProject= projectRepository.findById(projectId);
//        if(tempProject.isPresent()){
//            Project project=tempProject.get();
//            project.setApproved(true);
//            projectRepository.save(project);
//            return "The project approved";
//        }
//        return "The project does not exist";
//    }

    //This function helps to find a team available on a new project time
    //If the dates available, send confirmation email to the company
    //if not, search for earliest available time and send a regret email
    @Override
    public Boolean checkForProjectAvailability(int departmentId, Project newProject, Company company, Boolean isDelay) {
        LocalDateTime newStart = newProject.getStartDate();
        LocalDateTime newEnd = newProject.getEndDate();
        long projectDuration=newStart.until(newEnd, ChronoUnit.DAYS);
        LocalDateTime latestEnd = LocalDateTime.MIN;
        LocalDateTime suggestedStartDate=null;
        LocalDateTime suggestedEnd=null;
        String departmentName=newProject.getDepartment().getName();
        Optional<Department> tempDepartment = departmentRepository.findById(departmentId);
        if (tempDepartment.isPresent()) {
            Department department = tempDepartment.get();
            List<Team> teams = department.getTeams();

            for (Team team : teams) {
                boolean isAvailable = true;
                for (Project existingProject : team.getProjects()) {
                    if (!(existingProject.getEndDate().isBefore(newStart) || existingProject.getStartDate().isAfter(newEnd))) {
                        // There's a project in the same time
                        isAvailable = false;
                        if (existingProject.getEndDate().isAfter(latestEnd)) {
                            latestEnd = existingProject.getEndDate();
                        }
                        break;
                    }
                }
                if (isAvailable) {
                    //Assign the project to the available team
                    newProject.setAssignedTeam(team);
                    newProject.setApproved(true);
                    projectRepository.save(newProject);
                    //Sending an email for the company
                    String body  = "Dear " + company.getName() + ",\n\n" +
                            "We are pleased to inform you that your project titled \"" + newProject.getTitle() + "\" has been successfully reviewed and accepted by our team.\n\n" +
                            "The project is now scheduled to begin on " + newProject.getStartDate() + " and will be handled by one of our specialized teams in the " + department.getName() + " department.\n" +
                            "Our team will reach out to you shortly for any additional coordination or clarification if needed.\n\n" +
                            "We look forward to collaborating with you and ensuring the success of your project.\n\n" +
                            "If you have any questions or need further assistance, feel free to contact us.\n\n" +
                            "Best regards,\n" +
                            "Task Manager Team";

                    Email email = new Email(company.getEmail(), body, "Project Approved!");
                    if(!isDelay){
                        emailService.sendSimpleMail(email);
                    }
                    notificationRepository.save(new Notification("Project Assignment", "A new project assigned to your team, check for it!", false, team.getTeamLeader()));
                    return true; // Team is free in the given date range
                }
                else{
                    if (suggestedStartDate == null || latestEnd.isBefore(suggestedStartDate)) {
                        suggestedStartDate = latestEnd.plusDays(1);

                    }
                     suggestedEnd = suggestedStartDate.plusDays(projectDuration);

                }
            }
        }
        //No available teams at the assigned date
        //reject the project

        //check for the first available date suggested to start the project on it
        String body = "Dear " + company.getName() + ",\n\n" +
                "We regret to inform you that your project titled \"" + newProject.getTitle() + "\" cannot be accepted at the originally requested time due to team unavailability.\n\n" +
                "However, we highly value your collaboration, and we are pleased to suggest a new start date: " + suggestedStartDate + ", with an estimated end date of " + suggestedEnd + ".\n" +
                "This timeline aligns with the availability of our team in the " + departmentName + " department.\n\n" +
                "You can confirm, request a delay, or cancel the project through our company website.\n\n" +
                "If you have any questions or need further assistance, feel free to contact us.\n\n" +
                "Best regards,\n" +
                "Task Manager Team";

        Email email = new Email(company.getEmail(), body, "Project Rescheduling Suggestion");
        emailService.sendSimpleMail(email);
        newProject.setApproved(false);
        newProject.setStartDate(suggestedStartDate);
        newProject.setEndDate(suggestedEnd);
        projectRepository.save(newProject);
        return false;
    }

    //Used if the project time is not available, the company can delay it or cancel
    @Override
    public String cancelOrDelayProject(int projectId, String decision, Company company) {
        Optional<Project> tempProject = projectRepository.findById(projectId);
        if (tempProject.isPresent()) {
            Project project = tempProject.get();
            Company projectCompany=project.getCompany();
            Department dep=project.getDepartment();

            //check for company authorization
            if(projectCompany==company){
                if(decision.equals("cancel")){
                    projectRepository.delete(project);
                    return "Project Canceled";
                }
                else if(decision.equals("delay")){
                    if(checkForProjectAvailability(dep.getId(),project,company,true)){
                        project.setApproved(true);
                        projectRepository.save(project);
                        return "The project added successfully";
                    }
                    else{
                        return "The project is not added!";
                    }

                }

            }
            else{
                //not the project company
                return "This company does not own this project";
            }
        }
        return "This project does not exist";

    }

    //To get the company pending projects, to see the suggested new dates
    @Override
    public List<Project> getCompanyPendingProjects(Company company) {
        List<Project> projects = company.getProjects();
        List<Project> pendingProjects = new ArrayList<Project>();
        for (Project project : projects) {
            if (!project.getApproved()) {
                pendingProjects.add(project);

            }
        }
        return pendingProjects;
    }

    //To get team related projects based on its status, pending, in_process or finished
    @Override
    public List<Project> getTeamProjects(User teamLeader, String status) {

        Team team = teamRepository.findTeamByTeamLeader(teamLeader);
        if(team==null){
            return null;
        }
        List<Project> projects = new ArrayList<>();
        for (Project project : team.getProjects()) {
            if (project.getStatus().equals(status)) {
                projects.add(project);
            }
        }
        return projects;
    }

    @Override
    public List<Project> searchProjects(String query) {
        return projectRepository.searchProjects(query);
    }

    //Change project status by the team leader
    //Finished if all tasks are checked
    @Override
    public String changeProjectStatus(int projectId, String status, User teamLeader) {
        try{
            Project project = projectRepository.findById(projectId).get();
            if(project.getAssignedTeam().getTeamLeader().equals(teamLeader)){
                if(status.equals("finished")){
                    List<Task> tasks = project.getTasks();
                    System.out.println(tasks);
                    boolean isFinished = true;
                    for (Task task : tasks) {
                        if(task.getStatus()!= Task.Status.CHECKED){
                            isFinished = false;
                            break;
                        }

                    }
                    if(isFinished){
                        project.setStatus("finished");
                        projectRepository.save(project);
                        return "Project Updated Successfully";
                    }
                    else{
                        return "The project tasks are not finished!";
                    }
                }
                else if(status.equals("in-process")){
                    project.setStatus("in_process");
                    projectRepository.save(project);
                    return "Project Updated Successfully";

                }
                else{
                    return "Unknown status";
                }
            }
            else{
                return "This project is not assigned to this team";
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return "Error";
        }
    }

    @Override
    public String projectCount() {
        List<Project> projects = projectRepository.findAll();
        int finished = 0;
        int inProcess = 0;
        for (Project project : projects) {
            if(project.getStatus().equals("finished")){
                finished++;
            }
            else if(project.getStatus().equals("in_process")){
                inProcess++;
            }
        }

        return "The total number of projects is " + projectRepository.count() + " projects\n" +
                "The active projects :" + inProcess + " projects \n"+
                "The finished projects :" + finished + " projects \n";

    }

}
