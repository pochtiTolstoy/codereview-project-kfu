package org.example.codereview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import org.example.codereview.model.User;
import org.example.codereview.service.CommentService;
import org.example.codereview.util.CsrfUtil;

@WebServlet("/comments/resolve")
public class CommentResolveServlet extends HttpServlet {

    private CommentService commentService;

    @Override
    public void init() throws ServletException {
        Object svc = getServletContext().getAttribute("commentService");
        if (!(svc instanceof CommentService)) {
            throw new ServletException("CommentService not initialized in ServletContext");
        }
        commentService = (CommentService) svc;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

        String rootIdParam = req.getParameter("rootId");
        String reviewIdParam = req.getParameter("reviewId");
        String action = req.getParameter("action");

        if (rootIdParam == null || reviewIdParam == null || action == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        try {
            int rootId = Integer.parseInt(rootIdParam);
            int reviewId = Integer.parseInt(reviewIdParam);

            if ("resolve".equalsIgnoreCase(action)) {
                commentService.resolveThread(currentUser, rootId);
            } else if ("reopen".equalsIgnoreCase(action)) {
                commentService.reopenThread(currentUser, rootId);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/review?id=" + reviewId + "#comment-" + rootId);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (IllegalArgumentException e) {
            resp.sendRedirect(
                    req.getContextPath()
                            + "/review?id=" + reviewIdParam
                            + "&error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(
                    req.getContextPath()
                            + "/review?id=" + reviewIdParam
                            + "&error=comment_resolve_error");
        }
    }
}


