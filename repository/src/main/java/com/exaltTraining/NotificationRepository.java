package com.exaltTraining;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    @Query("SELECT n FROM Notification n WHERE n.title LIKE %:taskTitle% AND n.user = :user")
    List<Notification> findByTaskTitleAndUser(@Param("taskTitle") String taskTitle, @Param("user") User user);

    @Query("SELECT n FROM Notification n WHERE n.title LIKE %:meetingTitle% AND n.user = :user")
    List<Notification> findByMeetingTitleAndUser(@Param("meetingTitle") String meetingTitle, @Param("user") User user);

}
