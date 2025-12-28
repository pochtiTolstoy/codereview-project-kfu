package org.example.codereview.service;

import org.example.codereview.dao.ProjectDAO;
import org.example.codereview.dao.ProjectFileDAO;
import org.example.codereview.model.ProjectFile;
import org.example.codereview.model.Project;
import org.example.codereview.model.User;

import java.sql.SQLException;
import java.util.List;

public class ProjectFileService {

    private final ProjectFileDAO projectFileDAO = new ProjectFileDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();

    public List<ProjectFile> getFilesForProject(int projectId) throws SQLException {
        return projectFileDAO.findByProjectId(projectId);
    }

    public ProjectFile getFile(int fileId) throws SQLException {
        return projectFileDAO.findById(fileId);
    }

    public int addFile(User user, int projectId, String filename, String content)
            throws SQLException {

        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        if (filename.length() > 255) {
            throw new IllegalArgumentException("Filename too long");
        }

        Project project = projectDAO.findById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project not found");
        }

        boolean isOwner = project.getOwnerId() == user.getId();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Not allowed to add files to this project");
        }

        ProjectFile file = new ProjectFile(projectId, filename.trim(),
                content != null ? content : "");

        return projectFileDAO.create(file);
    }

    public void updateFile(User user, ProjectFile file) throws SQLException {

        Project project = projectDAO.findById(file.getProjectId());
        if (project == null) {
            throw new IllegalArgumentException("Project not found");
        }

        boolean isOwner = project.getOwnerId() == user.getId();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Not allowed to modify this file");
        }

        if (file.getFilename() == null || file.getFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        projectFileDAO.update(file);
    }

    public void deleteFile(User user, int fileId) throws SQLException {

        ProjectFile file = projectFileDAO.findById(fileId);
        if (file == null) {
            throw new IllegalArgumentException("File not found");
        }

        Project project = projectDAO.findById(file.getProjectId());

        boolean isOwner = project.getOwnerId() == user.getId();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Not allowed to delete this file");
        }

        projectFileDAO.delete(fileId);
    }
    
    public void createOrUpdateFile(int projectId, String filename, String content) 
            throws SQLException {
        
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
        
        if (filename.length() > 500) {
            throw new IllegalArgumentException("Filename too long (max 500 chars)");
        }
        
        Project project = projectDAO.findById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project not found");
        }
        
        ProjectFile existing = projectFileDAO.findByProjectAndName(projectId, filename.trim());
        
        if (existing != null) {
            existing.setContent(content != null ? content : "");
            projectFileDAO.update(existing);
        } else {
            ProjectFile newFile = new ProjectFile(projectId, filename.trim(), 
                content != null ? content : "");
            projectFileDAO.create(newFile);
        }
    }
}

