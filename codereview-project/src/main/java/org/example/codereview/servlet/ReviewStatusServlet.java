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

@WebServlet("/review/status")
public class ReviewStatusServlet extends HttpServlet {

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
        String action = req.getParameter("action");

        if (reviewIdParam == null || action == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        int reviewId;
        try {
            reviewId = Integer.parseInt(reviewIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid reviewId");
            return;
        }

        try {
            switch (action) {
                case "ready":
                    reviewService.readyForReview(currentUser, reviewId);
                    break;
                case "approve":
                    reviewService.approveReview(currentUser, reviewId);
                    break;
                case "requestChanges":
                    reviewService.requestChanges(currentUser, reviewId);
                    break;
                case "markUpdated":
                    reviewService.markUpdated(currentUser, reviewId);
                    break;
                case "close":
                    reviewService.closeReview(currentUser, reviewId);
                    break;
                case "merge":
                    reviewService.mergeReview(currentUser, reviewId);
                    break;
                case "abandon":
                    reviewService.abandonReview(currentUser, reviewId);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action: " + action);
            }
            resp.sendRedirect(req.getContextPath() + "/review?id=" + reviewId + "#review-status-actions");
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            String error = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/review?id=" + reviewId + "&error=" + error);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/review?id=" + reviewId + "&error=review_status_error");
        }
    }
}

