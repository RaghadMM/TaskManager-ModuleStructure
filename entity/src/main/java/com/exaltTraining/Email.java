package com.exaltTraining;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class Email {
    private String recipient;
    private String msgBody;
    private String subject;

    public Email(String recipient, String msgBody, String subject) {
        this.recipient = recipient;
        this.msgBody = msgBody;
        this.subject = subject;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public String getSubject() {
        return subject;
    }
}
