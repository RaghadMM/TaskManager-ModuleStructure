package com.exaltTraining;



import java.time.LocalDateTime;
//Helper class used to specify the form of returning task
public class taskPrinted {
    private int taskId;
    private String taskName;
    private String taskDescription;
    private String taskStatus;
    private LocalDateTime taskDeadline;
    private Task.Priority priority;

    public taskPrinted(int taskId, String taskName, String taskDescription, String taskStatus, LocalDateTime taskDeadline, Task.Priority priority) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
        this.taskDeadline = taskDeadline;
        this.priority = priority;
    }

    public Task.Priority getPriority() {
        return priority;
    }

    public void setPriority(Task.Priority priority) {
        this.priority = priority;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public LocalDateTime getTaskDeadline() {
        return taskDeadline;
    }

    public void setTaskDeadline(LocalDateTime taskDeadline) {
        this.taskDeadline = taskDeadline;
    }
}
