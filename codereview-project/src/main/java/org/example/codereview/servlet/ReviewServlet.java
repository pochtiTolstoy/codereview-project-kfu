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
import java.util.Map;
import java.util.HashMap;

import org.example.codereview.model.User;
import org.example.codereview.model.Project;
import org.example.codereview.model.CodeReview;
import org.example.codereview.model.ProjectFile;
import org.example.codereview.service.ProjectService;
import org.example.codereview.service.ReviewService;
import org.example.codereview.service.CommentService;
import org.example.codereview.dao.ProjectFileDAO;
import org.example.codereview.dao.ReviewFileDAO;
import org.example.codereview.model.ReviewFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.example.codereview.util.CsrfUtil;

@WebServlet("/reviews/*")
public class ReviewServlet extends HttpServlet {
    private ReviewService reviewService;
    private ProjectService projectService;
    private CommentService commentService;
    private ProjectFileDAO projectFileDAO;
    private ReviewFileDAO reviewFileDAO;

    @Override
    public void init() throws ServletException {
        reviewService = (ReviewService) getServletContext().getAttribute("reviewService");
        projectService = (ProjectService) getServletContext().getAttribute("projectService");
        commentService = (CommentService) getServletContext().getAttribute("commentService");
        Object pfd = getServletContext().getAttribute("projectFileDAO");
        Object rfd = getServletContext().getAttribute("reviewFileDAO");
        if (reviewService == null || projectService == null || commentService == null || !(pfd instanceof ProjectFileDAO) || !(rfd instanceof ReviewFileDAO)) {
            throw new ServletException("Services/DAOs not initialized in ServletContext");
        }
        projectFileDAO = (ProjectFileDAO) pfd;
        reviewFileDAO = (ReviewFileDAO) rfd;
    }

    private String toJsonString(String value) {
        if (value == null) {
            return "null";
        }
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        return "\"" + escaped + "\"";
    }

    private boolean wantsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

    private void writeJsonError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String json = String.format("{\"error\":%s}", toJsonString(message));
        resp.getWriter().write(json);
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

        
        if (pathInfo != null && pathInfo.equals("/edit")) {
            String idParam = req.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Review ID is required");
                return;
            }

            try {
                int reviewId = Integer.parseInt(idParam);
                CodeReview review = reviewService.getReview(reviewId);

                if (review == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Review not found");
                    return;
                }

                
                boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
                if (!isAdmin && review.getAuthorId() != currentUser.getId()) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Only review author can edit the review");
                    return;
                }

                
                Project project = projectService.getProject(review.getProjectId());
                req.setAttribute("review", review);
                req.setAttribute("project", project);
                req.getRequestDispatcher("/WEB-INF/views/edit_review.jsp").forward(req, resp);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid review ID format");
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Ошибка при загрузке ревью");
            }
            return;
        }

        
        if (pathInfo != null && pathInfo.equals("/new")) {
            
            String projectIdParam = req.getParameter("projectId");
            if (projectIdParam == null || projectIdParam.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
                return;
            }

            try {
                int projectId = Integer.parseInt(projectIdParam);

                
                if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())
                        && !projectService.isUserMember(currentUser.getId(), projectId)) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "You are not a member of this project");
                    return;
                }

                
                Project project = projectService.getProject(projectId);
                if (project == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                    return;
                }

                
                String fileNameParam = req.getParameter("fileName");
                String prefilledContent = null;
                String effectiveFileName = null;

                if (fileNameParam != null && !fileNameParam.isBlank()) {
                    String trimmedName = fileNameParam.trim();
                    
                    ProjectFile pf = projectFileDAO.findByProjectAndName(projectId, trimmedName);
                    if (pf != null) {
                        effectiveFileName = pf.getFilename();
                        prefilledContent = pf.getContent();
                    } else {
                        
                        effectiveFileName = trimmedName;
                    }
                }

                
                req.setAttribute("project", project);
                req.setAttribute("fileName", effectiveFileName);
                req.setAttribute("fileContent", prefilledContent);

                
                req.getRequestDispatcher("/WEB-INF/views/new_review.jsp").forward(req, resp);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Ошибка при загрузке проекта");
            }
            return;
        }

        
        if (pathInfo != null && pathInfo.equals("/file/new")) {
            String reviewIdParam = req.getParameter("reviewId");
            String fileNameParam = req.getParameter("fileName"); 

            if (reviewIdParam == null || reviewIdParam.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "reviewId is required");
                return;
            }

            try {
                int reviewId = Integer.parseInt(reviewIdParam);

                CodeReview review = reviewService.getReview(reviewId);
                if (review == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Review not found");
                    return;
                }

                if (!reviewService.canEditCurrentRevision(review)) {
                    String message = URLEncoder.encode("Создайте новую ревизию перед изменениями", StandardCharsets.UTF_8);
                    resp.sendRedirect(req.getContextPath() + "/review?id=" + reviewId + "&error=" + message);
                    return;
                }

                
                Project project = projectService.getProject(review.getProjectId());
                if (project == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                    return;
                }

                
                if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())
                        && !projectService.isUserMember(currentUser.getId(), project.getId())) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "You are not a member of this project");
                    return;
                }

                
                String prefilledContent = null;
                String effectiveFileName = null;
                boolean editingExistingFile = false;

                if (fileNameParam != null && !fileNameParam.isBlank()) {
                    String trimmedName = fileNameParam.trim();
                    ReviewFile latestFile = reviewFileDAO.findByReviewRevisionAndFilename(
                            review.getId(), review.getCurrentRevisionNumber(), trimmedName);
                    if (latestFile != null) {
                        effectiveFileName = latestFile.getFilename();
                        prefilledContent = latestFile.getContent();
                        editingExistingFile = true;
                    } else {
                        ProjectFile pf = projectFileDAO.findByProjectAndName(project.getId(), trimmedName);
                        if (pf != null) {
                            effectiveFileName = pf.getFilename();
                            prefilledContent = pf.getContent();
                        } else {
                            effectiveFileName = trimmedName; 
                        }
                    }
                }

                req.setAttribute("review", review);
                req.setAttribute("project", project);
                req.setAttribute("fileName", effectiveFileName);
                req.setAttribute("fileContent", prefilledContent);
                req.setAttribute("editingExistingFile", editingExistingFile);

                
                req.getRequestDispatcher("/WEB-INF/views/add_review_file.jsp").forward(req, resp);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid reviewId format");
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Ошибка при подготовке формы добавления файла");
            }
            return;
        }

        
        String projectIdParam = req.getParameter("projectId");
        if (projectIdParam == null || projectIdParam.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
            return;
        }

        try {
            int projectId = Integer.parseInt(projectIdParam);

            
            if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())
                    && !projectService.isUserMember(currentUser.getId(), projectId)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "You are not a member of this project");
                return;
            }

            
            Project project = projectService.getProject(projectId);
            if (project == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                return;
            }

            
            List<CodeReview> reviews = reviewService.getReviewsByProject(projectId);
            Map<Integer, String> authorNames = new HashMap<>();
            for (CodeReview r : reviews) {
                if (!authorNames.containsKey(r.getAuthorId())) {
                    User u = reviewService.getUserById(r.getAuthorId());
                    if (u != null) {
                        authorNames.put(r.getAuthorId(), u.getUsername());
                    }
                }
            }

            
            req.setAttribute("project", project);
            req.setAttribute("reviews", reviews);
            req.setAttribute("authorNames", authorNames);

            
            req.getRequestDispatcher("/WEB-INF/views/reviews.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Ошибка при загрузке ревью");
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

        
        if (pathInfo != null && pathInfo.equals("/comment")) {
            boolean wantsJson = wantsJson(req);
            String reviewIdParam = req.getParameter("reviewId");
            String reviewFileIdParam = req.getParameter("reviewFileId");
            String lineNumberParam = req.getParameter("lineNumber");
            String content = req.getParameter("content");
            String parentIdParam = req.getParameter("parentId");

            if (reviewIdParam == null || reviewIdParam.isEmpty()) {
                if (wantsJson) {
                    writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Review ID is required");
                } else {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Review ID is required");
                }
                return;
            }

            try {
                int reviewId = Integer.parseInt(reviewIdParam);

                Integer reviewFileId = null;
                if (reviewFileIdParam != null && !reviewFileIdParam.isEmpty()) {
                    reviewFileId = Integer.valueOf(reviewFileIdParam);
                }

                Integer lineNumber = null;
                if (lineNumberParam != null && !lineNumberParam.isEmpty()) {
                    lineNumber = Integer.valueOf(lineNumberParam);
                }

                Integer parentId = null;
                if (parentIdParam != null && !parentIdParam.isEmpty()) {
                    parentId = Integer.valueOf(parentIdParam);
                }

                
                int newId = commentService.addComment(currentUser, reviewId, reviewFileId, lineNumber, content, parentId);

                if (wantsJson) {
                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");
                    String json = String.format(
                            "{\"id\":%d,\"authorId\":%d,\"authorName\":%s,\"content\":%s,\"parentId\":%s,\"reviewFileId\":%s,\"lineNumber\":%s}",
                            newId,
                            currentUser.getId(),
                            toJsonString(currentUser.getUsername()),
                            toJsonString(content),
                            parentId != null ? parentId : "null",
                            reviewFileId != null ? reviewFileId : "null",
                            lineNumber != null ? lineNumber : "null"
                    );
                    resp.getWriter().write(json);
                } else {
                    
                    resp.sendRedirect(req.getContextPath() + "/review?id=" + reviewId);
                }
            } catch (NumberFormatException e) {
                if (wantsJson) {
                    writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
                } else {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
                }
            } catch (IllegalArgumentException e) {
                if (wantsJson) {
                    writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                } else {
                    resp.sendRedirect(
                        req.getContextPath()
                        + "/review?id=" + reviewIdParam
                        + "&error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8")
                    );
                }
            } catch (SecurityException e) {
                
                if (wantsJson) {
                    writeJsonError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
                } else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                if (wantsJson) {
                    writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "comment_db_error");
                } else {
                    resp.sendRedirect(
                        req.getContextPath()
                        + "/review?id=" + reviewIdParam
                        + "&error=comment_db_error"
                    );
                }
            }
            return;
        }

        
        if (pathInfo != null && pathInfo.equals("/comment/delete")) {
            String commentIdParam = req.getParameter("commentId");
            String reviewIdParam = req.getParameter("reviewId");

            if (commentIdParam == null || commentIdParam.isEmpty()
                    || reviewIdParam == null || reviewIdParam.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "commentId and reviewId are required");
                return;
            }

            try {
                int commentId = Integer.parseInt(commentIdParam);
                int reviewId = Integer.parseInt(reviewIdParam);

                
                commentService.deleteComment(currentUser, commentId);

                
                resp.sendRedirect(req.getContextPath() + "/review?id=" + reviewId);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
            } catch (IllegalArgumentException e) {
                resp.sendRedirect(
                    req.getContextPath()
                    + "/review?id=" + reviewIdParam
                    + "&error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8")
                );
            } catch (SecurityException e) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendRedirect(
                    req.getContextPath()
                    + "/review?id=" + reviewIdParam
                    + "&error=comment_delete_db_error"
                );
            }
            return;
        }

        
        if (pathInfo != null && pathInfo.equals("/file/new")) {
            String reviewIdParam = req.getParameter("reviewId");
            String fileName = req.getParameter("fileName");
            String fileContent = req.getParameter("fileContent");

            if (reviewIdParam == null || reviewIdParam.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "reviewId is required");
                return;
            }

            try {
                int reviewId = Integer.parseInt(reviewIdParam);

                CodeReview review = reviewService.getReview(reviewId);
                if (review == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Review not found");
                    return;
                }

                Project project = projectService.getProject(review.getProjectId());
                if (project == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                    return;
                }

                
                boolean isAuthor = (review.getAuthorId() == currentUser.getId());
                boolean isProjectOwner = (project.getOwnerId() == currentUser.getId());
                boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());

                if (!isAuthor && !isProjectOwner && !isAdmin) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "You are not allowed to attach files to this review");
                    return;
                }

                if (fileName == null || fileName.isBlank()) {
                    throw new IllegalArgumentException("File name cannot be empty");
                }

                
                if (fileContent == null) {
                    fileContent = ""; 
                }

                reviewService.addFileToReview(currentUser, reviewId, fileName, fileContent);

                
                resp.sendRedirect(req.getContextPath() + "/review?id=" + reviewId);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid reviewId format");
            } catch (IllegalArgumentException e) {
                resp.sendRedirect(req.getContextPath() + "/reviews/file/new?reviewId=" + reviewIdParam
                        + "&error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
            } catch (SecurityException e) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/reviews/file/new?reviewId=" + reviewIdParam
                        + "&error=SQL");
            }
            return;
        }

        
        if (pathInfo != null && pathInfo.equals("/edit")) {
            String idParam = req.getParameter("id");
            String title = req.getParameter("title");
            String content = req.getParameter("content");

            if (idParam == null || idParam.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Review ID is required");
                return;
            }

            try {
                int reviewId = Integer.parseInt(idParam);
                CodeReview review = reviewService.getReview(reviewId);

                if (review == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Review not found");
                    return;
                }

                
                boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
                if (!isAdmin && review.getAuthorId() != currentUser.getId()) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Only review author can edit the review");
                    return;
                }

                
                review.setTitle(title);
                review.setContent(content);
                reviewService.updateReview(review);

                
                resp.sendRedirect(req.getContextPath() + "/review?id=" + reviewId);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid review ID format");
            } catch (IllegalArgumentException e) {
                
                resp.sendRedirect(req.getContextPath() + "/reviews/edit?id=" + idParam + "&error=" +
                    java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/reviews/edit?id=" + idParam + "&error=2");
            }
            return;
        }

        
        if (pathInfo != null && pathInfo.equals("/delete")) {
            String idParam = req.getParameter("id");

            if (idParam == null || idParam.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Review ID is required");
                return;
            }

            try {
                int reviewId = Integer.parseInt(idParam);
                CodeReview review = reviewService.getReview(reviewId);

                if (review == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Review not found");
                    return;
                }

                
                boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
                if (!isAdmin && review.getAuthorId() != currentUser.getId()) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Only review author can delete the review");
                    return;
                }

                int projectId = review.getProjectId();

                
                reviewService.deleteReview(reviewId);

                
                resp.sendRedirect(req.getContextPath() + "/reviews?projectId=" + projectId);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid review ID format");
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Ошибка при удалении ревью");
            }
            return;
        }

        
        if (pathInfo != null && pathInfo.equals("/new")) {
            String projectIdParam = req.getParameter("projectId");
            String title = req.getParameter("title");
            String content = req.getParameter("content");

            
            String filename = req.getParameter("filename");
            String fileContent = req.getParameter("fileContent");

            if (projectIdParam == null || projectIdParam.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
                return;
            }

            try {
                int projectId = Integer.parseInt(projectIdParam);

                
                int newReviewId = reviewService.createReview(
                    currentUser.getId(),
                    projectId,
                    title,
                    content
                );

                
                if (filename != null && !filename.trim().isEmpty()
                        && fileContent != null && !fileContent.trim().isEmpty()) {

                    try {
                        reviewService.addFileToReview(currentUser, newReviewId, filename, fileContent);
                    } catch (IllegalArgumentException e) {
                        
                        
                        e.printStackTrace();
                    }
                }

                
                resp.sendRedirect(req.getContextPath() + "/review?id=" + newReviewId);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
            } catch (IllegalArgumentException e) {
                
                resp.sendRedirect(req.getContextPath() + "/reviews/new?projectId=" +
                    projectIdParam + "&error=" +
                    java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            } catch (RuntimeException e) {
                
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/reviews/new?projectId=" +
                    projectIdParam + "&error=2");
            }
            return;
        }

        
        resp.sendRedirect(req.getContextPath() + "/projects");
    }
}
