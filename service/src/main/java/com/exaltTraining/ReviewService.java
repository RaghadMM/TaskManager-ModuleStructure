package com.exaltTraining;



public interface ReviewService {
    String addReview(Review review, int taskId, User reviewer);
    String markATaskAsChecked( int taskId, User reviewer);

}
