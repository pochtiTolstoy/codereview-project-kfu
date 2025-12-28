package org.example.codereview.model;

import java.util.ArrayList;
import java.util.List;

public class CommentThread {
    private Comment root;
    private final List<Comment> replies = new ArrayList<>();
    private boolean outdated;
    private boolean canResolve;

    public Comment getRoot() {
        return root;
    }

    public void setRoot(Comment root) {
        this.root = root;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    public void addReply(Comment reply) {
        this.replies.add(reply);
    }

    public boolean isResolved() {
        return root != null && root.isResolved();
    }

    public boolean isOutdated() {
        return outdated;
    }

    public void setOutdated(boolean outdated) {
        this.outdated = outdated;
    }

    public boolean isCanResolve() {
        return canResolve;
    }

    public void setCanResolve(boolean canResolve) {
        this.canResolve = canResolve;
    }
}

