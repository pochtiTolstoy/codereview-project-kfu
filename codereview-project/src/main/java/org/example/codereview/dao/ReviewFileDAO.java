package org.example.codereview.dao;

import org.example.codereview.model.ReviewFile;
import org.example.codereview.model.ReviewFileChangeType;
import org.example.codereview.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReviewFileDAO {

    public int create(ReviewFile file) throws SQLException {
        String sql = "INSERT INTO review_files (review_id, filename, content, revision_number, change_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, file.getReviewId());
            stmt.setString(2, file.getFilename());
            stmt.setString(3, file.getContent());
            stmt.setInt(4, file.getRevisionNumber());
            stmt.setString(5, file.getChangeType().name());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    file.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public ReviewFile findById(int id) throws SQLException {
        String sql = "SELECT id, review_id, filename, content, revision_number, change_type FROM review_files WHERE id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ReviewFile file = new ReviewFile();
                    file.setId(rs.getInt("id"));
                    file.setReviewId(rs.getInt("review_id"));
                    file.setFilename(rs.getString("filename"));
                    file.setContent(rs.getString("content"));
                    file.setRevisionNumber(rs.getInt("revision_number"));
                    String change = rs.getString("change_type");
                    if (change != null) {
                        file.setChangeType(ReviewFileChangeType.valueOf(change));
                    }
                    return file;
                }
            }
        }

        return null;
    }

    public List<ReviewFile> findByReviewId(int reviewId) throws SQLException {
        List<ReviewFile> files = new ArrayList<>();
        String sql = "SELECT id, review_id, filename, content, revision_number, change_type FROM review_files WHERE review_id = ? ORDER BY revision_number DESC, id";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReviewFile file = new ReviewFile();
                    file.setId(rs.getInt("id"));
                    file.setReviewId(rs.getInt("review_id"));
                    file.setFilename(rs.getString("filename"));
                    file.setContent(rs.getString("content"));
                    file.setRevisionNumber(rs.getInt("revision_number"));
                    String change = rs.getString("change_type");
                    if (change != null) {
                        file.setChangeType(ReviewFileChangeType.valueOf(change));
                    }
                    files.add(file);
                }
            }
        }

        return files;
    }

    public List<ReviewFile> findByReviewAndRevision(int reviewId, int revisionNumber) throws SQLException {
        List<ReviewFile> files = new ArrayList<>();
        String sql = "SELECT id, review_id, filename, content, revision_number, change_type " +
                "FROM review_files WHERE review_id = ? AND revision_number = ? ORDER BY filename";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.setInt(2, revisionNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReviewFile file = new ReviewFile();
                    file.setId(rs.getInt("id"));
                    file.setReviewId(rs.getInt("review_id"));
                    file.setFilename(rs.getString("filename"));
                    file.setContent(rs.getString("content"));
                    file.setRevisionNumber(rs.getInt("revision_number"));
                    String change = rs.getString("change_type");
                    if (change != null) {
                        file.setChangeType(ReviewFileChangeType.valueOf(change));
                    }
                    files.add(file);
                }
            }
        }
        return files;
    }

    public ReviewFile findByReviewRevisionAndFilename(int reviewId, int revisionNumber, String filename) throws SQLException {
        String sql = "SELECT id, review_id, filename, content, revision_number, change_type " +
                "FROM review_files WHERE review_id = ? AND revision_number = ? AND filename = ? LIMIT 1";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.setInt(2, revisionNumber);
            stmt.setString(3, filename);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public ReviewFile findLatestBeforeRevision(int reviewId, String filename, int revisionNumber) throws SQLException {
        String sql = "SELECT id, review_id, filename, content, revision_number, change_type " +
                "FROM review_files WHERE review_id = ? AND filename = ? AND revision_number < ? " +
                "ORDER BY revision_number DESC LIMIT 1";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.setString(2, filename);
            stmt.setInt(3, revisionNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Integer> findRevisionNumbers(int reviewId) throws SQLException {
        List<Integer> revisions = new ArrayList<>();
        String sql = "SELECT DISTINCT revision_number FROM review_files WHERE review_id = ? ORDER BY revision_number ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    revisions.add(rs.getInt("revision_number"));
                }
            }
        }
        return revisions;
    }

    public void update(ReviewFile file) throws SQLException {
        String sql = "UPDATE review_files SET filename = ?, content = ?, revision_number = ?, change_type = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, file.getFilename());
            stmt.setString(2, file.getContent());
            stmt.setInt(3, file.getRevisionNumber());
            stmt.setString(4, file.getChangeType().name());
            stmt.setInt(5, file.getId());
            stmt.executeUpdate();
        }
    }

    private ReviewFile mapRow(ResultSet rs) throws SQLException {
        ReviewFile file = new ReviewFile();
        file.setId(rs.getInt("id"));
        file.setReviewId(rs.getInt("review_id"));
        file.setFilename(rs.getString("filename"));
        file.setContent(rs.getString("content"));
        file.setRevisionNumber(rs.getInt("revision_number"));
        String change = rs.getString("change_type");
        if (change != null) {
            file.setChangeType(ReviewFileChangeType.valueOf(change));
        }
        return file;
    }

    public void deleteByReviewId(int reviewId) throws SQLException {
        String sql = "DELETE FROM review_files WHERE review_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reviewId);
            stmt.executeUpdate();
        }
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM review_files WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}

