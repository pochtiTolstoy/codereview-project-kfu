package org.example.codereview.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.codereview.model.User;
import org.example.codereview.service.AuthService;

import java.io.IOException;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        Object svc = getServletContext().getAttribute("authService");
        if (!(svc instanceof AuthService)) {
            throw new ServletException("AuthService not initialized in ServletContext");
        }
        authService = (AuthService) svc;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("register".equals(action)) {
            String username = req.getParameter("username");
            String email = req.getParameter("email");
            String password = req.getParameter("password");

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);

            boolean success = authService.register(user);

            if (success) {
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
            } else {
                resp.sendRedirect(req.getContextPath() + "/register.jsp?error=1");
            }
        }
    }
}
