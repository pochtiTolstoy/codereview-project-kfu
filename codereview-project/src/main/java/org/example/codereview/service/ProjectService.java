package org.example.codereview.service;

import org.example.codereview.dao.ProjectDAO;
import org.example.codereview.dao.UserDAO;
import org.example.codereview.model.Project;
import org.example.codereview.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectService {
    private final ProjectDAO projectDAO = new ProjectDAO();

    public int createProject(int ownerId, String name, String description) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Project name is too long (max 255 characters)");
        }
        if (ownerId <= 0) {
            throw new IllegalArgumentException("Invalid owner ID");
        }

        Project project = new Project();
        project.setName(name.trim());
        project.setDescription(description != null ? description.trim() : null);
        project.setOwnerId(ownerId);

        int projectId = projectDAO.create(project);
        return projectId;
    }

    public Project getProject(int projectId) throws SQLException {
        if (projectId <= 0) {
            throw new IllegalArgumentException("Invalid project ID");
        }
        return projectDAO.findById(projectId);
    }

    public List<Project> getProjectsForUser(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return projectDAO.findByUser(userId);
    }

    public List<Project> getAllProjects() throws SQLException {
        return projectDAO.findAll();
    }

    public void updateProject(Project project) throws SQLException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        if (project.getId() <= 0) {
            throw new IllegalArgumentException("Invalid project ID");
        }
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        if (project.getName().length() > 255) {
            throw new IllegalArgumentException("Project name is too long (max 255 characters)");
        }

        project.setName(project.getName().trim());
        if (project.getDescription() != null) {
            project.setDescription(project.getDescription().trim());
        }

        projectDAO.update(project);
    }

    public void deleteProject(int projectId) throws SQLException {
        if (projectId <= 0) {
            throw new IllegalArgumentException("Invalid project ID");
        }
        projectDAO.delete(projectId);
    }

    public boolean isUserMember(int userId, int projectId) throws SQLException {
        if (userId <= 0 || projectId <= 0) {
            return false;
        }
        return projectDAO.isMember(userId, projectId);
    }

    public void addMember(int userId, int projectId) throws SQLException {
        if (userId <= 0 || projectId <= 0) {
            throw new IllegalArgumentException("Invalid user ID or project ID");
        }
        if (projectDAO.isMember(userId, projectId)) {
            throw new IllegalArgumentException("User is already a member of this project");
        }
        projectDAO.addMember(userId, projectId);
    }

    public void removeMember(int userId, int projectId) throws SQLException {
        if (userId <= 0 || projectId <= 0) {
            throw new IllegalArgumentException("Invalid user ID or project ID");
        }
        Project project = projectDAO.findById(projectId);
        if (project != null && project.getOwnerId() == userId) {
            throw new IllegalArgumentException("Cannot remove project owner from members");
        }
        projectDAO.removeMember(userId, projectId);
    }

    public List<User> getProjectMembers(int projectId) throws SQLException {
        if (projectId <= 0) {
            throw new IllegalArgumentException("Invalid project ID");
        }
        List<Integer> memberIds = projectDAO.getMemberIds(projectId);
        List<User> members = new ArrayList<>();
        UserDAO userDAO = new UserDAO();
        for (Integer userId : memberIds) {
            User user = userDAO.findById(userId);
            if (user != null) {
                members.add(user);
            }
        }
        return members;
    }
}