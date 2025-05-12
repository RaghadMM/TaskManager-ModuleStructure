package com.exaltTraining;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Integer> {
    Team findTeamByTeamLeader(User teamLeader);
    @Query("SELECT t FROM Team t WHERE " +
            "t.name LIKE CONCAT('%',:query, '%')")
    List<Team> searchTeams (String query);
}
