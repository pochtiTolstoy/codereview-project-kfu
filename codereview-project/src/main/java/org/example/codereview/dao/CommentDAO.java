package org.example.codereview.dao;

import org.example.codereview.model.Comment;
import org.example.codereview.util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    // Создание комментария (общего или inline)
    public int create(Comment comment) throws SQLException {
        String sql = "INSERT INTO review_comments " +
                "(review_id, author_id, review_file_id, line_number, parent_id, resolved, content, revision_number, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, comment.getReviewId());
            stmt.setInt(2, comment.getAuthorId());

            // review_file_id (может быть null)
            if (comment.getReviewFileId() != null) {
                stmt.setInt(3, comment.getReviewFileId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            // line_number (может быть null)
            if (comment.getLineNumber() != null) {
                stmt.setInt(4, comment.getLineNumber());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            // parent_id (может быть null)
            if (comment.getParentId() != null) {
                stmt.setInt(5, comment.getParentId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setBoolean(6, comment.isResolved());
            stmt.setString(7, comment.getContent());
            stmt.setInt(8, comment.getRevisionNumber());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    comment.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    // Один комментарий по id
    public Comment findById(int id) throws SQLException {
        String sql = "SELECT * FROM review_comments WHERE id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    // Все комментарии по ревью (и общие, и inline)
    public List<Comment> findByReviewId(int reviewId) throws SQLException {
        List<Comment> result = new ArrayList<>();
        String sql = "SELECT rc.*, rf.filename AS review_file_name FROM review_comments rc " +
                     "LEFT JOIN review_files rf ON rc.review_file_id = rf.id " +
                     "WHERE rc.review_id = ? " +
                     "ORDER BY rc.review_file_id NULLS FIRST, rc.line_number NULLS FIRST, rc.created_at ASC";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    // Все комментарии по конкретному файлу ревью
    public List<Comment> findByReviewFileId(int reviewFileId) throws SQLException {
        List<Comment> result = new ArrayList<>();
        String sql = "SELECT * FROM review_comments " +
                     "WHERE review_file_id = ? " +
                     "ORDER BY created_at ASC";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reviewFileId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    // Удаление комментария
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM review_comments WHERE id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Обновление статуса resolved только для root-комментариев
    public void updateResolved(int commentId, boolean resolved, Integer userId) throws SQLException {
        String sql = "UPDATE review_comments SET resolved = ?, resolved_by = ?, resolved_at = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, resolved);

            if (resolved) {
                if (userId != null) {
                    stmt.setInt(2, userId);
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            } else {
                stmt.setNull(2, Types.INTEGER);
                stmt.setNull(3, Types.TIMESTAMP);
            }

            stmt.setInt(4, commentId);
            stmt.executeUpdate();
        }
    }

    private Comment mapRow(ResultSet rs) throws SQLException {
        Comment c = new Comment();
        c.setId(rs.getInt("id"));
        c.setReviewId(rs.getInt("review_id"));
        c.setAuthorId(rs.getInt("author_id"));
        c.setContent(rs.getString("content"));
        c.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            int reviewFileId = rs.getInt("review_file_id");
            if (!rs.wasNull()) {
                c.setReviewFileId(reviewFileId);
            }
        } catch (SQLException e) {
        }

        try {
            int lineNumber = rs.getInt("line_number");
            if (!rs.wasNull()) {
                c.setLineNumber(lineNumber);
            }
        } catch (SQLException e) {
        }

        try {
            int parentId = rs.getInt("parent_id");
            if (!rs.wasNull()) {
                c.setParentId(parentId);
            }
        } catch (SQLException e) {
        }

        try {
            boolean resolved = rs.getBoolean("resolved");
            if (!rs.wasNull()) {
                c.setResolved(resolved);
            }
        } catch (SQLException e) {
        }

        try {
            int resolvedBy = rs.getInt("resolved_by");
            if (!rs.wasNull()) {
                c.setResolvedBy(resolvedBy);
            }
        } catch (SQLException e) {
        }

        try {
            Timestamp resolvedAt = rs.getTimestamp("resolved_at");
            if (resolvedAt != null) {
                c.setResolvedAt(resolvedAt);
            }
        } catch (SQLException e) {
        }

        try {
            int revision = rs.getInt("revision_number");
            if (!rs.wasNull()) {
                c.setRevisionNumber(revision);
            }
        } catch (SQLException e) {
        }

        try {
            String filename = rs.getString("review_file_name");
            c.setReviewFileName(filename);
        } catch (SQLException e) {
        }

        return c;
    }

    public int countUnresolvedRootComments(int reviewId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM review_comments " +
                "WHERE review_id = ? AND parent_id IS NULL AND resolved = FALSE";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countUnresolvedRootCommentsForRevision(int reviewId, int revisionNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM review_comments " +
                "WHERE review_id = ? AND parent_id IS NULL AND resolved = FALSE AND revision_number = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.setInt(2, revisionNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countUnresolvedRootCommentsByAuthor(int reviewId, int authorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM review_comments " +
                "WHERE review_id = ? AND parent_id IS NULL AND resolved = FALSE AND author_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.setInt(2, authorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
