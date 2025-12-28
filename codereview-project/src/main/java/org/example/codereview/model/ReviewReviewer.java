package org.example.codereview.model;

import java.time.LocalDateTime;

public class ReviewReviewer {
    private int id;
    private int reviewId;
    private int userId;
    private ReviewParticipantRole role = ReviewParticipantRole.REVIEWER;
    private LocalDateTime addedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public ReviewParticipantRole getRole() {
        return role;
    }

    public void setRole(ReviewParticipantRole role) {
        this.role = role;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}

