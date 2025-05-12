package com.exaltTraining;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name="name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private Type type;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="department_id")
    private List<User> tasks;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="manager_id")
    private User manager;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="department_id")
    private List<Team> teams;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="department_id")
    List<Project> projects;



    public Department() {
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Department(String name) {
        this.name = name;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
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

    public List<User> getTasks() {
        return tasks;
    }

    public void setTasks(List<User> tasks) {
        this.tasks = tasks;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tasks=" + tasks +
                ", manager=" + manager +
                '}';
    }
    public enum Type {
        QA,
        SOFTWARE,
        HARDWARE
    }
}
