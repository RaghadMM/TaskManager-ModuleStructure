package com.exaltTraining;



import java.util.List;

public interface NotificationService {

    List<Notification> getNotifications(User user, String required);
    String markAsRead(int notificationId, User user);
}
