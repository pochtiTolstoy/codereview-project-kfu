package org.example.codereview.model;

import java.sql.Timestamp;

public class CodeReview {
    private int id;
    private int projectId;
    private int authorId;
    private String title;
    private String content;
    private ReviewStatus status = ReviewStatus.WIP;
    private Timestamp createdAt;
    private int currentRevisionNumber = 1;
    private int lockedRevisionNumber = 0;
    
    public CodeReview() {
    }

    public CodeReview(int projectId, int authorId, String title, String content) {
        this.projectId = projectId;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.status = ReviewStatus.WIP;
        this.currentRevisionNumber = 1;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = ReviewStatus.fromDatabase(status);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getCurrentRevisionNumber() {
        return currentRevisionNumber;
    }

    public void setCurrentRevisionNumber(int currentRevisionNumber) {
        this.currentRevisionNumber = currentRevisionNumber;
    }

    public int getLockedRevisionNumber() {
        return lockedRevisionNumber;
    }

    public void setLockedRevisionNumber(int lockedRevisionNumber) {
        this.lockedRevisionNumber = lockedRevisionNumber;
    }

    @Override
    public String toString() {
        return "CodeReview{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", authorId=" + authorId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

