package com.exaltTraining;



import java.util.List;

public interface DepartmentService {
   Department addDepartment(Department department) ;
   Boolean assignManagerToDepartment(int departmentId,int managerId);
   String assignDepartmentMember(int departmentId, int managerId);
   List<Department> getAllDepartments();
   Boolean deleteDepartment(int departmentId);
   List<Department> searchDepartments(String query);
   String departmentCount();
}
