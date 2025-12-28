package org.example.codereview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import org.example.codereview.model.User;
import org.example.codereview.dao.UserDAO;
import org.example.codereview.util.CsrfUtil;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        Object dao = getServletContext().getAttribute("userDAO");
        if (!(dao instanceof UserDAO)) {
            throw new ServletException("UserDAO not initialized in ServletContext");
        }
        userDAO = (UserDAO) dao;
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ensureCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (CsrfUtil.rejectIfInvalid(req, resp)) {
            return;
        }
        req.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        
        if (password == null || password.length() < 6) {
            resp.sendRedirect(req.getContextPath() + "/register?error=weak_password");
            return;
        }
        if (email == null || !email.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            resp.sendRedirect(req.getContextPath() + "/register?error=invalid_email");
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("USER"); 

        try {
            if (userDAO.existsByUsername(username)) {
                resp.sendRedirect(req.getContextPath() + "/register?error=1");
            } else {
                userDAO.insert(user);
                resp.sendRedirect(req.getContextPath() + "/login?registered=1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/register?error=2");
        }
    }

    private void ensureCsrfToken(HttpServletRequest req) {
        Object token = req.getSession(true).getAttribute("csrf");
        if (token == null) {
            req.getSession().setAttribute("csrf", UUID.randomUUID().toString());
        }
    }
}
