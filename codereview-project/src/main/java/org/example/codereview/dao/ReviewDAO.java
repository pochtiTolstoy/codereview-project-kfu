package org.example.codereview.dao;

import org.example.codereview.model.CodeReview;
import org.example.codereview.model.ReviewStatus;
import org.example.codereview.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    public int create(CodeReview review) throws SQLException {
        String sql = "INSERT INTO code_reviews(project_id, author_id, title, content, status, current_revision_number, locked_revision_number, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, review.getProjectId());
            stmt.setInt(2, review.getAuthorId());
            stmt.setString(3, review.getTitle());
            stmt.setString(4, review.getContent());
            ReviewStatus status = review.getStatus() != null ? review.getStatus() : ReviewStatus.WIP;
            stmt.setString(5, status.name());
            int revision = review.getCurrentRevisionNumber() > 0 ? review.getCurrentRevisionNumber() : 1;
            stmt.setInt(6, revision);
            stmt.setInt(7, review.getLockedRevisionNumber());
            Timestamp createdAt = review.getCreatedAt();
            if (createdAt == null) {
                createdAt = new Timestamp(System.currentTimeMillis());
            }
            stmt.setTimestamp(8, createdAt);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    review.setId(rs.getInt(1));
                }
            }
        }
        return review.getId();
    }

    public CodeReview findById(int id) throws SQLException {
        String sql = "SELECT * FROM code_reviews WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                CodeReview review = new CodeReview();
                review.setId(rs.getInt("id"));
                review.setProjectId(rs.getInt("project_id"));
                review.setAuthorId(rs.getInt("author_id"));
                review.setTitle(rs.getString("title"));
                review.setContent(rs.getString("content"));
                review.setStatus(rs.getString("status"));
                review.setCurrentRevisionNumber(rs.getInt("current_revision_number"));
                review.setLockedRevisionNumber(rs.getInt("locked_revision_number"));
                review.setCreatedAt(rs.getTimestamp("created_at"));
                return review;
            }
        }
        return null;
    }

    public List<CodeReview> findByProjectId(int projectId) throws SQLException {
        List<CodeReview> reviews = new ArrayList<>();
        String sql = "SELECT * FROM code_reviews WHERE project_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CodeReview review = new CodeReview();
                review.setId(rs.getInt("id"));
                review.setProjectId(rs.getInt("project_id"));
                review.setAuthorId(rs.getInt("author_id"));
                review.setTitle(rs.getString("title"));
                review.setContent(rs.getString("content"));
                review.setStatus(rs.getString("status"));
                review.setCurrentRevisionNumber(rs.getInt("current_revision_number"));
                review.setLockedRevisionNumber(rs.getInt("locked_revision_number"));
                review.setCreatedAt(rs.getTimestamp("created_at"));
                reviews.add(review);
            }
        }
        return reviews;
    }

    public List<CodeReview> findAll() throws SQLException {
        List<CodeReview> reviews = new ArrayList<>();
        String sql = "SELECT * FROM code_reviews ORDER BY created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CodeReview review = new CodeReview();
                review.setId(rs.getInt("id"));
                review.setProjectId(rs.getInt("project_id"));
                review.setAuthorId(rs.getInt("author_id"));
                review.setTitle(rs.getString("title"));
                review.setContent(rs.getString("content"));
                review.setStatus(rs.getString("status"));
                review.setCurrentRevisionNumber(rs.getInt("current_revision_number"));
                review.setLockedRevisionNumber(rs.getInt("locked_revision_number"));
                review.setCreatedAt(rs.getTimestamp("created_at"));
                reviews.add(review);
            }
        }
        return reviews;
    }

    public List<CodeReview> findByAuthorId(int authorId) throws SQLException {
        List<CodeReview> reviews = new ArrayList<>();
        String sql = "SELECT * FROM code_reviews WHERE author_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, authorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CodeReview review = new CodeReview();
                review.setId(rs.getInt("id"));
                review.setProjectId(rs.getInt("project_id"));
                review.setAuthorId(rs.getInt("author_id"));
                review.setTitle(rs.getString("title"));
                review.setContent(rs.getString("content"));
                review.setStatus(rs.getString("status"));
                review.setCurrentRevisionNumber(rs.getInt("current_revision_number"));
                review.setLockedRevisionNumber(rs.getInt("locked_revision_number"));
                review.setCreatedAt(rs.getTimestamp("created_at"));
                reviews.add(review);
            }
        }
        return reviews;
    }

    public void update(CodeReview review) throws SQLException {
        String sql = "UPDATE code_reviews SET title = ?, content = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, review.getTitle());
            stmt.setString(2, review.getContent());
            ReviewStatus status = review.getStatus() != null ? review.getStatus() : ReviewStatus.WIP;
            stmt.setString(3, status.name());
            stmt.setInt(4, review.getId());
            stmt.executeUpdate();
        }
    }

    public void updateStatus(int id, ReviewStatus status) throws SQLException {
        String sql = "UPDATE code_reviews SET status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM code_reviews WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void updateCurrentRevisionNumber(int reviewId, int revisionNumber) throws SQLException {
        String sql = "UPDATE code_reviews SET current_revision_number = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, revisionNumber);
            stmt.setInt(2, reviewId);
            stmt.executeUpdate();
        }
    }

    public void updateLockedRevisionNumber(int reviewId, int lockedRevisionNumber) throws SQLException {
        String sql = "UPDATE code_reviews SET locked_revision_number = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lockedRevisionNumber);
            stmt.setInt(2, reviewId);
            stmt.executeUpdate();
        }
    }
}

