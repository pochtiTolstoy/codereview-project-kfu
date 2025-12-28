package org.example.codereview.model;

import java.sql.Timestamp;

public class ProjectFile {
    private int id;
    private int projectId;
    private String filename;
    private String content;
    private Integer lastReviewId;
    private Timestamp lastUpdatedAt;

    public ProjectFile() {
    }

    public ProjectFile(int projectId, String filename, String content) {
        this.projectId = projectId;
        this.filename = filename;
        this.content = content;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLastReviewId() {
        return lastReviewId;
    }

    public void setLastReviewId(Integer lastReviewId) {
        this.lastReviewId = lastReviewId;
    }

    public Timestamp getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Timestamp lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}

