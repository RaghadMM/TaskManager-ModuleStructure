package com.exaltTraining;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {
    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;


    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;
    private User user;


    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(3);
        department.setName("Microsoft");
        department.setType(Department.Type.SOFTWARE);

        user = new User();
        user.setFirstName("Ahmad");
        user.setLastName("Matar");
        user.setPassword(new BCryptPasswordEncoder().encode("ahmad123"));
        user.setEmail("ahmad.matar@gmail.com");


    }

    //Add department
    @Test
    void addDepartment_success() {
        when(departmentRepository.save(department)).thenReturn(department);


        Department result = departmentService.addDepartment(department);

        assertNotNull(result);
        assertEquals("Microsoft", result.getName());

        verify(departmentRepository, times(1)).save(department);

    }
    @Test
    void addDepartment_throwsException() {
        when(departmentRepository.save(department)).thenThrow(new RuntimeException());


        Department result = departmentService.addDepartment(department);

        assertNull(result);

    }
    //Assign manager
    @Test
    void assignManagerToDepartment_success() {
        department.setManager(null);
        user.setRole(User.Role.EMPLOYEE);

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(any(Notification.class));

        Boolean result = departmentService.assignManagerToDepartment(1,1);
        assertTrue(result);
        verify(departmentRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);

    }
    @Test
    void assignManagerToDepartment_invalidDepartment() {
        when(departmentRepository.findById(1)).thenReturn(Optional.empty());
        when(userRepository.findById(1)).thenReturn(Optional.of(user));


        Boolean result = departmentService.assignManagerToDepartment(1,1);
        assertFalse(result);
        verify(departmentRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);

    }
    @Test
    void assignManagerToDepartment_invalidUser() {
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(userRepository.findById(1)).thenReturn(Optional.empty());


        Boolean result = departmentService.assignManagerToDepartment(1,1);
        assertFalse(result);
        verify(departmentRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);

    }
    @Test
    void assignManagerToDepartment_departmentAlreadyHasAManager() {
        User newUser = new User();
        newUser.setRole(User.Role.DEPARTMENT_MANAGER);
        department.setManager(newUser);
        user.setRole(User.Role.EMPLOYEE);

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));


        Boolean result = departmentService.assignManagerToDepartment(1,1);
        assertFalse(result);
        verify(departmentRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);

    }
    @Test
    void assignManagerToDepartment_theUserIsAlreadyAManager() {

        department.setManager(null);
        user.setRole(User.Role.DEPARTMENT_MANAGER);

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        Boolean result = departmentService.assignManagerToDepartment(1,1);
        assertFalse(result);
        verify(departmentRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);

    }
    //Assign member to department
    @Test
    void assignMemberToDepartment_success() {

        user.setRole(User.Role.EMPLOYEE);
        user.setDepartment(null);

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));


        String result = departmentService.assignDepartmentMember(1,1);
        assertNotNull(result);
        assertEquals("The user has been assigned to the department", result);
        verify(departmentRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);

    }
    @Test
    void assignMemberToDepartment_departmentNotFound() {

        user.setRole(User.Role.EMPLOYEE);
        user.setDepartment(null);

        when(departmentRepository.findById(1)).thenReturn(Optional.empty());
        when(userRepository.findById(1)).thenReturn(Optional.of(user));


        String result = departmentService.assignDepartmentMember(1,1);
        assertNull(result);
        verify(departmentRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);

    }
    @Test
    void assignMemberToDepartment_userNotFound() {

        user.setRole(User.Role.EMPLOYEE);
        user.setDepartment(null);

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(userRepository.findById(1)).thenReturn(Optional.empty());


        String result = departmentService.assignDepartmentMember(1,1);
        assertNull(result);
        verify(departmentRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);

    }
    @Test
    void assignMemberToDepartment_userAlreadyAssignedToDepartment() {

        user.setRole(User.Role.EMPLOYEE);
        user.setDepartment(department);

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));


        String result = departmentService.assignDepartmentMember(1,1);
        assertNotNull(result);
        assertEquals("The user is already assigned to a department", result);
        verify(departmentRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);

    }
    //Get all departments
    @Test
    void getAllDepartments_returnAList(){
        Department department1 = new Department();
        department1.setName("Department 1");

        List<Department> departments = Arrays.asList(department, department1);
        when(departmentRepository.findAll()).thenReturn(departments);

        List<Department> result = departmentService.getAllDepartments();
        assertEquals(2, result.size());
        verify(departmentRepository, times(1)).findAll();


    }
    @Test
    void getAllDepartments_returnEmptyList(){

        when(departmentRepository.findAll()).thenReturn(null);

        List<Department> result = departmentService.getAllDepartments();
        assertNull(result);
        verify(departmentRepository, times(1)).findAll();


    }
    @Test
    void getAllDepartments_throwsException(){

        when(departmentRepository.findAll()).thenThrow(new RuntimeException());

        List<Department> result = departmentService.getAllDepartments();
        assertNull(result);
        verify(departmentRepository, times(1)).findAll();


    }
    //Delete a department
    @Test
    void deleteADepartment_success() {

        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        Boolean result = departmentService.deleteDepartment(department.getId());
        assertTrue(result);
    }
    @Test
    void deleteADepartment_invalidId() {

        when(departmentRepository.findById(department.getId())).thenReturn(Optional.empty());
        Boolean result = departmentService.deleteDepartment(department.getId());
        assertFalse(result);
    }
    @Test
    void deleteADepartment_throwException() {

        when(departmentRepository.findById(department.getId()))
                .thenReturn(Optional.of(department));
        doThrow(new RuntimeException())
                .when(departmentRepository)
                .deleteById(department.getId());
        Boolean result = departmentService.deleteDepartment(department.getId());
        assertFalse(result);
    }







}
