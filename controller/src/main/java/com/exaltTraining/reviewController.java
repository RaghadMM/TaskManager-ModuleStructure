package com.exaltTraining;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/taskManager")
public class reviewController {

    @Autowired
    private JwtService jwtService;

    private ReviewService reviewService;
    private UserService userService;

    public reviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    //Adding review API
    @PostMapping("review/task/{taskId}")
    public String addReview(@PathVariable("taskId") int taskId, @RequestBody Review review, @RequestHeader ("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User reviewer = userService.findUserByEmail(email);
        Department.Type depType= reviewer.getDepartment().getType();
        if(depType != Department.Type.QA){
            return "You are not allowed to add reviews";
        }

        return reviewService.addReview(review, taskId, reviewer);


    }

    //Mark a task as checked API
    @PutMapping("/task/markAsChecked/{taskId}")
    public String markTaskAsChecked(@PathVariable int taskId, @RequestHeader ("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        User reviewer = userService.findUserByEmail(email);
        Department.Type depType= reviewer.getDepartment().getType();
        if(depType != Department.Type.QA){
            return "You are not allowed to review";
        }
        return reviewService.markATaskAsChecked(taskId, reviewer);
    }
}
