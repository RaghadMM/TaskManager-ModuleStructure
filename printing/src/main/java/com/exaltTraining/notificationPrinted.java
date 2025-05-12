package com.exaltTraining;

public class notificationPrinted {
    private String title;
    private String body;
    private Boolean isRead;

    public notificationPrinted(String title, String body, Boolean isRead) {
        this.title = title;
        this.body = body;
        this.isRead = isRead;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }
}
