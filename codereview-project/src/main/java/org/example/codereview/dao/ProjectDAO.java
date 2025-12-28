package org.example.codereview.dao;

import org.example.codereview.model.Project;
import org.example.codereview.model.User;
import org.example.codereview.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    public int create(Project project) throws SQLException {
        String sql = "INSERT INTO projects(name, description, owner_id) VALUES (?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setInt(3, project.getOwnerId());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    project.setId(rs.getInt(1));
                }
            }
        }
        // Добавляем запись о членстве владельца
        String memberSql = "INSERT INTO project_members(user_id, project_id) VALUES (?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(memberSql)) {
            stmt.setInt(1, project.getOwnerId());
            stmt.setInt(2, project.getId());
            stmt.executeUpdate();
        }
        return project.getId();
    }

    public Project findById(int id) throws SQLException {
        String sql = "SELECT * FROM projects WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Project project = new Project();
                project.setId(rs.getInt("id"));
                project.setName(rs.getString("name"));
                project.setDescription(rs.getString("description"));
                project.setOwnerId(rs.getInt("owner_id"));
                return project;
            }
        }
        return null;
    }

    public List<Project> findAll() throws SQLException {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects ORDER BY id";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Project project = new Project();
                project.setId(rs.getInt("id"));
                project.setName(rs.getString("name"));
                project.setDescription(rs.getString("description"));
                project.setOwnerId(rs.getInt("owner_id"));
                projects.add(project);
            }
        }
        return projects;
    }

    public List<Project> findByUser(int userId) throws SQLException {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT p.* FROM projects p " +
                     "INNER JOIN project_members pm ON p.id = pm.project_id " +
                     "WHERE pm.user_id = ? ORDER BY p.id";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Project project = new Project();
                project.setId(rs.getInt("id"));
                project.setName(rs.getString("name"));
                project.setDescription(rs.getString("description"));
                project.setOwnerId(rs.getInt("owner_id"));
                projects.add(project);
            }
        }
        return projects;
    }

    public void update(Project project) throws SQLException {
        String sql = "UPDATE projects SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setInt(3, project.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        // Сначала удаляем связи в project_members
        String memberSql = "DELETE FROM project_members WHERE project_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(memberSql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
        // Затем удаляем сам проект
        String sql = "DELETE FROM projects WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public boolean isMember(int userId, int projectId) throws SQLException {
        String sql = "SELECT 1 FROM project_members WHERE user_id = ? AND project_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, projectId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public void addMember(int userId, int projectId) throws SQLException {
        String sql = "INSERT INTO project_members(user_id, project_id) VALUES (?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, projectId);
            stmt.executeUpdate();
        }
    }

    public void removeMember(int userId, int projectId) throws SQLException {
        String sql = "DELETE FROM project_members WHERE user_id = ? AND project_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, projectId);
            stmt.executeUpdate();
        }
    }

    public List<Integer> getMemberIds(int projectId) throws SQLException {
        List<Integer> memberIds = new ArrayList<>();
        String sql = "SELECT user_id FROM project_members WHERE project_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                memberIds.add(rs.getInt("user_id"));
            }
        }
        return memberIds;
    }

    public List<User> findMembers(int projectId) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                     "JOIN project_members pm ON pm.user_id = u.id " +
                     "WHERE pm.project_id = ? ORDER BY u.id";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setRole(rs.getString("role"));
                users.add(u);
            }
        }
        return users;
    }
}

