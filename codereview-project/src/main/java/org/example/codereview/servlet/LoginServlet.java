package org.example.codereview.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.util.UUID;

import org.example.codereview.dao.UserDAO;
import org.example.codereview.model.User;
import org.example.codereview.util.PasswordUtil;
import org.example.codereview.util.CsrfUtil;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ensureCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (CsrfUtil.rejectIfInvalid(req, resp)) {
            return;
        }
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        User user = userDAO.findByUsername(username);

        if (user != null && PasswordUtil.hash(password).equals(user.getPassword())) {
            req.getSession().setAttribute("user", user);
            resp.sendRedirect(req.getContextPath() + "/projects");
        } else {
            resp.sendRedirect(req.getContextPath() + "/login?error=1");
        }
    }

    private void ensureCsrfToken(HttpServletRequest req) {
        Object token = req.getSession(true).getAttribute("csrf");
        if (token == null) {
            req.getSession().setAttribute("csrf", UUID.randomUUID().toString());
        }
    }
}
