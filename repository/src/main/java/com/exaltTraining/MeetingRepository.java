package com.exaltTraining;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Integer> {
    @Query("SELECT DISTINCT m FROM Meeting m " +
            "LEFT JOIN m.participants p " +
            "WHERE (m.organizer = :user OR p = :user) " +
            "AND (m.title LIKE CONCAT('%', :query, '%') " +
            "OR m.description LIKE CONCAT('%', :query, '%'))")
    List<Meeting> searchMeetings(@Param("user") User user, @Param("query") String query);

}
