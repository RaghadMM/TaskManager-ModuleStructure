package com.exaltTraining;



import java.util.List;

public interface MessageService {
    String sendMessage(Message message, User sender , int receiverId);
    List<Message> getAChat(User sender,int receiverId);
    String deleteMessage(int messageId, User user);
    String deleteAChat(int userId, User currentUser);
    List<Message> getUnreadMessages(User user);
    String updateMessage( User sender, int messageId, String newContent);

}
