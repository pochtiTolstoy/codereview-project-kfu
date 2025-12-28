package org.example.codereview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

import org.example.codereview.model.User;
import org.example.codereview.model.Project;
import org.example.codereview.model.CodeReview;
import org.example.codereview.model.Comment;
import org.example.codereview.model.ReviewFile;
import org.example.codereview.model.ProjectFile;
import org.example.codereview.model.DiffLine;
import org.example.codereview.model.CommentThread;
import org.example.codereview.model.CommentLineInfo;
import org.example.codereview.model.ReviewVote;
import org.example.codereview.model.ReviewVoteLabel;
import org.example.codereview.service.ProjectService;
import org.example.codereview.service.ReviewService;
import org.example.codereview.service.CommentService;
import org.example.codereview.dao.UserDAO;
import org.example.codereview.dao.ProjectFileDAO;
import org.example.codereview.util.DiffUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@WebServlet("/review")
public class ReviewDetailServlet extends HttpServlet {
    private ReviewService reviewService;
    private ProjectService projectService;
    private CommentService commentService;
    private UserDAO userDAO;
    private ProjectFileDAO projectFileDAO;

    @Override
    public void init() throws ServletException {
        reviewService = (ReviewService) getServletContext().getAttribute("reviewService");
        projectService = (ProjectService) getServletContext().getAttribute("projectService");
        commentService = (CommentService) getServletContext().getAttribute("commentService");
        Object udao = getServletContext().getAttribute("userDAO");
        Object pdao = getServletContext().getAttribute("projectFileDAO");
        if (reviewService == null || projectService == null || commentService == null
                || !(udao instanceof UserDAO) || !(pdao instanceof ProjectFileDAO)) {
            throw new ServletException("Services/DAOs not initialized in ServletContext");
        }
        userDAO = (UserDAO) udao;
        projectFileDAO = (ProjectFileDAO) pdao;
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

            
            Project project = projectService.getProject(review.getProjectId());
            if (project == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                return;
            }

            
            boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
            if (!isAdmin && !projectService.isUserMember(currentUser.getId(), review.getProjectId())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "You are not a member of this project");
                return;
            }

            int currentRevision = review.getCurrentRevisionNumber();
            List<Integer> revisions = reviewService.getRevisionNumbers(reviewId);
            if (!revisions.contains(currentRevision)) {
                revisions.add(currentRevision);
            }
            revisions.sort(Integer::compareTo);

            int selectedRevision = currentRevision;
            String revParam = req.getParameter("rev");
            if (revParam != null) {
                try {
                    int requested = Integer.parseInt(revParam);
                    if (requested >= 1 && revisions.contains(requested)) {
                        selectedRevision = requested;
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            int selectedBaseRevision = selectedRevision > 1 ? selectedRevision - 1 : 0;
            String baseParam = req.getParameter("baseRev");
            if (baseParam != null) {
                try {
                    int requestedBase = Integer.parseInt(baseParam);
                    if (requestedBase == 0) {
                        selectedBaseRevision = 0;
                    } else if (requestedBase >= 1
                            && requestedBase < selectedRevision
                            && revisions.contains(requestedBase)) {
                        selectedBaseRevision = requestedBase;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            if (selectedBaseRevision >= selectedRevision && selectedRevision > 1) {
                selectedBaseRevision = selectedRevision - 1;
            }
            if (selectedBaseRevision == selectedRevision) {
                selectedBaseRevision = selectedRevision > 1 ? selectedRevision - 1 : 0;
            }

            List<Comment> comments = commentService.getCommentsForReview(reviewId);

            List<ReviewFile> reviewFiles = reviewService.getFilesForRevision(reviewId, selectedRevision);
            List<ReviewFile> latestReviewFiles = reviewService.getFilesForRevision(reviewId, currentRevision);
            Map<Integer, ReviewFile> filesByIdForSelected = new LinkedHashMap<>();
            Map<String, ReviewFile> filesByNameForSelected = new LinkedHashMap<>();
            for (ReviewFile rf : reviewFiles) {
                filesByIdForSelected.put(rf.getId(), rf);
                filesByNameForSelected.put(rf.getFilename(), rf);
            }

            Map<Integer, Map<Integer, List<CommentThread>>> commentThreadsByFile = new LinkedHashMap<>();
            List<CommentThread> generalCommentThreads = new ArrayList<>();
            List<CommentThread> legacyCommentThreads = new ArrayList<>();
            Map<Integer, CommentThread> threadById = new LinkedHashMap<>();
            List<Comment> repliesBuffer = new ArrayList<>();
            boolean hasUnresolvedThreads = false;
            int unresolvedCount = 0;
            boolean hasOwnUnresolvedThreads = false;
            

            for (Comment comment : comments) {
                boolean outdated = comment.getRevisionNumber() < currentRevision;
                if (comment.getParentId() == null) {
                    CommentThread thread = new CommentThread();
                    thread.setRoot(comment);
                    thread.setOutdated(outdated);
                    boolean canResolve = currentUser != null
                            && (isAdmin || currentUser.getId() == comment.getAuthorId());
                    thread.setCanResolve(canResolve);
                    threadById.put(comment.getId(), thread);
                    if (!comment.isResolved() && !outdated) {
                        hasUnresolvedThreads = true;
                        unresolvedCount++;
                    }
                    if (!comment.isResolved()
                            && currentUser != null
                            && comment.getAuthorId() == currentUser.getId()) {
                        hasOwnUnresolvedThreads = true;
                    }

                    Integer displayFileId = null;
                    Integer rawFileId = comment.getReviewFileId();
                    if (rawFileId != null && filesByIdForSelected.containsKey(rawFileId)) {
                        displayFileId = rawFileId;
                    } else if (comment.getReviewFileName() != null) {
                        ReviewFile mapped = filesByNameForSelected.get(comment.getReviewFileName());
                        if (mapped != null) {
                            displayFileId = mapped.getId();
                        }
                    }

                    if (displayFileId != null) {
                        Map<Integer, List<CommentThread>> byLine = commentThreadsByFile
                                .computeIfAbsent(displayFileId, k -> new LinkedHashMap<>());
                        Integer lineKey = comment.getLineNumber();
                        byLine.computeIfAbsent(lineKey, k -> new ArrayList<>()).add(thread);
                    } else if (comment.getReviewFileId() != null) {
                        legacyCommentThreads.add(thread);
                    } else {
                        generalCommentThreads.add(thread);
                    }
                } else {
                    repliesBuffer.add(comment);
                }
            }

            for (Comment reply : repliesBuffer) {
                if (reply.getParentId() == null) {
                    continue;
                }
                CommentThread thread = threadById.get(reply.getParentId());
                if (thread != null) {
                    thread.addReply(reply);
                }
            }

            Map<String, String> baseContents = new LinkedHashMap<>();
            List<ProjectFile> projectFiles = projectFileDAO.findByProjectId(project.getId());
            if (selectedBaseRevision == 0) {
                
                List<ReviewFile> baseSnapshot = reviewService.getFilesForRevision(reviewId, 0);
                if (baseSnapshot != null && !baseSnapshot.isEmpty()) {
                    for (ReviewFile bf : baseSnapshot) {
                        baseContents.put(bf.getFilename(), bf.getContent());
                    }
                } else {
                    
                    for (ProjectFile pf : projectFiles) {
                        baseContents.put(pf.getFilename(), pf.getContent());
                    }
                }
            } else {
                List<ReviewFile> baseFiles = reviewService.getFilesForRevision(reviewId, selectedBaseRevision);
                for (ReviewFile bf : baseFiles) {
                    baseContents.put(bf.getFilename(), bf.getContent());
                }
            }

            Map<Integer, List<DiffLine>> diffMap = new LinkedHashMap<>();
            for (ReviewFile rf : reviewFiles) {
                String baseContent = baseContents.getOrDefault(rf.getFilename(), "");
                List<DiffLine> diffLines = DiffUtil.diff(
                        splitLines(baseContent),
                        splitLines(rf.getContent()));
                diffMap.put(rf.getId(), diffLines);
            }

            
            User author = userDAO.findById(review.getAuthorId());

            
            List<User> reviewers = reviewService.getReviewers(reviewId);

            
            List<ReviewVote> votes = reviewService.getVotesForReview(reviewId);
            Map<Integer, Integer> codeReviewVotes = new HashMap<>();
            for (ReviewVote vote : votes) {
                if (vote.getLabel() == ReviewVoteLabel.CODE_REVIEW) {
                    codeReviewVotes.put(vote.getUserId(), vote.getValue());
                }
            }
            boolean hasBlockingCodeReview = codeReviewVotes.values().stream().anyMatch(val -> val < 0);
            int maxCodeReview = codeReviewVotes.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            boolean allReviewersApproved = reviewers.isEmpty();
            if (!reviewers.isEmpty()) {
                allReviewersApproved = true;
                for (User reviewer : reviewers) {
                    Integer value = codeReviewVotes.get(reviewer.getId());
                    if (value == null || value < 1) {
                        allReviewersApproved = false;
                        break;
                    }
                }
            }

            
            Map<Integer, Map<Integer, CommentLineInfo>> commentInfoByFileAndLine = new LinkedHashMap<>();
            for (Map.Entry<Integer, Map<Integer, List<CommentThread>>> fileEntry : commentThreadsByFile.entrySet()) {
                Integer fileId = fileEntry.getKey();
                Map<Integer, List<CommentThread>> byLine = fileEntry.getValue();
                
                Map<Integer, CommentLineInfo> lineMap = new LinkedHashMap<>();
                for (Map.Entry<Integer, List<CommentThread>> lineEntry : byLine.entrySet()) {
                    Integer lineNumber = lineEntry.getKey();
                    List<CommentThread> threads = lineEntry.getValue();

                    boolean hasAny = threads.stream().anyMatch(t -> !t.isOutdated());
                    boolean hasUnresolved = threads.stream().anyMatch(t -> !t.isOutdated() && !t.isResolved());

                    Integer firstThreadId = null;
                    for (CommentThread thread : threads) {
                        if (thread.isOutdated()) {
                            continue;
                        }
                        if (!thread.isResolved()) {
                            firstThreadId = thread.getRoot().getId();
                            break;
                        }
                        if (firstThreadId == null) {
                            firstThreadId = thread.getRoot().getId();
                        }
                    }

                    lineMap.put(lineNumber, new CommentLineInfo(hasAny, hasUnresolved, firstThreadId));
                }
                
                commentInfoByFileAndLine.put(fileId, lineMap);
            }

            
            Set<Integer> authorIds = new HashSet<>();
            for (CommentThread thread : generalCommentThreads) {
                if (thread.getRoot() != null) {
                    authorIds.add(thread.getRoot().getAuthorId());
                }
                if (thread.getReplies() != null) {
                    thread.getReplies().forEach(c -> authorIds.add(c.getAuthorId()));
                }
            }
            for (Map<Integer, List<CommentThread>> byLine : commentThreadsByFile.values()) {
                for (List<CommentThread> threads : byLine.values()) {
                    for (CommentThread t : threads) {
                        if (t.getRoot() != null) {
                            authorIds.add(t.getRoot().getAuthorId());
                        }
                        if (t.getReplies() != null) {
                            t.getReplies().forEach(c -> authorIds.add(c.getAuthorId()));
                        }
                    }
                }
            }
            Map<Integer, String> authorNames = new HashMap<>();
            for (Integer id : authorIds) {
                User u = userDAO.findById(id);
                if (u != null) {
                    authorNames.put(id, u.getUsername());
                }
            }

            
            req.setAttribute("project", project);
            req.setAttribute("review", review);
            req.setAttribute("comments", comments);
            req.setAttribute("commentThreadsByFile", commentThreadsByFile);
            req.setAttribute("commentInfoByFileAndLine", commentInfoByFileAndLine);
            req.setAttribute("generalCommentThreads", generalCommentThreads);
            req.setAttribute("legacyCommentThreads", legacyCommentThreads);
            req.setAttribute("hasUnresolvedComments", hasUnresolvedThreads);
            req.setAttribute("unresolvedCount", unresolvedCount);
            req.setAttribute("hasOwnUnresolvedThreads", hasOwnUnresolvedThreads);
            req.setAttribute("reviewFiles", reviewFiles);
            req.setAttribute("currentRevisionFiles", latestReviewFiles);
            req.setAttribute("projectFiles", projectFiles);
            req.setAttribute("diffMap", diffMap);
            req.setAttribute("currentRevision", currentRevision);
            req.setAttribute("selectedRevision", selectedRevision);
            req.setAttribute("selectedBaseRevision", selectedBaseRevision);
            req.setAttribute("revisions", revisions);
            req.setAttribute("baseRevisionLabel",
                    selectedBaseRevision == 0 ? "Базовый код проекта" : ("Ревизия #" + selectedBaseRevision));
            req.setAttribute("baseContents", baseContents);
            req.setAttribute("canEditCurrentRevision", reviewService.canEditCurrentRevision(review));
            req.setAttribute("lockedRevisionNumber", review.getLockedRevisionNumber());
            req.setAttribute("authorNames", authorNames);

            boolean isAuthor = review.getAuthorId() == currentUser.getId();
            boolean isProjectOwner = project.getOwnerId() == currentUser.getId();
            boolean isReviewer = isAdmin
                    || reviewers.stream().anyMatch(u -> u.getId() == currentUser.getId());
            boolean canManageReviewers = isAuthor || isProjectOwner || isAdmin;

            req.setAttribute("isAuthor", isAuthor);
            req.setAttribute("isProjectOwner", isProjectOwner);
            req.setAttribute("isAdmin", isAdmin);
            req.setAttribute("isReviewer", isReviewer);
            req.setAttribute("canManageReviewers", canManageReviewers);
            req.setAttribute("author", author);
            req.setAttribute("reviewers", reviewers);
            req.setAttribute("codeReviewVotes", codeReviewVotes);
            req.setAttribute("hasBlockingCodeReview", hasBlockingCodeReview);
            req.setAttribute("maxCodeReview", maxCodeReview);
            req.setAttribute("allReviewersApproved", allReviewersApproved);
            req.setAttribute("currentUser", currentUser);
            req.setAttribute("authorNames", authorNames);

            
            req.getRequestDispatcher("/WEB-INF/views/review.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid review ID format");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Ошибка при загрузке ревью");
        }
    }

    private List<String> splitLines(String content) {
        List<String> lines = new ArrayList<>();
        if (content == null) {
            return lines;
        }
        String[] array = content.split("\n", -1);
        for (String line : array) {
            lines.add(line);
        }
        return lines;
    }
}

