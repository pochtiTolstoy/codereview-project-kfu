package org.example.codereview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.example.codereview.model.User;
import org.example.codereview.model.Project;
import org.example.codereview.service.ProjectService;
import org.example.codereview.util.CsrfUtil;

@WebServlet("/projects/*")
public class ProjectServlet extends HttpServlet {
    private ProjectService projectService;

    @Override
    public void init() throws ServletException {
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
        
        
        if (pathInfo != null && pathInfo.equals("/new")) {
            req.getRequestDispatcher("/WEB-INF/views/new_project.jsp").forward(req, resp);
            return;
        }

        
        if (pathInfo != null && pathInfo.equals("/edit")) {
            String idParam = req.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
                return;
            }

            try {
                int projectId = Integer.parseInt(idParam);
                Project project = projectService.getProject(projectId);
                
                if (project == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                    return;
                }

                
                boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
                if (!isAdmin && project.getOwnerId() != currentUser.getId()) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, 
                        "Only project owner can edit the project");
                    return;
                }

                req.setAttribute("project", project);
                req.getRequestDispatcher("/WEB-INF/views/edit_project.jsp").forward(req, resp);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Ошибка при загрузке проекта");
            }
            return;
        }

        
        try {
            
            List<Project> projects = "ADMIN".equalsIgnoreCase(currentUser.getRole())
                    ? projectService.getAllProjects()
                    : projectService.getProjectsForUser(currentUser.getId());

            req.setAttribute("projects", projects);
            req.getRequestDispatcher("/WEB-INF/views/projects.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при загрузке проектов");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (CsrfUtil.rejectIfInvalid(req, resp)) {
            return;
        }
        
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");

        
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String pathInfo = req.getPathInfo();
        
        
        if (pathInfo != null && pathInfo.equals("/new")) {
            String name = req.getParameter("name");
            String description = req.getParameter("description");

            try {
                
                int newProjectId = projectService.createProject(
                    currentUser.getId(), 
                    name, 
                    description
                );
                
                
                resp.sendRedirect(req.getContextPath() + "/project?id=" + newProjectId);
            } catch (IllegalArgumentException e) {
                
                resp.sendRedirect(req.getContextPath() + "/projects/new?error=" + 
                    java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/projects/new?error=2");
            }
            return;
        }

        
        if (pathInfo != null && pathInfo.equals("/edit")) {
            String idParam = req.getParameter("id");
            String name = req.getParameter("name");
            String description = req.getParameter("description");

            if (idParam == null || idParam.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
                return;
            }

            try {
                int projectId = Integer.parseInt(idParam);
                Project project = projectService.getProject(projectId);
                
                if (project == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                    return;
                }

                
                boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
                if (!isAdmin && project.getOwnerId() != currentUser.getId()) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, 
                        "Only project owner can edit the project");
                    return;
                }

                
                project.setName(name);
                project.setDescription(description);
                projectService.updateProject(project);

                
                resp.sendRedirect(req.getContextPath() + "/project?id=" + projectId);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
            } catch (IllegalArgumentException e) {
                
                resp.sendRedirect(req.getContextPath() + "/projects/edit?id=" + idParam + "&error=" + 
                    java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/projects/edit?id=" + idParam + "&error=2");
            }
            return;
        }

        
        if (pathInfo != null && pathInfo.equals("/delete")) {
            String idParam = req.getParameter("id");

            if (idParam == null || idParam.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
                return;
            }

            try {
                int projectId = Integer.parseInt(idParam);
                Project project = projectService.getProject(projectId);
                
                if (project == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                    return;
                }

                
                boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
                if (!isAdmin && project.getOwnerId() != currentUser.getId()) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, 
                        "Only project owner can delete the project");
                    return;
                }

                
                projectService.deleteProject(projectId);

                
                resp.sendRedirect(req.getContextPath() + "/projects");
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Ошибка при удалении проекта");
            }
            return;
        }

        
        resp.sendRedirect(req.getContextPath() + "/projects");
    }
}

