package com.exaltTraining;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("taskManager")
public class departmentController {

    private DepartmentService departmentService;
    @Autowired
    private JwtService jwtService;

    public departmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    //Add a new department API
    @PostMapping("/department")
    public String addDepartment(@RequestBody Department department, @RequestHeader("Authorization") String authHeader) {
        // Extract the token
        String token = authHeader.substring(7); // Remove "Bearer "

        // Extract role from token using JwtService
        String role = jwtService.extractUserRole(token);

        System.out.println(role);
        // Check if user is admin
        if (!"admin".equalsIgnoreCase(role)) {
            return "Unauthorized: Only admin can add departments.";
        }

        Department tempDep=departmentService.addDepartment(department);
        if(tempDep!=null) {
            return "Department added successfully";
        }
        else {
            return "Department not added";
        }

    }

    //Set department manager API
    @PostMapping("/department/{departmentId}/setManager/{userId}")
    public String addDepartmentManager(@PathVariable int departmentId,@PathVariable int userId, @RequestHeader("Authorization") String authHeader) {
        // Extract the token
        String token = authHeader.substring(7); // Remove "Bearer "

        // Extract role from token using JwtService
        String role = jwtService.extractUserRole(token);

        // Check if user is admin
        if (!"admin".equalsIgnoreCase(role)) {
            return "Unauthorized: Only admin can add departments.";
        }

        Boolean added= departmentService.assignManagerToDepartment(departmentId, userId);
        if(added) {
            return "Manager assigned successfully";
        }
        else {
            return "Cant assign the manager to department";
        }

    }

    //Assign users to a department API
    @PutMapping("/department/{departmentId}/setDepartmentMember/{userId}")
    public String assignMemberToDepartment(@PathVariable int departmentId, @PathVariable int userId, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return "Unauthorized: Only admin can assign members to department.";
        }
        String result = departmentService.assignDepartmentMember(departmentId, userId);
        if(result!=null) {
            return result;
        }
        else {
            return "Cant assign the member to department";
        }
    }
    //Get all departments API
    @GetMapping("/departments")
    public List<DepartmentPrinted> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return departments.stream().map(department -> {
            User manager = department.getManager();
            UserPrinted managerDTO = new UserPrinted(manager.getId(), manager.getFirstName(),manager.getLastName(),manager.getEmail(),manager.getRole().toString(),manager.getStatus().toString());
            return new DepartmentPrinted(department.getId(), department.getName(), managerDTO);
        }).collect(Collectors.toList());
    }
    //Delete a department API
    @DeleteMapping("/department/{departmentId}")
    public String deleteDepartment(@PathVariable int departmentId, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return "Unauthorized: Only admin can delete department.";
        }
        Boolean isDeleted = departmentService.deleteDepartment(departmentId);
        if(isDeleted) {
            return "Department deleted successfully";
        }
        else {
            return "Cant delete the department";
        }
    }
    @GetMapping("/departments/search")
    public ResponseEntity<List<DepartmentPrinted>> searchDepartments(@RequestParam("query") String query, @RequestHeader ("Authorization") String authHeader ) {
        String token = authHeader.substring(7);
        String role = jwtService.extractUserRole(token);
        if (!"admin".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // or UNAUTHORIZED

        }
        List<Department> departments = departmentService.searchDepartments(query);
        return ResponseEntity.ok(printDepartments(departments));
    }

    // A helper function to form the list of departments returned
    private List<DepartmentPrinted> printDepartments(List <Department> departments) {
        List<DepartmentPrinted> departmentPrinteds = departments.stream()
                .map(department -> new DepartmentPrinted(
                        department.getId(),
                        department.getName(),
                        new UserPrinted(
                                department.getManager().getId(),
                                department.getManager().getFirstName(),
                                department.getManager().getLastName(),
                                department.getManager().getEmail()
                        )
                ))
                .collect(Collectors.toList());
        return departmentPrinteds;

    }

}
