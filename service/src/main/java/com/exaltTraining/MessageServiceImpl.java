package com.exaltTraining;


import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MessageServiceImpl implements MessageService {

    private MessageRepository messageRepository;
    private UserRepository userRepository;

    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    //Send a message to a user
    @Override
    public String sendMessage(Message message, User sender, int receiverId) {
        Optional<User> receiver = userRepository.findById(receiverId);
        if (receiver.isPresent()) {
            try{
                User theReceiver = receiver.get();
                message.setSender(sender);
                message.setRecipient(theReceiver);
                message.setSendingTime(LocalDateTime.now());
                message.setRead(false);
                messageRepository.save(message);
                return "Message sent";
            }
            catch (Exception e){
                e.printStackTrace();
                return "Message not sent";
            }
        }
        return "Recipient not found";
    }

    //Get a conversation between two users
    //Mark the messages form the other user as read
    @Override
    public List<Message> getAChat(User sender, int receiverId) {
        Optional<User> receiver = userRepository.findById(receiverId);
        if (receiver.isPresent()) {
            List<Message> chat= messageRepository.getChatBetweenUsers(sender,receiver.get());
            for (Message m : chat) {
                if(m.getRecipient().equals(sender)){
                    m.setRead(true);
                    messageRepository.save(m);
                }

            }
            return chat;
        }
        return null;
    }

    //Delete a message
    //Check for message existing, belonging to the user
    @Override
    public String deleteMessage(int messageId, User user) {
        Optional<Message> message = messageRepository.findById(messageId);
        if (message.isPresent()) {
            Message theMessage = message.get();
            if(theMessage.getSender().equals(user)){
                messageRepository.delete(theMessage);
                return "Message deleted";
            }
            else{
                return "This message does not belong to you";
            }
        }
        return "Message not found";
    }
//Delete a whole conversation by the sender or the receiver
    @Override
    public String deleteAChat(int userId, User currentUser) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User otherSideUser = user.get();
            List<Message> chat = messageRepository.getChatBetweenUsers(otherSideUser,currentUser);
            if(chat.isEmpty()){
                return "No chat found";
            }
            messageRepository.deleteAll(chat);
            return "Chat deleted";
        }
        return "User not found";
    }

    //Get user unread messages
    @Override
    public List<Message> getUnreadMessages(User user) {
        List<Message> messages = user.getReceivedMessages();
        List<Message> unreadMessages = new ArrayList<>();
        for (Message m : messages) {
            if (!m.getRead()) {
                unreadMessages.add(m);
            }
        }
        return unreadMessages;
    }

    //Edit a message by its sender
    @Override
    public String updateMessage(User sender, int messageId, String newContent) {
        Optional<Message> message = messageRepository.findById(messageId);
        if (message.isPresent()) {
            Message theMessage = message.get();
            if(theMessage.getSender().equals(sender)){
                theMessage.setContent(newContent);
                messageRepository.save(theMessage);
                return "Message updated";
            }
            else {
                return "This message does not belong to you";
            }
        }
        return "Message not found";
    }


}
