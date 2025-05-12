package com.exaltTraining;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/taskManager")
public class teamController {

    private TeamService teamService;
   @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    public teamController(JwtService jwtService, TeamService teamService) {
        this.jwtService = jwtService;
        this.teamService = teamService;
    }

    //Create new team API
    @PostMapping("/team")
    public String createTeam(@RequestBody Team team, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer "

        // Extract role from token using JwtService
        String role = jwtService.extractUserRole(token);
        String username = jwtService.extractUsername(token);
        // Check if user is a department manager
        if (!"department_manager".equalsIgnoreCase(role)) {
            return "Unauthorized: Only department managers can add teams.";
        }
        User user = userService.findUserByEmail(username);
        Department department=user.getDepartment();
        System.out.println(username);
        System.out.println("dep "+department);
        Team newTeam = teamService.createTeam(team,department);
        if (newTeam != null) {
            return "Team created successfully";
        }
        return "Team creation failed";

    }

    //Set team leader API
    @PostMapping("/team/{teamId}/setLeader/{userId}")
    public String setLeader(@PathVariable int teamId, @PathVariable int userId, @RequestHeader("Authorization") String authHeader) {
        // Extract the token
        String token = authHeader.substring(7); // Remove "Bearer "

        // Extract role from token using JwtService
        String role = jwtService.extractUserRole(token);

        if (!"department_manager".equalsIgnoreCase(role)) {
            return "Unauthorized: Only department managers can assign to teams.";
        }

        Boolean added= teamService.assignTeamLeader(teamId, userId);
        if(added) {
            return "Leader assigned successfully";
        }
        else {
            return "Cant assign Leader to team";
        }
    }

    //Add team member API
    @PostMapping("/team/{teamId}/assignMember/{userId}")
    public String assignTeamMember(@PathVariable int teamId, @PathVariable int userId, @RequestHeader("Authorization") String authHeader) {
        // Extract the token
        String token = authHeader.substring(7); // Remove "Bearer "

        // Extract role from token using JwtService
        String username = jwtService.extractUsername(token);
        User user = userService.findUserByEmail(username); // the user who signed in
        String role = jwtService.extractUserRole(token);

        if (!"department_manager".equalsIgnoreCase(role)) {
            return "Unauthorized: Only department managers can assign to teams.";
        }
        //pass the signed-in user department to check if the team member department is the same
        String result= teamService.assignTeamMember(teamId, userId,user.getDepartment().getId());
        if(result!=null) {
            return result;
        }
        else {
            return "Cant assign Member to team";
        }
    }

    //Get all teams API
    @GetMapping("/teams")
    public List<teamPrinted> getAllTeams(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        String username = jwtService.extractUsername(token);
        User user = userService.findUserByEmail(username);
        Department department=user.getDepartment();
        if(!"department_manager".equalsIgnoreCase(role)) {
            return null;
        }
        List<Team> teams = teamService.getAllTeams(department);
        return printTeam(teams);

    }
    //Delete a team
    @DeleteMapping("/team/{teamId}")
    public String deleteTeam(@PathVariable int teamId, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userService.findUserByEmail(username);
        String role = jwtService.extractUserRole(token);
        if (!"department_manager".equalsIgnoreCase(role)) {
            return "Unauthorized: Only department managers can delete teams.";
        }
        String result = teamService.deleteTeam(teamId, user);
        if(result!=null) {
            return result;
        }
        else {
            return "Cant delete Team";
        }
    }
    //Get team members
    @GetMapping("/team/{teamId}")
    public teamPrinted getTeam( @PathVariable int teamId,@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        String role = jwtService.extractUserRole(token);
        User user = userService.findUserByEmail(username);
        Team theTeam = teamService.getTeam(teamId);
        if (theTeam.getTeamLeader()!= user || theTeam==null || !"team_manager".equalsIgnoreCase(role)) {
            return null;
        }
        //The team members
        List<User> members=theTeam.getTeamMembers();
        List<UserPrinted> printedMembers = theTeam.getTeamMembers().stream()
                .map(member -> new UserPrinted(
                        member.getId(),
                        member.getFirstName(),
                        member.getLastName(),
                        member.getEmail()
                ))
                .collect(Collectors.toList());

        teamPrinted printedTeam = new teamPrinted(
                theTeam.getId(),
                theTeam.getName(),
                new UserPrinted(
                        theTeam.getTeamLeader().getId(),
                        theTeam.getTeamLeader().getFirstName(),
                        theTeam.getTeamLeader().getLastName(),
                        theTeam.getTeamLeader().getEmail()
                ),
                new DepartmentPrinted(
                        theTeam.getDepartment().getId(),
                        theTeam.getDepartment().getName()
                ),
                printedMembers
        );
        return printedTeam;

    }
    @GetMapping("/availableMembers")
    public List<UserPrinted> getAllAvailableUsers(@RequestHeader ("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userService.findUserByEmail(username);
        List<User> availableUsers= teamService.getAvailableTeamMembers(user);
        List<UserPrinted> printedMembers = availableUsers.stream()
                .map(member -> new UserPrinted(
                        member.getId(),
                        member.getFirstName(),
                        member.getLastName(),
                        member.getEmail()
                ))
                .collect(Collectors.toList());
        return printedMembers;
    }
    @GetMapping("/teams/search")
    public ResponseEntity<List<teamPrinted>> searchTeams(@RequestParam("query") String query, @RequestHeader ("Authorization") String authHeader ) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // or UNAUTHORIZED

        }
        List<Team> teams = teamService.searchTeams(query);


        return ResponseEntity.ok(printTeam(teams));
    }

    // A helper function to form the list of teams returned
    private List<teamPrinted> printTeam(List <Team> teams) {

        List<teamPrinted> teamPrinteds = teams.stream()
                .map(team -> new teamPrinted(
                        team.getId(),
                        team.getName(),
                        new UserPrinted(
                                team.getTeamLeader().getId(),
                                team.getTeamLeader().getFirstName(),
                                team.getTeamLeader().getLastName(),
                                team.getTeamLeader().getEmail()
                        ),
                        new DepartmentPrinted(
                                team.getDepartment().getId(),
                                team.getDepartment().getName()
                        ),
                        team.getTeamMembers().stream()
                                .map(member -> new UserPrinted(
                                        member.getId(),
                                        member.getFirstName(),
                                        member.getLastName(),
                                        member.getEmail()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return teamPrinteds;

    }

}
