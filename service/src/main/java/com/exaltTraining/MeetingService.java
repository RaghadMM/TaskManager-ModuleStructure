package com.exaltTraining;


import java.util.List;

public interface MeetingService {

    String createMeeting(MeetingRequest meeting, User user);
    List<Meeting> getMeetings(User user);
    void checkUpcomingMeeting();
    String deleteMeeting(int meetingId, User user);
    String updateMeeting(MeetingRequest meeting, User user, int meetingId);
    List<Meeting> searchMeetings(String query, User user);
}
