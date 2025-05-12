package com.exaltTraining;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    List<Project> getProjectsByApproved(Boolean approved);

    Project findProjectByTasks(List<Task> tasks);
    @Query("SELECT p FROM Project p WHERE " +
            "p.title LIKE CONCAT('%',:query, '%')" +
            "Or p.description LIKE CONCAT('%', :query, '%')")
    List<Project> searchProjects(String query);

    int getProjectsByStatus(String finished);
}
