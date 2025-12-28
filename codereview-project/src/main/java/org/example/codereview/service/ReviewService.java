package org.example.codereview.service;

import org.example.codereview.dao.CommentDAO;
import org.example.codereview.dao.ProjectDAO;
import org.example.codereview.dao.ProjectFileDAO;
import org.example.codereview.dao.ReviewDAO;
import org.example.codereview.dao.ReviewFileDAO;
import org.example.codereview.dao.ReviewReviewerDAO;
import org.example.codereview.dao.ReviewVoteDAO;
import org.example.codereview.dao.UserDAO;
import org.example.codereview.model.CodeReview;
import org.example.codereview.model.Project;
import org.example.codereview.model.ProjectFile;
import org.example.codereview.model.ReviewFile;
import org.example.codereview.model.ReviewFileChangeType;
import org.example.codereview.model.ReviewParticipantRole;
import org.example.codereview.model.ReviewReviewer;
import org.example.codereview.model.ReviewStatus;
import org.example.codereview.model.ReviewVote;
import org.example.codereview.model.ReviewVoteLabel;
import org.example.codereview.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReviewService {
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ReviewFileDAO reviewFileDAO = new ReviewFileDAO();
    private final CommentDAO commentDAO = new CommentDAO();
    private final ProjectFileDAO projectFileDAO = new ProjectFileDAO();
    private final ReviewReviewerDAO reviewReviewerDAO = new ReviewReviewerDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ReviewVoteDAO reviewVoteDAO = new ReviewVoteDAO();

    public int createReview(int authorId, int projectId, String title, String content) throws SQLException {
        User author = userDAO.findById(authorId);
        boolean isAdmin = author != null && "ADMIN".equalsIgnoreCase(author.getRole());
        if (!isAdmin && !projectDAO.isMember(authorId, projectId)) {
            throw new RuntimeException("User is not a member of this project");
        }

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Review title cannot be empty");
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException("Review title is too long (max 255 characters)");
        }
        if (authorId <= 0) {
            throw new IllegalArgumentException("Invalid author ID");
        }
        if (projectId <= 0) {
            throw new IllegalArgumentException("Invalid project ID");
        }

        CodeReview review = new CodeReview(projectId, authorId, title.trim(),
                                          content != null ? content.trim() : null);

        int reviewId = reviewDAO.create(review);

        List<ProjectFile> baseFiles = projectFileDAO.findByProjectId(projectId);
        for (ProjectFile pf : baseFiles) {
            ReviewFile snapshot = new ReviewFile();
            snapshot.setReviewId(reviewId);
            snapshot.setFilename(pf.getFilename());
            String normalizedSnapshot = pf.getContent().replace("\r\n", "\n").replace("\r", "\n");
            snapshot.setContent(normalizedSnapshot);
            snapshot.setRevisionNumber(0);
            snapshot.setChangeType(ReviewFileChangeType.MODIFIED);
            reviewFileDAO.create(snapshot);
        }

        return reviewId;
    }

    public CodeReview getReview(int reviewId) throws SQLException {
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        return reviewDAO.findById(reviewId);
    }

    public List<CodeReview> getReviewsByProject(int projectId) throws SQLException {
        if (projectId <= 0) {
            throw new IllegalArgumentException("Invalid project ID");
        }
        return reviewDAO.findByProjectId(projectId);
    }

    public User getUserById(int userId) throws SQLException {
        if (userId <= 0) {
            return null;
        }
        return userDAO.findById(userId);
    }

    public List<CodeReview> getReviewsByAuthor(int authorId) throws SQLException {
        if (authorId <= 0) {
            throw new IllegalArgumentException("Invalid author ID");
        }
        return reviewDAO.findByAuthorId(authorId);
    }

    public List<CodeReview> getAllReviews() throws SQLException {
        return reviewDAO.findAll();
    }

    public List<ReviewVote> getVotesForReview(int reviewId) throws SQLException {
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        return reviewVoteDAO.findByReviewId(reviewId);
    }

    public List<User> getReviewers(int reviewId) throws SQLException {
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        List<ReviewReviewer> assignments = reviewReviewerDAO.findByReviewId(reviewId);
        List<User> reviewers = new ArrayList<>();
        for (ReviewReviewer assignment : assignments) {
            User reviewer = userDAO.findById(assignment.getUserId());
            if (reviewer != null) {
                reviewers.add(reviewer);
            }
        }
        return reviewers;
    }

    public void addReviewer(User actor, int reviewId, String usernameOrEmail) throws SQLException {
        CodeReview review = requireReview(reviewId);
        ensureCanManageReviewers(actor, review);

        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Укажите логин или email ревьюера");
        }

        User targetUser = userDAO.findByUsernameOrEmail(usernameOrEmail.trim());
        if (targetUser == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        if (!projectDAO.isMember(targetUser.getId(), review.getProjectId())) {
            throw new IllegalArgumentException("Пользователь не состоит в проекте");
        }

        if (targetUser.getId() == review.getAuthorId()) {
            throw new IllegalArgumentException("Нельзя назначить автора ревьюером");
        }

        reviewReviewerDAO.addReviewer(reviewId, targetUser.getId(), ReviewParticipantRole.REVIEWER);
    }

    public void removeReviewer(User actor, int reviewId, int reviewerUserId) throws SQLException {
        CodeReview review = requireReview(reviewId);
        ensureCanManageReviewers(actor, review);
        reviewReviewerDAO.removeReviewer(reviewId, reviewerUserId);
    }

    public boolean isReviewer(int reviewId, int userId) throws SQLException {
        if (reviewId <= 0 || userId <= 0) {
            return false;
        }
        return reviewReviewerDAO.exists(reviewId, userId);
    }

    public void updateReview(CodeReview review) throws SQLException {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null");
        }
        if (review.getId() <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        if (review.getTitle() == null || review.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Review title cannot be empty");
        }
        if (review.getTitle().length() > 255) {
            throw new IllegalArgumentException("Review title is too long (max 255 characters)");
        }

        review.setTitle(review.getTitle().trim());
        if (review.getContent() != null) {
            review.setContent(review.getContent().trim());
        }

        reviewDAO.update(review);
    }

    public void readyForReview(User actor, int reviewId) throws SQLException {
        CodeReview review = requireReview(reviewId);
        ensureAuthor(actor, review);
        ensureStatus(review, ReviewStatus.WIP);
        transition(review, ReviewStatus.ACTIVE);
        lockCurrentRevision(review);
    }

    public void approveReview(User actor, int reviewId) throws SQLException {
        CodeReview review = requireReview(reviewId);
        ensureReviewer(actor, review);
        ensureNoUnresolvedComments(review);
        ensureReviewerHasNoOwnUnresolvedComments(reviewId, actor);
        reviewVoteDAO.upsertVote(reviewId, actor.getId(), ReviewVoteLabel.CODE_REVIEW, 1);
        if (!allReviewersApproved(reviewId)) {
            return;
        }
        if (review.getStatus() == ReviewStatus.APPROVED) {
            return;
        }
        ensureStatus(review, ReviewStatus.ACTIVE);
        transition(review, ReviewStatus.APPROVED);
    }

    public void requestChanges(User actor, int reviewId) throws SQLException {
        CodeReview review = requireReview(reviewId);
        ensureReviewer(actor, review);
        if (review.getStatus() != ReviewStatus.ACTIVE && review.getStatus() != ReviewStatus.APPROVED) {
            throw new IllegalStateException("Можно запросить правки только у активного ревью");
        }
        reviewVoteDAO.upsertVote(reviewId, actor.getId(), ReviewVoteLabel.CODE_REVIEW, -1);
        transition(review, ReviewStatus.CHANGES_REQUIRED);
    }

    public void markUpdated(User actor, int reviewId) throws SQLException {
        CodeReview review = requireReview(reviewId);
        ensureAuthor(actor, review);
        ensureStatus(review, ReviewStatus.CHANGES_REQUIRED);
        transition(review, ReviewStatus.ACTIVE);
        lockCurrentRevision(review);
    }

    public void closeReview(User actor, int reviewId) throws SQLException {
        CodeReview review = requireReview(reviewId);
        ensureCanClose(actor, review);
        ensureStatus(review, ReviewStatus.APPROVED);
        ensureNoUnresolvedComments(review);
        ensureAllReviewersApproved(reviewId);
        transition(review, ReviewStatus.CLOSED);
    }

    public void abandonReview(User actor, int reviewId) throws SQLException {
        CodeReview review = requireReview(reviewId);
        ensureCanClose(actor, review);
        if (review.getStatus().isTerminal()) {
            throw new IllegalStateException("Ревью уже завершено");
        }
        transition(review, ReviewStatus.ABANDONED);
    }

    public int startNewRevision(User actor, int reviewId) throws SQLException {
        CodeReview review = requireReview(reviewId);
        ensureAuthor(actor, review);
        int currentRevision = review.getCurrentRevisionNumber();
        int newRevision = currentRevision + 1;

        List<ReviewFile> previousFiles = reviewFileDAO.findByReviewAndRevision(reviewId, currentRevision);
        for (ReviewFile prev : previousFiles) {
            ReviewFile clone = new ReviewFile();
            clone.setReviewId(prev.getReviewId());
            clone.setFilename(prev.getFilename());
            clone.setContent(prev.getContent());
            clone.setRevisionNumber(newRevision);
            clone.setChangeType(prev.getChangeType());
            reviewFileDAO.create(clone);
        }

        reviewDAO.updateCurrentRevisionNumber(reviewId, newRevision);
        review.setCurrentRevisionNumber(newRevision);

        resetVotesAndReopenIfNeeded(review);
        return newRevision;
    }

    public void mergeReview(User actor, int reviewId) throws SQLException {
        CodeReview review = requireReview(reviewId);
        ensureCanClose(actor, review);
        ensureStatus(review, ReviewStatus.APPROVED);
        ensureNoUnresolvedComments(review);
        ensureAllReviewersApproved(reviewId);

        int currentRevision = review.getCurrentRevisionNumber();
        List<ReviewFile> files = reviewFileDAO.findByReviewAndRevision(reviewId, currentRevision);
        for (ReviewFile file : files) {
            ReviewFileChangeType type = file.getChangeType();
            if (type == ReviewFileChangeType.DELETED) {
                projectFileDAO.deleteByProjectAndFilename(review.getProjectId(), file.getFilename());
            } else {
                ProjectFile masterFile = new ProjectFile(review.getProjectId(), file.getFilename(), file.getContent());
                masterFile.setLastReviewId(reviewId);
                projectFileDAO.upsert(masterFile);
            }
        }

        transition(review, ReviewStatus.CLOSED);
    }

    private CodeReview requireReview(int reviewId) throws SQLException {
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        CodeReview review = reviewDAO.findById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review not found");
        }
        if (review.getCurrentRevisionNumber() <= 0) {
            review.setCurrentRevisionNumber(1);
        }
        return review;
    }

    private void ensureReviewer(User actor, CodeReview review) throws SQLException {
        if (actor == null) {
            throw new SecurityException("User must be authenticated");
        }
        if (review.getStatus().isTerminal()) {
            throw new IllegalStateException("Review already finished");
        }
        if (isAdmin(actor)) {
            return;
        }
        if (review.getAuthorId() == actor.getId()) {
            throw new SecurityException("Author cannot review own changes");
        }
        if (!reviewReviewerDAO.exists(review.getId(), actor.getId())) {
            throw new SecurityException("Вы не назначены ревьюером");
        }
    }

    private void ensureAuthor(User actor, CodeReview review) {
        if (actor == null || review.getAuthorId() != actor.getId()) {
            throw new SecurityException("Only review author can perform this action");
        }
    }

    private void ensureCanClose(User actor, CodeReview review) throws SQLException {
        if (actor == null) {
            throw new SecurityException("User must be authenticated");
        }
        if (isAdmin(actor)) {
            return;
        }
        if (review.getAuthorId() == actor.getId()) {
            return;
        }
        Project project = projectDAO.findById(review.getProjectId());
        if (project != null && project.getOwnerId() == actor.getId()) {
            return;
        }
        throw new SecurityException("Only author, project owner or admin can close review");
    }

    private void ensureCanManageReviewers(User actor, CodeReview review) throws SQLException {
        if (actor == null) {
            throw new SecurityException("User must be authenticated");
        }
        if (isAdmin(actor)) {
            return;
        }
        if (review.getAuthorId() == actor.getId()) {
            return;
        }
        Project project = projectDAO.findById(review.getProjectId());
        if (project != null && project.getOwnerId() == actor.getId()) {
            return;
        }
        throw new SecurityException("Недостаточно прав для управления ревьюерами");
    }

    private void ensureStatus(CodeReview review, ReviewStatus expected) {
        if (review.getStatus() != expected) {
            throw new IllegalStateException("Ожидается статус " + expected + ", но текущий: " + review.getStatus());
        }
    }

    private void ensureNoUnresolvedComments(CodeReview review) throws SQLException {
        int revision = review.getCurrentRevisionNumber() > 0 ? review.getCurrentRevisionNumber() : 1;
        if (commentDAO.countUnresolvedRootCommentsForRevision(review.getId(), revision) > 0) {
            throw new IllegalStateException("Есть нерешённые комментарии");
        }
    }

    private void ensureReviewerHasNoOwnUnresolvedComments(int reviewId, User actor) throws SQLException {
        if (actor == null) {
            throw new SecurityException("User must be authenticated");
        }
        if (commentDAO.countUnresolvedRootCommentsByAuthor(reviewId, actor.getId()) > 0) {
            throw new IllegalStateException("Сначала закройте свои комментарии (Resolve)");
        }
    }

    private boolean allReviewersApproved(int reviewId) throws SQLException {
        List<ReviewReviewer> reviewers = reviewReviewerDAO.findByReviewId(reviewId);
        if (reviewers.isEmpty()) {
            return true;
        }
        for (ReviewReviewer reviewer : reviewers) {
            Integer voteValue = reviewVoteDAO.findVoteValue(reviewId, reviewer.getUserId(), ReviewVoteLabel.CODE_REVIEW);
            if (voteValue == null || voteValue < 1) {
                return false;
            }
        }
        return true;
    }

    private void ensureAllReviewersApproved(int reviewId) throws SQLException {
        if (!allReviewersApproved(reviewId)) {
            throw new IllegalStateException("Не все назначенные ревьюеры поставили плюс");
        }
    }

    private void transition(CodeReview review, ReviewStatus target) throws SQLException {
        if (review.getStatus() == null) {
            review.setStatus(ReviewStatus.WIP);
        }
        if (!review.getStatus().canTransitionTo(target)) {
            throw new IllegalStateException("Cannot change status from "
                    + review.getStatus() + " to " + target);
        }
        reviewDAO.updateStatus(review.getId(), target);
        review.setStatus(target);
    }

    private boolean isAdmin(User actor) {
        return actor != null && "ADMIN".equalsIgnoreCase(actor.getRole());
    }

    public void deleteReview(int reviewId) throws SQLException {
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        reviewDAO.delete(reviewId);
    }

    public int addFileToReview(User actor, int reviewId, String filename, String content) throws SQLException {
        if (actor == null) {
            throw new SecurityException("User must be authenticated");
        }
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
        if (content == null) {
            throw new IllegalArgumentException("File content cannot be null");
        }

        CodeReview review = requireReview(reviewId);
        ensureAuthor(actor, review);
        ensureCurrentRevisionEditable(review);

        String normalizedName = filename.trim();
        int currentRevision = review.getCurrentRevisionNumber();
        
        String normalizedContent = content.replace("\r\n", "\n").replace("\r", "\n");

        ReviewFile currentFile = reviewFileDAO.findByReviewRevisionAndFilename(reviewId, currentRevision, normalizedName);
        ReviewFile previousFile = reviewFileDAO.findLatestBeforeRevision(reviewId, normalizedName, currentRevision);
        ProjectFile existingInProject = projectFileDAO.findByProjectAndName(review.getProjectId(), normalizedName);

        boolean existedBefore = previousFile != null || existingInProject != null;

        ReviewFileChangeType changeType;
        if (normalizedContent.isBlank()) {
            if (!existedBefore && currentFile == null) {
                throw new IllegalArgumentException("Нельзя удалить файл, которого нет в проекте или прошлых ревизиях");
            }
            changeType = ReviewFileChangeType.DELETED;
        } else if (!existedBefore && currentFile == null) {
            changeType = ReviewFileChangeType.ADDED;
        } else {
            changeType = ReviewFileChangeType.MODIFIED;
        }

        if (currentFile != null) {
            currentFile.setContent(normalizedContent);
            currentFile.setChangeType(changeType);
            reviewFileDAO.update(currentFile);
            resetVotesAndReopenIfNeeded(review);
            return currentFile.getId();
        }

        ReviewFile file = new ReviewFile(reviewId, normalizedName, normalizedContent);
        file.setRevisionNumber(currentRevision);
        file.setChangeType(changeType);
        int id = reviewFileDAO.create(file);

        resetVotesAndReopenIfNeeded(review);
        return id;
    }

    private void resetVotesAndReopenIfNeeded(CodeReview review) throws SQLException {
        if (!allReviewersApproved(review.getId())) {
            return;
        }

        reviewVoteDAO.deleteByReviewId(review.getId());

        if (review.getStatus() == ReviewStatus.APPROVED) {
            transition(review, ReviewStatus.CHANGES_REQUIRED);
        }
    }

    public List<ReviewFile> getFilesForRevision(int reviewId, int revisionNumber) throws SQLException {
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        if (revisionNumber < 0) {
            return reviewFileDAO.findByReviewId(reviewId);
        }
        if (revisionNumber == 0) {
            return reviewFileDAO.findByReviewAndRevision(reviewId, 0);
        }
        return reviewFileDAO.findByReviewAndRevision(reviewId, revisionNumber);
    }

    /**
     * Удалить конкретный файл по id.
     */
    public void deleteFile(int fileId) throws SQLException {
        if (fileId <= 0) {
            throw new IllegalArgumentException("Invalid file ID");
        }
        reviewFileDAO.deleteById(fileId);
    }

    public void deleteFilesForReview(int reviewId) throws SQLException {
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        reviewFileDAO.deleteByReviewId(reviewId);
    }

    public List<Integer> getRevisionNumbers(int reviewId) throws SQLException {
        if (reviewId <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }
        List<Integer> revisions = reviewFileDAO.findRevisionNumbers(reviewId);
        revisions.removeIf(r -> r <= 0);
        if (revisions.isEmpty()) {
            CodeReview review = requireReview(reviewId);
            int maxRevision = Math.max(1, review.getCurrentRevisionNumber());
            for (int i = 1; i <= maxRevision; i++) {
                revisions.add(i);
            }
        }
        return revisions;
    }

    public boolean canEditCurrentRevision(CodeReview review) {
        if (review == null) {
            return false;
        }
        if (review.getStatus() == null || review.getStatus() == ReviewStatus.WIP) {
            return true;
        }
        return review.getCurrentRevisionNumber() > review.getLockedRevisionNumber();
    }

    private void ensureCurrentRevisionEditable(CodeReview review) {
        if (!canEditCurrentRevision(review)) {
            throw new IllegalStateException("Текущий патчсет опубликован. Создайте новую ревизию перед изменениями.");
        }
    }

    private void lockCurrentRevision(CodeReview review) throws SQLException {
        int revision = review.getCurrentRevisionNumber();
        reviewDAO.updateLockedRevisionNumber(review.getId(), revision);
        review.setLockedRevisionNumber(revision);
    }
}
