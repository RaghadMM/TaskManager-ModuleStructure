package com.exaltTraining;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;


    //Get all users testing
    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setFirstName("Raghad");
        user1.setLastName("Matar");
        user1.setEmail("raghad@gmail.com");
        user1.setPassword("raghad");

        User user2 = new User();
        user2.setFirstName("Shifa");
        user2.setLastName("Matar");
        user2.setEmail("shifa@gmail.com");
        user2.setPassword("shifa");


        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();

    }
    @Test
    void testGetAllUsers_emptyList() {

        List<User> users = new ArrayList<>();

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(0, result.size());
        verify(userRepository, times(1)).findAll();

    }
    @Test
    void testGetAllUsers_exceptionThrown() {
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        List<User> result = null;
        try {
            result = userService.getAllUsers();
        } catch (Exception e) {
            // Optionally log or assert exception message
        }

        //when exception catching expected nul returned
        assertNull(result);
        verify(userRepository, times(1)).findAll();

    }

    @Test
    void login_successful() {
        String email = "raghad@gmail.com";
        String password = "raghad123";
        String hashedPassword = "$2a$12$r.ErjHkDFTmjo90D8ijQdema66/GKPUY6ENfUNCI78YeG/ppSgbBu";

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(hashedPassword);

        List<User> users = List.of(newUser);

        when(userRepository.findAll()).thenReturn(users);
        when(bCryptPasswordEncoder.matches(password, hashedPassword)).thenReturn(true);

        User result = userService.login(email, password);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }
    @Test
    void login_incorrectPassword_returnsNull() {
        String email = "raghad@gmail.com";
        String password = "raghad123";
        String hashedPassword = "$2a$12$r.ErjHkDFTmjo90D8ijQdema66/GKPUY6ENfUNCI78YeG/ppSgbBu";

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(hashedPassword);

        List<User> users = List.of(newUser);

        when(userRepository.findAll()).thenReturn(users);
        when(bCryptPasswordEncoder.matches(password, hashedPassword)).thenReturn(false);

        User result = userService.login(email, password);

        assertNull(result);

    }
    @Test
    void login_userNotFound_returnsNull() {
        String email = "raghad@gmail.com";
        String password = "raghad123";
        String hashedPassword = "$2a$12$r.ErjHkDFTmjo90D8ijQdema66/GKPUY6ENfUNCI78YeG/ppSgbBu";
        String incorrectEmail = "raghad1@gmail.com";

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(hashedPassword);

        List<User> users = List.of(newUser);

        when(userRepository.findAll()).thenReturn(users);

        User result = userService.login(incorrectEmail, password);

        assertNull(result);

    }
    @Test
    void createUser_successful() {
        String firstName = "Raghad";
        String lastName = "Matar";
        String email = "raghad@gmail.com";
        String password = "raghad123";
        String hashedPassword = "$2a$12$r.ErjHkDFTmjo90D8ijQdema66/GKPUY6ENfUNCI78YeG/ppSgbBu";

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);

        when(emailService.sendSimpleMail(any(Email.class))).thenReturn("Email sent successfully");
        when(userRepository.save(newUser)).thenReturn(newUser);


        User result = userService.registerUser(newUser);


        assertNotNull(result);
        assertEquals(email, result.getEmail());


        verify(emailService, times(1)).sendSimpleMail(any(Email.class));

        verify(userRepository, times(1)).save(newUser);

    }
    @Test
    void createUser_failSendingEmail() {
        String firstName = "Raghad";
        String lastName = "Matar";
        String email = "raghad@gmail.com";
        String password = "raghad123";
        String hashedPassword = "$2a$12$r.ErjHkDFTmjo90D8ijQdema66/GKPUY6ENfUNCI78YeG/ppSgbBu";

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);

        when(emailService.sendSimpleMail(any(Email.class))).thenThrow(new RuntimeException("Email Failed"));
        assertThrows(RuntimeException.class, () -> userService.registerUser(newUser));
        verify(emailService, times(1)).sendSimpleMail(any(Email.class));
        verify(userRepository, never()).save(any(User.class)); // should fail before saving

    }
    @Test
    void findUserByEmail_successful() {
        String firstName = "Raghad";
        String lastName = "Matar";
        String email = "raghad@gmail.com";
        String password = "raghad123";


        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);


        when(userRepository.findUserByEmail("raghad@gmail.com")).thenReturn(newUser);
        User result = userService.findUserByEmail("raghad@gmail.com");
        System.out.println(result);
        assertNotNull(result);
        assertEquals(email, result.getEmail());

        verify(userRepository, times(1)).findUserByEmail(email);


    }
    @Test
    void findUserByEmail_notFound() {
        String firstName = "Raghad";
        String lastName = "Matar";
        String email = "raghad@gmail.com";
        String password = "raghad123";
        String hashedPassword = "$2a$12$r.ErjHkDFTmjo90D8ijQdema66/GKPUY6ENfUNCI78YeG/ppSgbBu";

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);

        User result = userService.findUserByEmail("raghad1@gmail.com");
        assertNull(result);

    }
    //Reset password
    @Test
    void resetPassword_successful() {
        String firstName = "Raghad";
        String lastName = "Matar";
        String email = "raghad@gmail.com";
        String password = "raghad123";
        User newUser = new User();
        newUser.setId(1);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);

        String oldPassword = "raghad123";
        String newPassword = "raghad12";
        PasswordResetForm form= new PasswordResetForm(oldPassword, newPassword);

        when(userRepository.findById(1)).thenReturn(Optional.of(newUser));
        when(bCryptPasswordEncoder.matches("raghad123", newUser.getPassword())).thenReturn(true);
        when(bCryptPasswordEncoder.encode("raghad12")).thenReturn("$2a$12$zn75a54CyDiwOhj8/F9sPu9A4QcyQRvxIsb3JWxRiYh/Lv8qWflm.");
        String result = userService.resetPassword(1,"raghad@gmail.com",form);

        assertEquals("Password reset successful", result);
        verify(userRepository, times(1)).save(newUser);

    }
    @Test
    void resetPassword_userNotFound() {
        String firstName = "Raghad";
        String lastName = "Matar";
        String email = "raghad@gmail.com";
        String password = "raghad123";
        User newUser = new User();
        newUser.setId(1);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);

        String oldPassword = "raghad123";
        String newPassword = "raghad12";
        PasswordResetForm form= new PasswordResetForm(oldPassword, newPassword);

        when(userRepository.findById(2)).thenReturn(Optional.empty());
        String result = userService.resetPassword(2,"raghad@gmail.com",form);

        assertEquals("User not found", result);
        verify(userRepository, times(0)).save(any(User.class));

    }
    @Test
    public void resetPassword_mismatchedEmails() {
        System.out.println("/////////////////////////in resetttt");
        String firstName = "Raghad";
        String lastName = "Matar";
        String email = "raghad@gmail.com";
        String password = "raghad123";
        User newUser = new User();
        newUser.setId(1);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);

        String oldPassword = "raghad123";
        String newPassword = "raghad12";
        PasswordResetForm form= new PasswordResetForm(oldPassword, newPassword);

        when(userRepository.findById(1)).thenReturn(Optional.of(newUser));
        String result = userService.resetPassword(1,"raghad1@gmail.com",form);
        System.out.println(result);
        assertEquals("Unauthorized user", result);
        verify(userRepository, never()).save(any());

    }
    @Test
    public void resetPassword_mismatchedOldPassword() {
        String firstName = "Raghad";
        String lastName = "Matar";
        String email = "raghad@gmail.com";
        String password = "raghad123";
        User newUser = new User();
        newUser.setId(1);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);

        String oldPassword = "raghad12";
        String newPassword = "raghad1";
        PasswordResetForm form= new PasswordResetForm(oldPassword, newPassword);

        when(userRepository.findById(1)).thenReturn(Optional.of(newUser));
        String result = userService.resetPassword(1,"raghad@gmail.com",form);
        System.out.println(result);
        assertEquals("The passwords do not match", result);
        verify(userRepository, never()).save(any());

    }


}
