package org.example.codereview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import org.example.codereview.model.User;
import org.example.codereview.service.ReviewService;
import org.example.codereview.util.CsrfUtil;

@WebServlet("/review/revision")
public class ReviewRevisionServlet extends HttpServlet {

    private ReviewService reviewService;

    @Override
    public void init() throws ServletException {
        reviewService = (ReviewService) getServletContext().getAttribute("reviewService");
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

        String reviewIdParam = req.getParameter("reviewId");
        if (reviewIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "reviewId is required");
            return;
        }

        try {
            int reviewId = Integer.parseInt(reviewIdParam);
            reviewService.startNewRevision(currentUser, reviewId);
            resp.sendRedirect(req.getContextPath() + "/review?id=" + reviewId + "&info=revision-started");
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid reviewId");
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            String error = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/review?id=" + reviewIdParam + "&error=" + error);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/review?id=" + reviewIdParam + "&error=revision_error");
        }
    }
}

