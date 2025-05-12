package com.exaltTraining;

import java.util.List;

//Helper class used to specify the form of returning team
public class teamPrinted {
    private int teamId;
    private String teamName;
    private UserPrinted leader;
    private DepartmentPrinted department;
    private List<UserPrinted> Members;
    public teamPrinted() {
        super();
    }

    public teamPrinted(int teamId, String teamName, UserPrinted leader, DepartmentPrinted department) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.leader = leader;
        this.department = department;
    }

    public teamPrinted(int teamId, String teamName, UserPrinted leader, DepartmentPrinted department, List<UserPrinted> members) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.leader = leader;
        this.department = department;
        Members = members;
    }

    public List<UserPrinted> getMembers() {
        return Members;
    }

    public void setMembers(List<UserPrinted> members) {
        Members = members;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public UserPrinted getLeader() {
        return leader;
    }

    public void setLeader(UserPrinted leader) {
        this.leader = leader;
    }

    public DepartmentPrinted getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentPrinted department) {
        this.department = department;
    }
}
