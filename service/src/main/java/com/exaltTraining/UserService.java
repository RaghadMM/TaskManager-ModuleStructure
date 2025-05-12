package com.exaltTraining;



import java.util.List;

public interface UserService {
    User registerUser(User user);
    User login(String email, String password);
    User findUserByEmail(String email);
    List<User> getAllUsers();
    String resetPassword(int userId,String email, PasswordResetForm form);
    List<User> searchUsers(String query);
    String userCount();

}
