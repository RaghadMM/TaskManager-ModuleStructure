package com.exaltTraining;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE " +
            "u.firstName LIKE CONCAT('%',:query, '%')" +
            "Or u.lastName LIKE CONCAT('%', :query, '%')" +
            "Or u.email LIKE CONCAT('%', :query, '%')"
    )
    List<User> searchUser (String query);

    User findUserByEmail(String email);
}
