package org.example.codereview.model;

public class CommentLineInfo {
    private boolean hasAny;
    private boolean hasUnresolved;
    private Integer firstThreadId;

    public CommentLineInfo(boolean hasAny, boolean hasUnresolved, Integer firstThreadId) {
        this.hasAny = hasAny;
        this.hasUnresolved = hasUnresolved;
        this.firstThreadId = firstThreadId;
    }

    public boolean isHasAny() {
        return hasAny;
    }

    public boolean isHasUnresolved() {
        return hasUnresolved;
    }

    public Integer getFirstThreadId() {
        return firstThreadId;
    }
}

