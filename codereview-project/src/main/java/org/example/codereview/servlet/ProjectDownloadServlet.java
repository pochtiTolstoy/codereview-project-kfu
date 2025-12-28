package org.example.codereview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.example.codereview.model.Project;
import org.example.codereview.model.ProjectFile;
import org.example.codereview.model.User;
import org.example.codereview.service.ProjectFileService;
import org.example.codereview.service.ProjectService;

@WebServlet("/projects/download")
public class ProjectDownloadServlet extends HttpServlet {

    private ProjectService projectService;
    private ProjectFileService projectFileService;

    @Override
    public void init() throws ServletException {
        Object ps = getServletContext().getAttribute("projectService");
        Object pfs = getServletContext().getAttribute("projectFileService");
        if (!(ps instanceof ProjectService) || !(pfs instanceof ProjectFileService)) {
            throw new ServletException("Project services not initialized in ServletContext");
        }
        projectService = (ProjectService) ps;
        projectFileService = (ProjectFileService) pfs;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String projectIdParam = req.getParameter("id");
        if (projectIdParam == null || projectIdParam.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
            return;
        }

        int projectId;
        try {
            projectId = Integer.parseInt(projectIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
            return;
        }

        try {
            Project project = projectService.getProject(projectId);
            if (project == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                return;
            }

            boolean isMember = projectService.isUserMember(currentUser.getId(), projectId);
            boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
            if (!isMember && !isAdmin) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not a member of this project");
                return;
            }

            List<ProjectFile> files = projectFileService.getFilesForProject(projectId);
            String archiveName = buildArchiveName(project) + ".zip";

            resp.setContentType("application/zip");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + archiveName + "\"");

            try (ZipOutputStream zos = new ZipOutputStream(resp.getOutputStream(), StandardCharsets.UTF_8)) {
                for (ProjectFile file : files) {
                    String entryName = sanitizePath(file.getFilename(), file.getId());
                    zos.putNextEntry(new ZipEntry(entryName));
                    byte[] contentBytes = file.getContent() != null
                            ? file.getContent().getBytes(StandardCharsets.UTF_8)
                            : new byte[0];
                    zos.write(contentBytes);
                    zos.closeEntry();
                }
                zos.finish();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to prepare project archive");
        }
    }

    private String buildArchiveName(Project project) {
        String base = project.getName() != null ? project.getName().trim() : "";
        if (base.isEmpty()) {
            base = "project-" + project.getId();
        }
        
        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String sanitizePath(String rawPath, int fallbackId) {
        if (rawPath == null || rawPath.isBlank()) {
            return "file-" + fallbackId;
        }
        String normalized = rawPath.replace('\\', '/');
        
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.contains("..")) {
            normalized = normalized.replace("..", "_");
        }
        return normalized.isEmpty() ? "file-" + fallbackId : normalized;
    }
}

