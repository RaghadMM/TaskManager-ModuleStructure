package com.exaltTraining;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/taskManager")
public class taskController {

    @Autowired
    private JwtService jwtService;
    private TaskService taskService;
    private CompanyService companyService;
    private UserService userService;

    public taskController(TaskService taskService, CompanyService companyService, UserService userService) {
        this.taskService = taskService;
        this.companyService = companyService;
        this.userService = userService;
    }

    //Add new task to a project API
    @PostMapping("/task/project/{projectId}")
    public String addTask(@PathVariable int projectId, @RequestBody Task task,@RequestHeader("Authorization") String authHeader){
        // Extract the token
        String token = authHeader.substring(7); // Remove "Bearer "

        // Extract role from token using JwtService
        String role = jwtService.extractUserRole(token);
        String companyEmail=jwtService.extractUsername(token);
        System.out.println(role);
        if (!"company".equalsIgnoreCase(role)) {
            return "Unauthorized: Only Companies accounts can add projects.";
        }

        Company company = companyService.findCompanyByEmail(companyEmail);
        if(company.getApproved()){
            String result=taskService.addTaskToAProject(task,projectId,company);
            if(result!=null){
                return result;
            }
            else{
                return "Task not added";
            }
        }
        else{
            return "The company is not approved yet.";
        }
    }
    //Assign task to a member API
    @PutMapping("/task/{taskId}/member/{memberId}")
    public String assignATaskToMember(@PathVariable int taskId, @PathVariable int memberId, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        String username = jwtService.extractUsername(token);
        User user = userService.findUserByEmail(username);
        if(!"team_manager".equalsIgnoreCase(role)) {
            return "Unauthorized: Only team leaders accounts can assign tasks.";
        }
        String result = taskService.assignTaskToAMember(taskId,memberId,user);
        return result;

    }
    //Get pending tasks API
    @GetMapping("/pendingTasks")
    public List<taskPrinted> getPendingTasks(@RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        String username = jwtService.extractUsername(token);
        User user = userService.findUserByEmail(username);
        if(!"team_manager".equalsIgnoreCase(role)) {
            return null;
        }
        List<Task> tasks = taskService.getTeamBendingTasks(user);

        return printTask(tasks);
    }
    //Delete a pending task API
    @DeleteMapping("/task/{taskId}")
    public String deleteTask(@PathVariable int taskId, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        String username = jwtService.extractUsername(token);
        Company company = companyService.findCompanyByEmail(username);
        if(!"company".equalsIgnoreCase(role)) {
            return "Unauthorized: Only Companies accounts can delete tasks.";
        }
        String result = taskService.deleteTaskFromAProject(taskId,company);
        return result;
    }

    //Mark a task as done by the employee
    @PutMapping("/task/{taskId}")
    public String updateTaskStatus(@PathVariable int taskId, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        String username = jwtService.extractUsername(token);
        User user = userService.findUserByEmail(username);
        if(!"employee".equalsIgnoreCase(role)) {
            return "Unauthorized: Only Employee accounts can update tasks.";
        }
        String result=taskService.updateTaskStatus(taskId,user);
        return result;

    }
    @GetMapping("/tasks/search")
    public ResponseEntity<List<taskPrinted>> searchTasks(@RequestParam("query") String query, @RequestHeader ("Authorization") String authHeader ) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return null;
        }
        List<Task> tasks = taskService.searchTasks(query);

        return ResponseEntity.ok(printTask(tasks));
    }

    //Get tasks by status API
    @GetMapping("/tasks/{status}")
    public List<taskPrinted> getTasksByStatus(@PathVariable String status, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User user = userService.findUserByEmail(email);
        List<Task> tasks = taskService.getTasksByStatus(user,status);
         return printTask(tasks);
    }

    // A helper function to form the list of tasks returned
    private List<taskPrinted> printTask(List <Task> tasks) {

        List<taskPrinted> taskPrinteds = tasks.stream()
                .map(task -> new taskPrinted(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus().toString(),
                        task.getDeadline(),
                        task.getPriority()
                ))
                .collect(Collectors.toList());

        return taskPrinteds;

    }
}
