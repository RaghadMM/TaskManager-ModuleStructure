package com.exaltTraining;

import java.time.LocalDateTime;

public class MeetingPrinted {
    private UserPrinted organizer;
    private String Title;
    private String Description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public MeetingPrinted(UserPrinted organizer, String title, String description, LocalDateTime startTime, LocalDateTime endTime) {
        this.organizer = organizer;
        Title = title;
        Description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UserPrinted getOrganizer() {
        return organizer;
    }

    public void setOrganizer(UserPrinted organizer) {
        this.organizer = organizer;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
