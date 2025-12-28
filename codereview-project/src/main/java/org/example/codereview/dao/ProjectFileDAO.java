package org.example.codereview.dao;

import org.example.codereview.model.ProjectFile;
import org.example.codereview.util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectFileDAO {

    public int create(ProjectFile file) throws SQLException {
        String sql = "INSERT INTO project_files(project_id, filename, content, last_review_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, file.getProjectId());
            stmt.setString(2, file.getFilename());
            stmt.setString(3, file.getContent());
            if (file.getLastReviewId() != null) {
                stmt.setInt(4, file.getLastReviewId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
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

    public ProjectFile findById(int id) throws SQLException {
        String sql = "SELECT * FROM project_files WHERE id = ?";

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

    public List<ProjectFile> findByProjectId(int projectId) throws SQLException {
        List<ProjectFile> files = new ArrayList<>();
        String sql = "SELECT * FROM project_files WHERE project_id = ? ORDER BY filename";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(mapRow(rs));
                }
            }
        }
        return files;
    }

    public ProjectFile findByProjectAndName(int projectId, String filename) throws SQLException {
        String sql = "SELECT * FROM project_files WHERE project_id = ? AND filename = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            stmt.setString(2, filename);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void update(ProjectFile file) throws SQLException {
        String sql = "UPDATE project_files SET filename = ?, content = ?, last_review_id = ?, last_updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, file.getFilename());
            stmt.setString(2, file.getContent());
            if (file.getLastReviewId() != null) {
                stmt.setInt(3, file.getLastReviewId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setInt(4, file.getId());
            stmt.executeUpdate();
        }
    }

    public void upsert(ProjectFile file) throws SQLException {
        ProjectFile existing = findByProjectAndName(file.getProjectId(), file.getFilename());
        if (existing == null) {
            create(file);
        } else {
            existing.setContent(file.getContent());
            existing.setLastReviewId(file.getLastReviewId());
            update(existing);
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM project_files WHERE id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void deleteByProject(int projectId) throws SQLException {
        String sql = "DELETE FROM project_files WHERE project_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            stmt.executeUpdate();
        }
    }

    public void deleteByProjectAndFilename(int projectId, String filename) throws SQLException {
        String sql = "DELETE FROM project_files WHERE project_id = ? AND filename = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            stmt.setString(2, filename);
            stmt.executeUpdate();
        }
    }

    private ProjectFile mapRow(ResultSet rs) throws SQLException {
        ProjectFile file = new ProjectFile();
        file.setId(rs.getInt("id"));
        file.setProjectId(rs.getInt("project_id"));
        file.setFilename(rs.getString("filename"));
        file.setContent(rs.getString("content"));
        int lastReviewId = rs.getInt("last_review_id");
        if (!rs.wasNull()) {
            file.setLastReviewId(lastReviewId);
        }
        Timestamp updatedAt = rs.getTimestamp("last_updated_at");
        if (updatedAt != null) {
            file.setLastUpdatedAt(updatedAt);
        }
        return file;
    }
}

