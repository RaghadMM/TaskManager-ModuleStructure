package com.exaltTraining;


import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MeetingServiceImpl implements MeetingService {

    private MeetingRepository meetingRepository;
    private UserRepository userRepository;
    private NotificationRepository notificationRepository;

    public MeetingServiceImpl(MeetingRepository meetingRepository, UserRepository userRepository, NotificationRepository notificationRepository) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public String createMeeting(MeetingRequest meeting, User user) {
        try{

            List<User> participants= userRepository.findAllById(meeting.getParticipantIds());
            Meeting theMeeting = new Meeting();
            theMeeting.setTitle(meeting.getTitle());
            theMeeting.setDescription(meeting.getDescription());
            theMeeting.setStartDate(meeting.getStartTime());
            theMeeting.setEndDate(meeting.getEndTime());
            theMeeting.setOrganizer(user);
            theMeeting.setParticipants(participants);
            meetingRepository.save(theMeeting);

            String message = "You have been invited to a meeting titled '" + meeting.getTitle() +
                    "' scheduled by " + user.getFirstName() + " " + user.getLastName() +
                    " (" + user.getRole().toString().toLowerCase() + ").";

            for(User u: participants){
                notificationRepository.save(new Notification("New Scheduled meeting", message, false, u));
            }
            return "Meeting created";
        }
        catch(Exception e){
            e.printStackTrace();
            return "Meeting not created";
        }

    }

    @Override
    public List<Meeting> getMeetings(User user) {
        List<Meeting> meetings = user.getMeetings();
        meetings.sort(Comparator.comparing(Meeting::getStartDate));
        return meetings;
    }

    @Override
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void checkUpcomingMeeting() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime oneDayLater = currentTime.plusDays(1);

        List<Meeting> meetings = meetingRepository.findAll();

        for (Meeting meeting : meetings) {
            LocalDateTime startDate = meeting.getStartDate();
            if (startDate != null && startDate.isAfter(currentTime) && startDate.isBefore(oneDayLater)) {
                List<User> participants = meeting.getParticipants();
                for (User u : participants) {
                    List<Notification> sent = notificationRepository.findByMeetingTitleAndUser(meeting.getTitle(), u);

                    if (sent.isEmpty()) {
                        String formattedTime = meeting.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        String body = "Reminder: You have a scheduled meeting titled '" + meeting.getTitle() +
                                "' at " + formattedTime + ". Please make sure to attend.";

                        notificationRepository.save(new Notification(
                                "Meeting Reminder",
                                body,
                                false,
                                u
                        ));
                    }
                }
            }
        }
    }

    @Transactional
    @Override
    public String deleteMeeting(int meetingId, User user) {
        Optional<Meeting> meeting = meetingRepository.findById(meetingId);
        if (meeting.isPresent()) {
            Meeting theMeeting = meeting.get();
            if(theMeeting.getOrganizer().equals(user)){
                meetingRepository.delete(theMeeting);
                List<User> participants = theMeeting.getParticipants();
                String message = "The meeting titled  '" + theMeeting.getTitle() +
                        "' scheduled at " + theMeeting.getStartDate() + " -  " + theMeeting.getEndDate() +
                        " was cancelled by the organizer ";
                for (User u : participants) {
                    notificationRepository.save(new Notification("Cancelled Meeting", message, false, u));
                }
                return "Meeting cancelled";
            }
            else{
                return "This meeting is not organized by this user";
            }

        }
        return "Meeting not found";
    }

    @Override
    public String updateMeeting(MeetingRequest meeting, User user, int meetingId) {
        Optional<Meeting> meetingOptional = meetingRepository.findById(meetingId);
        if (meetingOptional.isPresent()) {
            Meeting theMeeting = meetingOptional.get();
            if(theMeeting.getOrganizer().equals(user)){
                if(meeting.getTitle() != null){
                    theMeeting.setTitle(meeting.getTitle());
                }
                if(meeting.getDescription() != null){
                    theMeeting.setDescription(meeting.getDescription());
                }
                if(meeting.getStartTime() != null){
                    theMeeting.setStartDate(meeting.getStartTime());
                }
                if(meeting.getEndTime() != null){
                    theMeeting.setEndDate(meeting.getEndTime());
                }
                meetingRepository.save(theMeeting);
                List<User> participants = theMeeting.getParticipants();
                String message = "Meeting '" + theMeeting.getTitle() + "' was updated.";
                for (User u : participants) {
                    notificationRepository.save(new Notification("Meeting Updated", message, false, u));
                }
                return "Meeting updated";
            }
            else{
                return "This meeting is not organized by this user";
            }
        }
        return "Meeting not found";
    }

    @Override
    public List<Meeting> searchMeetings(String query, User user) {
        return meetingRepository.searchMeetings(user, query);
    }

}



