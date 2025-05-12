package com.exaltTraining;


import java.time.LocalDateTime;
import java.util.List;

//Helper class used to specify the form of returning project
public class projectPrinted {
    private int projectId;
    private String name;
    private String description ;
    private companyPrinted company;
    private DepartmentPrinted department;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<taskPrinted> tasks;


    public projectPrinted(int projectId, String name, String description, LocalDateTime startTime, LocalDateTime endTime) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;

    }

    public projectPrinted(int projectId, String name, String description, companyPrinted company, DepartmentPrinted department) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.company = company;
        this.department = department;
    }

    public projectPrinted(int projectId, String name, String description, companyPrinted company, DepartmentPrinted department, LocalDateTime startTime, LocalDateTime endTime, List<taskPrinted> tasks) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.company = company;
        this.department = department;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tasks = tasks;
    }

    public List<taskPrinted> getTasks() {
        return tasks;
    }

    public void setTasks(List<taskPrinted> tasks) {
        this.tasks = tasks;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public companyPrinted getCompany() {
        return company;
    }

    public void setCompany(companyPrinted company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DepartmentPrinted getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentPrinted department) {
        this.department = department;
    }
}
