package com.exaltTraining;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    //private AuthenticationManager authenticationManager;
    private EntityManager entityManager;
    private EmailService emailService;



    public UserServiceImpl(UserRepository userRepository, EmailService emailService,BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
    // Create an account for a user by the admin
    //The user receives email includes the password
    @Override
    public User registerUser(User user) {
        Email email = new Email(user.getEmail(),"We are happy to be with us! \n here is your account password: "+ user.getPassword(),"Welcome to our company!");
        String result =emailService.sendSimpleMail(email);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        return user;

    }
    //Log in for the users.
    //generate a JWT token if the user is authenticated.
    @Override
    public User login(String email, String password) {

        List<User> users=userRepository.findAll();
        for(User user:users) {
            if(user.getEmail().equals(email) && bCryptPasswordEncoder.matches(password,user.getPassword())) {
                return user;
            }
        }
        System.out.println("User not found");
        return null; // or throw custom exception
    }

    //Helper function to get a user by his email
    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }
    //Get all users in the system
    @Override
    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();

        }
        catch (Exception e) {
            return null;
        }
    }

    //Reset password
    //Checks for user authorization, old passwords matching
    @Override
    public String resetPassword(int userId, String email, PasswordResetForm form) {
        Optional<User> user=userRepository.findById(userId);
        String oldPassedPassword = form.getOldPassword();
        String newPassedPassword = form.getNewPassword();
        if(user.isPresent()) {
            User tempUser =user.get();
            String realPassword = tempUser.getPassword();
            if(tempUser.getEmail().equals(email)) {
                if(bCryptPasswordEncoder.matches(oldPassedPassword,realPassword)) {
                    tempUser.setPassword(bCryptPasswordEncoder.encode(newPassedPassword));
                    userRepository.save(tempUser);
                    return "Password reset successful";
                }
                else {
                    return "The passwords do not match";
                }

            }
            else{
                return "Unauthorized user";
            }
        }
        return "User not found";
    }

    @Override
    public List<User> searchUsers(String query) {
        List<User> users=userRepository.searchUser(query);
        return users;
    }

    @Override
    public String userCount() {
        return "The total number of users is " + userRepository.count() + " users \n";

    }

}
