package com.exaltTraining;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/taskManager")
public class messageController {

    @Autowired
    private JwtService jwtService;
    private MessageService messageService;
    private UserService userService;


    public messageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    //Send a message API
    @PostMapping("/message/{receiverId}")
    public String sendMessage(@PathVariable("receiverId") int receiverId, @RequestBody Message message,@RequestHeader ("Authorization") String authHeader ) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User sender = userService.findUserByEmail(email);

        return messageService.sendMessage(message,sender,receiverId);
    }

    //Get a chat between two users API
    @GetMapping("/chat/{userId}")
    public List<messagePrinted> getChat(@PathVariable("userId") int userId, @RequestHeader ("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User sender = userService.findUserByEmail(email);

        List <Message> chat= messageService.getAChat(sender,userId);
        return printMessages(chat);

    }

    //Delete a message API
    @DeleteMapping("message/{messageId}")
    public String deleteMessage(@PathVariable("messageId") int messageId, @RequestHeader ("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User user = userService.findUserByEmail(email);
        return messageService.deleteMessage(messageId,user);

    }

    //Delete a chat API
    @DeleteMapping("/chat/{userId}")
    public String deleteChat(@PathVariable("userId") int userId, @RequestHeader ("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User user = userService.findUserByEmail(email);
        return messageService.deleteAChat(userId,user);
    }

    //Get unread messages API
    @GetMapping("/messages/unread")
    public List<messagePrinted> getMessages(@RequestHeader ("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User sender = userService.findUserByEmail(email);
        List <Message> unread = messageService.getUnreadMessages(sender);
        return printMessages(unread);
    }

    //Edit a message API
    @PutMapping("/message/{messageId}")
    public String updateMessage(@PathVariable("messageId") int messageId, @RequestBody Map<String, String> payload, @RequestHeader ("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User user = userService.findUserByEmail(email);
        String newContent = payload.get("newContent");
        return messageService.updateMessage(user,messageId,newContent);

    }

    // A helper function to form the list of messages returned
    private List<messagePrinted> printMessages (List <Message> messages) {
        List<messagePrinted> messagePrinted = messages.stream()
                .map(message -> new messagePrinted(
                        new UserPrinted(
                                message.getSender().getFirstName(),
                                message.getSender().getLastName()
                        ),
                        message.getContent(),
                        message.getSendingTime(),
                        message.getRead()

                ))
                .collect(Collectors.toList());
        return messagePrinted;

    }

}
