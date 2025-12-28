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

@WebServlet("/review/reviewers")
public class ReviewReviewerServlet extends HttpServlet {

    private ReviewService reviewService;

    @Override
    public void init() throws ServletException {
        reviewService = (ReviewService) getServletContext().getAttribute("reviewService");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid review ID");
            return;
        }

        String redirectUrl = req.getContextPath() + "/review?id=" + reviewId;

        try {
            switch (action) {
                case "add":
                    String userQuery = req.getParameter("user");
                    reviewService.addReviewer(currentUser, reviewId, userQuery);
                    redirectUrl += "&reviewerInfo=" + encode("Ревьюер добавлен");
                    break;
                case "remove":
                    String reviewerIdParam = req.getParameter("userId");
                    if (reviewerIdParam == null) {
                        throw new IllegalArgumentException("Не указан пользователь");
                    }
                    int reviewerUserId = Integer.parseInt(reviewerIdParam);
                    reviewService.removeReviewer(currentUser, reviewId, reviewerUserId);
                    redirectUrl += "&reviewerInfo=" + encode("Ревьюер удалён");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action: " + action);
            }
        } catch (SecurityException | IllegalArgumentException e) {
            redirectUrl += "&reviewerError=" + encode(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            redirectUrl += "&reviewerError=" + encode("Ошибка при обновлении списка ревьюеров");
        }

        resp.sendRedirect(redirectUrl);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

