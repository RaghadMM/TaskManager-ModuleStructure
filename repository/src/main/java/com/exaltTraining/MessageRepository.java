package com.exaltTraining;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = :sender AND m.recipient = :receiver) OR " +
            "(m.sender = :receiver AND m.recipient = :sender) " +
            "ORDER BY m.sendingTime ASC")
    List<Message> getChatBetweenUsers(@Param("sender") User sender, @Param("receiver") User receiver);
}
