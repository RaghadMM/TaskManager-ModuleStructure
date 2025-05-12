package com.exaltTraining;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/taskManager")
public class notificationController {

    @Autowired
    private JwtService jwtService;
    private NotificationService notificationService;
    private UserService userService;

    public notificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    //Get notifications API
    @GetMapping("/notifications/{required}")
    public List<notificationPrinted> getNotifications(@PathVariable String required, @RequestHeader ("Authorization") String authHeader) {
        // Extract the token
        String token = authHeader.substring(7); // Remove "Bearer "

        // Extract role from token using JwtService
        String email=jwtService.extractUsername(token);
        User user= userService.findUserByEmail(email);

        List< Notification> notificationList = notificationService.getNotifications(user, required);
        return (printNotifications(notificationList));
    }

    //Mark a notification as read
    @PutMapping("notification/{notificationId}")
    public String markAsRead(@PathVariable int notificationId, @RequestHeader ("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email=jwtService.extractUsername(token);
        User user= userService.findUserByEmail(email);
        return notificationService.markAsRead(notificationId, user);
    }

    // A helper function to form the list of notifications returned
    private List<notificationPrinted> printNotifications (List <Notification> notifications) {
        List<notificationPrinted> notificationPrinteds = notifications.stream()
                .map(notification -> new notificationPrinted(
                        notification.getTitle(),
                        notification.getBody(),
                        notification.isRead()
                ))
                .collect(Collectors.toList());
        return notificationPrinteds;

    }
}
