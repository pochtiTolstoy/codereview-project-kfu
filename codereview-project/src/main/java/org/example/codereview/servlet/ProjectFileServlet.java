package org.example.codereview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.example.codereview.model.User;
import org.example.codereview.model.Project;
import org.example.codereview.model.ProjectFile;
import org.example.codereview.service.ProjectFileService;
import org.example.codereview.service.ProjectService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@WebServlet("/project/file/*")
public class ProjectFileServlet extends HttpServlet {

    private ProjectFileService projectFileService;
    private ProjectService projectService;

    @Override
    public void init() throws ServletException {
        projectFileService = (ProjectFileService) getServletContext().getAttribute("projectFileService");
        projectService = (ProjectService) getServletContext().getAttribute("projectService");
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

        String pathInfo = req.getPathInfo(); 
        if (pathInfo == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            switch (pathInfo) {
                case "/view": {
                    handleView(req, resp, currentUser);
                    break;
                }
                case "/edit": {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Изменения файлов возможны только через ревью");
                    return;
                }
                case "/new": {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Добавление файлов возможно только через ревью");
                    return;
                }
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DB error");
        }
    }

    private void handleView(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws SQLException, ServletException, IOException {

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File ID is required");
            return;
        }

        int fileId = Integer.parseInt(idParam);
        ProjectFile file = projectFileService.getFile(fileId);
        if (file == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }

        Project project = projectService.getProject(file.getProjectId());
        if (project == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            return;
        } 

        
        boolean isMember = projectService.isUserMember(currentUser.getId(), project.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        if (!isMember && !isAdmin) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Not allowed to view this file");
            return;
        } 

        boolean isOwner = (project.getOwnerId() == currentUser.getId());
        req.setAttribute("canEditProject", isOwner || isAdmin);
        req.setAttribute("project", project);
        req.setAttribute("file", file);

        req.getRequestDispatcher("/WEB-INF/views/project_file_view.jsp").forward(req, resp);
    }

    @SuppressWarnings("unused")
    private void handleEditForm(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws SQLException, ServletException, IOException {

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File ID is required");
            return;
        }

        int fileId = Integer.parseInt(idParam);
        ProjectFile file = projectFileService.getFile(fileId);
        if (file == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }

        Project project = projectService.getProject(file.getProjectId());
        if (project == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            return;
        }

        boolean isOwner = (project.getOwnerId() == currentUser.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Not allowed to edit this file");
            return;
        }

        req.setAttribute("project", project);
        req.setAttribute("file", file);

        req.getRequestDispatcher("/WEB-INF/views/project_file_edit.jsp").forward(req, resp);
    }

    @SuppressWarnings("unused")
    private void handleNewForm(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws SQLException, ServletException, IOException {

        String projectIdParam = req.getParameter("projectId");
        if (projectIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
            return;
        }

        int projectId = Integer.parseInt(projectIdParam);
        Project project = projectService.getProject(projectId);
        if (project == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            return;
        }

        boolean isOwner = (project.getOwnerId() == currentUser.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Not allowed to add files");
            return;
        }

        req.setAttribute("project", project);
        req.getRequestDispatcher("/WEB-INF/views/project_file_new.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        switch (pathInfo) {
            case "/new":
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Добавление файлов возможно только через ревью");
                return;
            case "/edit":
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Изменение файлов возможно только через ревью");
                return;
            case "/delete":
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Удаление файлов возможно только через ревью");
                return;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @SuppressWarnings("unused")
    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws SQLException, IOException {

        String projectIdParam = req.getParameter("projectId");
        String filename = req.getParameter("filename");
        String content = req.getParameter("content");

        if (projectIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
            return;
        }

        int projectId = Integer.parseInt(projectIdParam);

        try {
            int newFileId = projectFileService.addFile(currentUser, projectId, filename, content);
            resp.sendRedirect(req.getContextPath() + "/project/file/view?id=" + newFileId);
        } catch (IllegalArgumentException | SecurityException e) {
            String error = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath()
                    + "/project/file/new?projectId=" + projectId
                    + "&error=" + error);
        }
    }

    @SuppressWarnings("unused")
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws SQLException, IOException {

        String fileIdParam = req.getParameter("id");
        String filename = req.getParameter("filename");
        String content = req.getParameter("content");

        if (fileIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File ID is required");
            return;
        }

        int fileId = Integer.parseInt(fileIdParam);
        ProjectFile existing = projectFileService.getFile(fileId);
        if (existing == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }

        existing.setFilename(filename);
        existing.setContent(content);

        try {
            projectFileService.updateFile(currentUser, existing);
            resp.sendRedirect(req.getContextPath() + "/project/file/view?id=" + fileId);
        } catch (IllegalArgumentException | SecurityException e) {
            String error = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(
                    req.getContextPath()
                    + "/project/file/edit?id=" + fileId
                    + "&error=" + error
            );
        }
    }

    @SuppressWarnings("unused")
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws SQLException, IOException {

        String fileIdParam = req.getParameter("id");
        if (fileIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File ID is required");
            return;
        }

        int fileId = Integer.parseInt(fileIdParam);
        ProjectFile existing = projectFileService.getFile(fileId);
        if (existing == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }

        int projectId = existing.getProjectId();

        try {
            projectFileService.deleteFile(currentUser, fileId);
            resp.sendRedirect(req.getContextPath() + "/project?id=" + projectId);
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }
}

