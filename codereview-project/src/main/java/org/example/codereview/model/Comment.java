package org.example.codereview.model;

import java.sql.Timestamp;

public class Comment {
    private int id;
    private int reviewId;
    private int authorId;
    private Integer reviewFileId;
    private Integer lineNumber;
    private String content;
    private Timestamp createdAt;
    private Integer parentId;
    private boolean resolved;
    private Integer resolvedBy;
    private Timestamp resolvedAt;
    private int revisionNumber = 1;
    private String reviewFileName;

    public Comment() {
    }

    public Comment(int reviewId, int authorId, String content) {
        this.reviewId = reviewId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

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

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getReviewFileId() {
        return reviewFileId;
    }

    public void setReviewFileId(Integer reviewFileId) {
        this.reviewFileId = reviewFileId;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public boolean isRoot() {
        return parentId == null;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public Integer getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Integer resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public Timestamp getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Timestamp resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(int revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public String getReviewFileName() {
        return reviewFileName;
    }

    public void setReviewFileName(String reviewFileName) {
        this.reviewFileName = reviewFileName;
    }
}
