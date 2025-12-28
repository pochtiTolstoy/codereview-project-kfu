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

import org.example.codereview.dao.UserDAO;
import org.example.codereview.dao.ProjectDAO;
import org.example.codereview.model.User;
import org.example.codereview.model.Project;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private UserDAO userDAO;
    private ProjectDAO projectDAO;

    @Override
    public void init() throws ServletException {
        Object udao = getServletContext().getAttribute("userDAO");
        Object pdao = getServletContext().getAttribute("projectDAO");
        if (!(udao instanceof UserDAO) || !(pdao instanceof ProjectDAO)) {
            throw new ServletException("AdminServlet: DAOs not initialized");
        }
        userDAO = (UserDAO) udao;
        projectDAO = (ProjectDAO) pdao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User currentUser = (session != null)
                ? (User) session.getAttribute("user")
                : null;

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access denied: ADMIN only");
            return;
        }

        try {
            List<User> users = userDAO.findAll();
            List<Project> projects = projectDAO.findAll();

            req.setAttribute("users", users);
            req.setAttribute("projects", projects);

            req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Ошибка при загрузке данных для админ-панели");
        }
    }
}

