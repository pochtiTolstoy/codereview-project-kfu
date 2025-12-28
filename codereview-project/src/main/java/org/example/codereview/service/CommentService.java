package org.example.codereview.service;

import org.example.codereview.dao.CommentDAO;
import org.example.codereview.dao.ReviewDAO;
import org.example.codereview.dao.ProjectDAO;
import org.example.codereview.dao.ReviewReviewerDAO;
import org.example.codereview.dao.ReviewFileDAO;
import org.example.codereview.model.Comment;
import org.example.codereview.model.CodeReview;
import org.example.codereview.model.Project;
import org.example.codereview.model.ReviewFile;
import org.example.codereview.model.User;

import java.sql.SQLException;
import java.util.List;

public class CommentService {

    private final CommentDAO commentDAO = new CommentDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ReviewFileDAO reviewFileDAO = new ReviewFileDAO();
    private final ReviewReviewerDAO reviewReviewerDAO = new ReviewReviewerDAO();

    /**
     * Добавление комментария.
     *
     * @param currentUser  текущий пользователь (обязательно не null)
     * @param reviewId     ID ревью
     * @param reviewFileId ID файла ревью (может быть null для общего комментария)
     * @param lineNumber   номер строки (может быть null)
     * @param content      текст комментария
     */
    public int addComment(User currentUser,
                          int reviewId,
                          Integer reviewFileId,
                          Integer lineNumber,
                          String content,
                          Integer parentCommentId) throws SQLException {
        if (currentUser == null) {
            throw new SecurityException("User must be logged in to add comments");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Комментарий не может быть пустым");
        }
        String trimmed = content.trim();
        if (trimmed.length() > 5000) {
            throw new IllegalArgumentException("Комментарий слишком длинный (максимум 5000 символов)");
        }
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }

        CodeReview review = reviewDAO.findById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review not found");
        }

        Project project = projectDAO.findById(review.getProjectId());
        if (project == null) {
            throw new IllegalArgumentException("Project not found for this review");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        boolean isMember = projectDAO.isMember(currentUser.getId(), project.getId());

        if (!isAdmin && !isMember) {
            throw new SecurityException("You are not allowed to comment on this review");
        }

        ReviewFile reviewFile = null;
        if (reviewFileId != null) {
            reviewFile = reviewFileDAO.findById(reviewFileId);
            if (reviewFile == null || reviewFile.getReviewId() != reviewId) {
                throw new IllegalArgumentException("Review file does not belong to this review");
            }
        }

        if (lineNumber != null && lineNumber <= 0) {
            throw new IllegalArgumentException("Номер строки должен быть положительным");
        }

        Comment parentComment = null;
        if (parentCommentId != null) {
            parentComment = commentDAO.findById(parentCommentId);
            if (parentComment == null) {
                throw new IllegalArgumentException("Parent comment not found");
            }
            if (parentComment.getReviewId() != reviewId) {
                throw new IllegalArgumentException("Parent comment belongs to another review");
            }
        }

        int commentRevision = review.getCurrentRevisionNumber();

        Comment comment = new Comment();
        comment.setReviewId(reviewId);
        comment.setAuthorId(currentUser.getId());
        comment.setContent(trimmed);

        if (parentComment != null) {
            comment.setParentId(parentCommentId);
            comment.setReviewFileId(parentComment.getReviewFileId());
            comment.setLineNumber(parentComment.getLineNumber());
            comment.setResolved(false);
            commentRevision = parentComment.getRevisionNumber();
        } else if (reviewFile != null) {
            comment.setReviewFileId(reviewFileId);
            comment.setLineNumber(lineNumber);
            comment.setResolved(false);
        } else {
            comment.setReviewFileId(null);
            comment.setLineNumber(null);
            comment.setResolved(false);
        }
        comment.setRevisionNumber(commentRevision);

        int id = commentDAO.create(comment);
        comment.setId(id);
        return id;
    }

    public List<Comment> getCommentsForReview(int reviewId) throws SQLException {
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        return commentDAO.findByReviewId(reviewId);
    }

    public void deleteComment(User currentUser, int commentId) throws SQLException {
        if (currentUser == null) {
            throw new SecurityException("User must be logged in to delete comments");
        }
        if (commentId <= 0) {
            throw new IllegalArgumentException("Invalid comment ID");
        }

        Comment comment = commentDAO.findById(commentId);
        if (comment == null) {
            return;
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        boolean isAuthor = (comment.getAuthorId() == currentUser.getId());

        if (!isAdmin && !isAuthor) {
            throw new SecurityException("You are not allowed to delete this comment");
        }

        commentDAO.delete(commentId);
    }

    public void resolveThread(User currentUser, int rootCommentId) throws SQLException {
        toggleThreadResolved(currentUser, rootCommentId, true);
    }

    public void reopenThread(User currentUser, int rootCommentId) throws SQLException {
        toggleThreadResolved(currentUser, rootCommentId, false);
    }

    private void toggleThreadResolved(User currentUser, int rootCommentId, boolean resolve) throws SQLException {
        if (currentUser == null) {
            throw new SecurityException("User must be logged in to change comment status");
        }
        Comment root = commentDAO.findById(rootCommentId);
        if (root == null) {
            throw new IllegalArgumentException("Комментарий не найден");
        }
        if (!root.isRoot()) {
            throw new IllegalArgumentException("Только корневые комментарии можно закрывать");
        }

        CodeReview review = reviewDAO.findById(root.getReviewId());
        if (review == null) {
            throw new IllegalArgumentException("Review not found");
        }
        Project project = projectDAO.findById(review.getProjectId());
        if (project == null) {
            throw new IllegalArgumentException("Project not found for this review");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        boolean isThreadAuthor = root.getAuthorId() == currentUser.getId();
        boolean isAssignedReviewer = reviewReviewerDAO.exists(review.getId(), currentUser.getId());

        if (!isAdmin && !isThreadAuthor && !isAssignedReviewer) {
            throw new SecurityException("Нет прав для изменения статуса комментария");
        }

        commentDAO.updateResolved(rootCommentId, resolve, resolve ? currentUser.getId() : null);
    }
}
