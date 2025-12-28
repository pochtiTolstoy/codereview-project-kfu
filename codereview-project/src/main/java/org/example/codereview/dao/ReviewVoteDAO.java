package org.example.codereview.dao;

import org.example.codereview.model.ReviewVote;
import org.example.codereview.model.ReviewVoteLabel;
import org.example.codereview.util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewVoteDAO {

    public void upsertVote(int reviewId, int userId, ReviewVoteLabel label, int value) throws SQLException {
        String sql = "INSERT INTO review_votes (review_id, user_id, label, value) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (review_id, user_id, label) DO UPDATE " +
                "SET value = EXCLUDED.value, created_at = CURRENT_TIMESTAMP";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.setInt(2, userId);
            stmt.setString(3, label.name());
            stmt.setInt(4, value);
            stmt.executeUpdate();
        }
    }

    public List<ReviewVote> findByReviewId(int reviewId) throws SQLException {
        String sql = "SELECT id, review_id, user_id, label, value, created_at " +
                "FROM review_votes WHERE review_id = ? ORDER BY created_at";
        List<ReviewVote> votes = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReviewVote vote = new ReviewVote();
                    vote.setId(rs.getInt("id"));
                    vote.setReviewId(rs.getInt("review_id"));
                    vote.setUserId(rs.getInt("user_id"));
                    vote.setLabel(ReviewVoteLabel.valueOf(rs.getString("label")));
                    vote.setValue(rs.getInt("value"));
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) {
                        vote.setCreatedAt(created.toLocalDateTime());
                    }
                    votes.add(vote);
                }
            }
        }
        return votes;
    }

    public Integer findVoteValue(int reviewId, int userId, ReviewVoteLabel label) throws SQLException {
        String sql = "SELECT value FROM review_votes WHERE review_id = ? AND user_id = ? AND label = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.setInt(2, userId);
            stmt.setString(3, label.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("value");
                }
            }
        }
        return null;
    }

    public void deleteByReviewId(int reviewId) throws SQLException {
        String sql = "DELETE FROM review_votes WHERE review_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.executeUpdate();
        }
    }
}

