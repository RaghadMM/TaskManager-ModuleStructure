package com.exaltTraining;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/taskManager")
public class meetingController {

    @Autowired
    private JwtService jwtService;
    private MeetingService meetingService;
    private UserService userService;


    public meetingController(MeetingService meetingService, UserService userService) {
        this.meetingService = meetingService;
        this.userService = userService;
    }

    @PostMapping("/meeting")
    public String createMeeting(@RequestBody MeetingRequest meeting, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        String role= jwtService.extractUserRole(token);
        User user= userService.findUserByEmail(email);
        if("employee".equals(role)) {
            return "Create meetings is not allowed for users";
        }
        return meetingService.createMeeting(meeting,user);
    }

    @GetMapping("/meetings")
    public List<MeetingPrinted> getMeetings(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User user= userService.findUserByEmail(email);
        List<Meeting> meetings = meetingService.getMeetings(user);
        return printMeeting(meetings);
    }

    @DeleteMapping("/meeting/{meetingId}")
    public String deleteMeeting(@PathVariable int meetingId, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User user= userService.findUserByEmail(email);
        return meetingService.deleteMeeting(meetingId,user);
    }

    @PutMapping("/meeting/{meetingId}")
    public String updateMeeting(@PathVariable int meetingId,@RequestBody MeetingRequest meeting, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User user= userService.findUserByEmail(email);

        return meetingService.updateMeeting(meeting,user,meetingId);
    }

    @GetMapping("/meetings/search")
    public List<MeetingPrinted> searchMeetings(@RequestParam("query") String query,@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User user= userService.findUserByEmail(email);

        List <Meeting> meetings= meetingService.searchMeetings(query,user);
        return printMeeting(meetings);

    }

    // A helper function to form the list of meetings returned
    private List<MeetingPrinted> printMeeting (List <Meeting> meetings) {
        List<MeetingPrinted> meetingPrinted = meetings.stream()
                .map(meeting -> new MeetingPrinted(
                        new UserPrinted(
                                meeting.getOrganizer().getFirstName(),
                                meeting.getOrganizer().getLastName()
                        ),
                        meeting.getTitle(),
                        meeting.getDescription(),
                        meeting.getStartDate(),
                        meeting.getEndDate()

                ))
                .collect(Collectors.toList());
        return meetingPrinted;
    }
}
