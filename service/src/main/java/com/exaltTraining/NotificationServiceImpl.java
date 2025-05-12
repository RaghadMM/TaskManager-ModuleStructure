package com.exaltTraining;


import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private NotificationRepository notificationRepository;


    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    //Get a user notifications either all or unread
    @Override
    public List<Notification> getNotifications(User user, String required) {
        List<Notification> notifications = user.getNotifications();
        List<Notification> unreadNotifications = new ArrayList<>();
        if(required.equalsIgnoreCase("all")) {
            return user.getNotifications();
        } else if (required.equalsIgnoreCase("unread")) {
            for (Notification notification : notifications) {
                if(!notification.isRead()) {
                    unreadNotifications.add(notification);
                }
            }

        }
        return unreadNotifications;
    }

    //Mark a notification as read
    @Override
    public String markAsRead(int notificationId, User user) {
        Optional<Notification> notification=notificationRepository.findById(notificationId);
        if(notification.isPresent()) {
           Notification notify = notification.get();
           if(notify.getUser().equals(user)) {
               notify.setRead(true);
               notificationRepository.save(notify);
               return "Notification Marked as Read";
           }
           else{
               return "This notification is not assigned to the user.";
           }

        }
        return "Notification not found.";
    }
}
