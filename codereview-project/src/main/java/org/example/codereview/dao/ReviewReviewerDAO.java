package org.example.codereview.dao;

import org.example.codereview.model.ReviewParticipantRole;
import org.example.codereview.model.ReviewReviewer;
import org.example.codereview.util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewReviewerDAO {

    public List<ReviewReviewer> findByReviewId(int reviewId) throws SQLException {
        String sql = "SELECT id, review_id, user_id, role, added_at " +
                "FROM review_reviewers WHERE review_id = ? ORDER BY added_at";
        List<ReviewReviewer> list = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public void addReviewer(int reviewId, int userId, ReviewParticipantRole role) throws SQLException {
        String sql = "INSERT INTO review_reviewers (review_id, user_id, role) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (review_id, user_id) DO UPDATE SET role = EXCLUDED.role";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.setInt(2, userId);
            stmt.setString(3, role.name());
            stmt.executeUpdate();
        }
    }

    public void removeReviewer(int reviewId, int userId) throws SQLException {
        String sql = "DELETE FROM review_reviewers WHERE review_id = ? AND user_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public boolean exists(int reviewId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM review_reviewers WHERE review_id = ? AND user_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private ReviewReviewer mapRow(ResultSet rs) throws SQLException {
        ReviewReviewer reviewer = new ReviewReviewer();
        reviewer.setId(rs.getInt("id"));
        reviewer.setReviewId(rs.getInt("review_id"));
        reviewer.setUserId(rs.getInt("user_id"));
        reviewer.setRole(ReviewParticipantRole.valueOf(rs.getString("role")));
        Timestamp addedAt = rs.getTimestamp("added_at");
        if (addedAt != null) {
            reviewer.setAddedAt(addedAt.toLocalDateTime());
        }
        return reviewer;
    }
}

