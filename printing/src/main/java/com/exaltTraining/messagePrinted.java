package com.exaltTraining;



import java.time.LocalDateTime;

public class messagePrinted {

    private UserPrinted sender;
    private String message;
    private LocalDateTime sendingTime;
    private Boolean isRead;

    public messagePrinted(UserPrinted sender, String message, LocalDateTime sendingTime, Boolean isRead) {
        this.sender = sender;
        this.message = message;
        this.sendingTime = sendingTime;
        this.isRead = isRead;
    }




    public UserPrinted getSender() {
        return sender;
    }

    public void setSender(UserPrinted sender) {
        this.sender = sender;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSendingTime() {
        return sendingTime;
    }

    public void setSendingTime(LocalDateTime sendingTime) {
        this.sendingTime = sendingTime;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }
}
