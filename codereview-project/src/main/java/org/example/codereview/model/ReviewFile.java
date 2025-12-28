package org.example.codereview.model;

public class ReviewFile {
    private int id;
    private int reviewId;
    private String filename;
    private String content;
    private int revisionNumber = 1;
    private ReviewFileChangeType changeType = ReviewFileChangeType.MODIFIED;

    public ReviewFile() {
    }

    public ReviewFile(int reviewId, String filename, String content) {
        this.reviewId = reviewId;
        this.filename = filename;
        this.content = content;
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

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(int revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public ReviewFileChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ReviewFileChangeType changeType) {
        this.changeType = changeType;
    }

    @Override
    public String toString() {
        return "ReviewFile{" +
                "id=" + id +
                ", reviewId=" + reviewId +
                ", filename='" + filename + '\'' +
                '}';
    }
}

