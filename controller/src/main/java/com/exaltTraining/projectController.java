package com.exaltTraining;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/taskManager")
public class projectController {

    @Autowired
    private JwtService jwtService;
    private ProjectService projectService;
    private CompanyService companyService;
    private UserService userService;
    public projectController(ProjectService projectService, CompanyService companyService, UserService userService) {
        this.projectService = projectService;
        this.companyService = companyService;
        this.userService = userService;
    }

    //Add a new project by a company API
    @PostMapping("/project/{departmentId}")
    public String addProject(@RequestBody Project project,@PathVariable int departmentId, @RequestHeader("Authorization") String authHeader){
        // Extract the token
        String token = authHeader.substring(7); // Remove "Bearer "

        // Extract role from token using JwtService
        String role = jwtService.extractUserRole(token);
        String companyEmail=jwtService.extractUsername(token);
        System.out.println(role);
        if (!"company".equalsIgnoreCase(role)) {
            return "Unauthorized: Only Companies accounts can add projects.";
        }
        //Check for company approval
        Company company = companyService.findCompanyByEmail(companyEmail);
        if(company.getApproved()){

            project.setApproved(false);
            project.setCompany(company);

            String result = projectService.addProject(project,departmentId,company);
            if(result != null){
                return result;
            }
            else{
                return "The project has not been added yet!";
            }
        }
        else{
            return "The company is not approved yet";
        }


    }
    //Get project tasks API
    @GetMapping("/projectTasks/{projectId}")
    public List<taskPrinted> getProjectTasks(@PathVariable int projectId, @RequestHeader("Authorization") String authHeader){

        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        String companyEmail=jwtService.extractUsername(token);
        Company company = companyService.findCompanyByEmail(companyEmail);

        if(!"company".equalsIgnoreCase(role)) {
            return null;
        }
        if(!company.getApproved()){
            return null;
        }
        List<Task> tasks = projectService.getProjectTasks(projectId, company);

        return tasks.stream().map(task -> {


            return new taskPrinted(task.getId(), task.getTitle(), task.getDescription(), task.getStatus().toString(), task.getDeadline(),task.getPriority());
        }).collect(Collectors.toList());
    }

    //Get pending projects API
    @GetMapping("/pendingProjects")
    public List<projectPrinted> getPendingProjects(@RequestHeader("Authorization") String authHeader ){
        // Extract the token
        String token = authHeader.substring(7); // Remove "Bearer "

        // Extract role from token using JwtService
        String role = jwtService.extractUserRole(token);
        System.out.println(role);
        if (!"admin".equalsIgnoreCase(role)) {
            return null;
        }
        List<Project> projects = projectService.getPendingProjects();
        if(projects == null){
            return null;
        }
        return projects.stream().map(project -> {
            //company
            Company company= project.getCompany();
            companyPrinted companyP=new companyPrinted(company.getId(),company.getName(),company.getEmail());
            System.out.println(companyP);
            //department
            Department department= project.getDepartment();
            System.out.println("department "+department.getId());
            DepartmentPrinted departmentP =new DepartmentPrinted(department.getId(),department.getName());
            System.out.println("department printed "+departmentP);

            return new projectPrinted(project.getId(),project.getTitle(),project.getDescription(),companyP,departmentP);
        }).collect(Collectors.toList());

    }

    //To delay or cancel a project API
    @PutMapping("/project/{projectId}/{decision}")
    public String cancelOrDelayProject(@PathVariable int projectId, @PathVariable String decision, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        String companyEmail=jwtService.extractUsername(token);
        Company company = companyService.findCompanyByEmail(companyEmail);
        if (!"company".equalsIgnoreCase(role)) {
            return "Unauthorized: Only Companies accounts can cancel projects.";
        }
        return projectService.cancelOrDelayProject(projectId,decision,company);
    }

    //To get all company's pending projects
    @GetMapping("/Company/pendingProjects")
    public List<projectPrinted> getCompanyPendingProjects(@RequestHeader ("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        String companyEmail=jwtService.extractUsername(token);
        if(!"company".equalsIgnoreCase(role)) {
            return null;
        }
        Company company = companyService.findCompanyByEmail(companyEmail);
        List<Project> projects = projectService.getCompanyPendingProjects(company);

        List<projectPrinted> projectP = projects.stream()
                .map(project -> new projectPrinted(
                        project.getId(),
                        project.getTitle(),
                        project.getDescription(),
                        project.getStartDate(),
                        project.getEndDate()
                ))
                .collect(Collectors.toList());

        return projectP;

    }

    @GetMapping("/projects/{status}")
    public List<projectPrinted> getTeamActiveProject(@PathVariable String status,@RequestHeader ("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        String userEmail=jwtService.extractUsername(token);
        User teamLeader = userService.findUserByEmail(userEmail);
        if(!"team_manager".equalsIgnoreCase(role)) {
            return null;
        }
        List<Project> projects = projectService.getTeamProjects(teamLeader, status);



        return printProjects(projects);


    }

    @GetMapping("/projects/search")
    public ResponseEntity<List<projectPrinted>> searchProjects(@RequestParam("query") String query, @RequestHeader ("Authorization") String authHeader ) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // or UNAUTHORIZED
        }
        List<Project> projects = projectService.searchProjects(query);

        return ResponseEntity.ok(printProjects(projects));
    }

    //Change project status API
    @PutMapping("/project/changeStatus/{projectId}/{status}")
    public String changeProjectStatus(@PathVariable int projectId, @PathVariable String status, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        String email=jwtService.extractUsername(token);
        User teamLeader = userService.findUserByEmail(email);
        if (!"team_manager".equalsIgnoreCase(role)) {
            return "Unauthorized: Only team leaders accounts can change projects status.";
        }
        return projectService.changeProjectStatus(projectId,status,teamLeader);

    }

    // A helper function to form the list of projects returned
    private List<projectPrinted> printProjects(List <Project> projects) {
        List<projectPrinted> printedProjects = projects.stream().map(tempProject -> {
            Company company = tempProject.getCompany();
            companyPrinted cpr = new companyPrinted(
                    company.getId(),
                    company.getName(),
                    company.getEmail()
            );

            Department department = tempProject.getDepartment();
            DepartmentPrinted departmentP = new DepartmentPrinted(
                    department.getId(),
                    department.getName()
            );

            List<taskPrinted> printedTasks = tempProject.getTasks().stream()
                    .map(task -> new taskPrinted(
                            task.getId(),
                            task.getDescription(),
                            task.getTitle(),
                            task.getStatus().toString(),
                            task.getDeadline(),
                            task.getPriority()
                    ))
                    .collect(Collectors.toList());

            return new projectPrinted(
                    tempProject.getId(),
                    tempProject.getTitle(),
                    tempProject.getDescription(),
                    cpr,
                    departmentP,
                    tempProject.getStartDate(),
                    tempProject.getEndDate(),
                    printedTasks
            );
        }).collect(Collectors.toList());
        return printedProjects;

    }

}
