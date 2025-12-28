package org.example.codereview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.example.codereview.dao.UserDAO;
import org.example.codereview.dao.ProjectDAO;
import org.example.codereview.model.User;
import org.example.codereview.model.Project;
import org.example.codereview.service.ProjectService;
import org.example.codereview.util.CsrfUtil;

@WebServlet("/admin/project-members")
public class AdminProjectMembersServlet extends HttpServlet {

    private UserDAO userDAO;
    private ProjectDAO projectDAO;
    private ProjectService projectService;

    @Override
    public void init() throws ServletException {
        Object udao = getServletContext().getAttribute("userDAO");
        Object pdao = getServletContext().getAttribute("projectDAO");
        Object ps = getServletContext().getAttribute("projectService");
        if (!(udao instanceof UserDAO) || !(pdao instanceof ProjectDAO) || !(ps instanceof ProjectService)) {
            throw new ServletException("AdminProjectMembersServlet: services/daos not initialized");
        }
        userDAO = (UserDAO) udao;
        projectDAO = (ProjectDAO) pdao;
        projectService = (ProjectService) ps;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String projectIdParam = req.getParameter("projectId");
        if (projectIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "projectId required");
            return;
        }

        try {
            int projectId = Integer.parseInt(projectIdParam);

            Project project = projectDAO.findById(projectId);
            if (project == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                return;
            }

            boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
            boolean isOwner = project.getOwnerId() == currentUser.getId();

            if (!isAdmin && !isOwner) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }

            List<User> members = projectDAO.findMembers(projectId);
            List<User> allUsers = userDAO.findAll();

            req.setAttribute("project", project);
            req.setAttribute("members", members);
            req.setAttribute("users", allUsers);

            req.getRequestDispatcher("/WEB-INF/views/admin_project_members.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid format");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SQL error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;
        if (CsrfUtil.rejectIfInvalid(req, resp)) {
            return;
        }

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        String projectIdParam = req.getParameter("projectId");

        if (projectIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "projectId required");
            return;
        }

        try {
            int projectId = Integer.parseInt(projectIdParam);

            Project project = projectDAO.findById(projectId);
            if (project == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                return;
            }

            boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
            boolean isOwner = project.getOwnerId() == currentUser.getId();

            if (!isAdmin && !isOwner) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }

            if ("add".equals(action)) {
                int userId = Integer.parseInt(req.getParameter("userId"));
                projectService.addMember(userId, projectId);
            } else if ("remove".equals(action)) {
                int userId = Integer.parseInt(req.getParameter("userId"));
                projectService.removeMember(userId, projectId);
            }

            resp.sendRedirect(req.getContextPath() + "/admin/project-members?projectId=" + projectId);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(
                req.getContextPath()
                + "/admin/project-members?projectId=" + projectIdParam
                + "&error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8")
            );
        }
    }
}

