package com.exaltTraining;

//Helper class used to specify the form of returning department
public class DepartmentPrinted {
    private int id;
    private String name;
    private UserPrinted Manager;

    public DepartmentPrinted(int id, String name, UserPrinted Manager) {
        this.id = id;
        this.name = name;
        this.Manager = Manager;

    }

    public DepartmentPrinted(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public UserPrinted getUser() {
        return Manager;
    }

    public void setUser(UserPrinted Manager) {
        this.Manager = Manager;
    }

}
