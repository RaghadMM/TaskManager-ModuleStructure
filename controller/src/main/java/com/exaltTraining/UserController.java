package com.exaltTraining;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("taskManager")
public class UserController {

    private UserService userService;
    private CompanyService companyService;
    private TaskService taskService;
    private TeamService teamService;
    private ProjectService projectService;
    private DepartmentService departmentService;

    @Autowired
    private JwtService jwtService;


    public UserController(UserService userService, CompanyService companyService, TaskService taskService, TeamService teamService, ProjectService projectService, DepartmentService departmentService,JwtService jwtService) {
        this.userService = userService;
        this.companyService = companyService;
        this.taskService = taskService;
        this.teamService = teamService;
        this.projectService = projectService;
        this.departmentService = departmentService;
        this.jwtService = jwtService;

    }

    // Create an account for a user by the admin API
    @PostMapping("/user")
    public String createUser(@RequestBody User user, @RequestHeader("Authorization") String authHeader) {
        try{
            //Extract the user role
            String token = authHeader.substring(7);
            String role = jwtService.extractUserRole(token);
            if (!"admin".equalsIgnoreCase(role)) {
                return "Unauthorized: Only admin can register users.";
            }
            User newUser =userService.registerUser(user);
            System.out.println(newUser);
            if (newUser != null) {
                return "User registered successfully";
            }
            else {
                return "User could not be registered";
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return "Error creating user: check for email validation ";
        }
    }

    //Log in for the users API
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        User authenticatedUser =userService.login(email,password);
        System.out.println(authenticatedUser);
        if(authenticatedUser != null){
            String token = jwtService.generateToken(email,authenticatedUser.getRole().toString());
            return "User logged in successfully \n Here is the token: " + token;
        }
        else{
            return "User is not authenticated";
        }
    }

    //Get all users API
    @GetMapping("/users")
    public List<UserPrinted> getUsers() {
        List <User> users= userService.getAllUsers();
        return printUser(users);
    }
    //Reset password API
    @PutMapping("/passwordReset/{userId}")
    public String changePassword(@PathVariable int userId, @RequestBody PasswordResetForm form, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        return userService.resetPassword(userId, email,form);

    }
    @GetMapping("/users/search")
    public ResponseEntity<List<UserPrinted>> searchUsers(@RequestParam("query") String query, @RequestHeader ("Authorization") String authHeader ) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // or UNAUTHORIZED
        }

        List<User> users = userService.searchUsers(query);

        return ResponseEntity.ok(printUser(users));
    }

    @GetMapping("/analyticsDashboard")
    public String analyticsDashboard(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return "Not an admin role";
        }
        return userService.userCount() + departmentService.departmentCount() + teamService.teamCount() + projectService.projectCount() + taskService.taskCount() +companyService.companiesCount();

    }

    // A helper function to form the list of users returned
    private List<UserPrinted> printUser(List <User> users) {

        List<UserPrinted> userPrinteds = users.stream()
                .map(user -> new UserPrinted(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole().toString(),
                        user.getStatus().toString()
                ))
                .collect(Collectors.toList());

        return userPrinteds;

    }
}
