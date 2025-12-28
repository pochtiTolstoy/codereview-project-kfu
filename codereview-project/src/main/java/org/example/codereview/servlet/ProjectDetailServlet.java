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

import org.example.codereview.model.User;
import org.example.codereview.model.Project;
import org.example.codereview.model.ProjectFile;
import org.example.codereview.service.ProjectService;
import org.example.codereview.service.ProjectFileService;

@WebServlet("/project")
public class ProjectDetailServlet extends HttpServlet {
    private ProjectService projectService;
    private ProjectFileService projectFileService;

    @Override
    public void init() throws ServletException {
        Object ps = getServletContext().getAttribute("projectService");
        Object pfs = getServletContext().getAttribute("projectFileService");
        if (!(ps instanceof ProjectService) || !(pfs instanceof ProjectFileService)) {
            throw new ServletException("Project services not initialized in ServletContext");
        }
        projectService = (ProjectService) ps;
        projectFileService = (ProjectFileService) pfs;
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
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
            return;
        }

        try {
            int projectId = Integer.parseInt(idParam);

            
            Project project = projectService.getProject(projectId);
            if (project == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                return;
            }

            
            boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
            if (!isAdmin && !projectService.isUserMember(currentUser.getId(), projectId)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "You are not a member of this project");
                return;
            }

            
            List<User> members = projectService.getProjectMembers(projectId);

            
            List<ProjectFile> projectFiles = projectFileService.getFilesForProject(projectId);

            
            boolean isOwner = (project.getOwnerId() == currentUser.getId());
            boolean canEditProject = isOwner || isAdmin;

            
            req.setAttribute("project", project);
            req.setAttribute("members", members);
            req.setAttribute("currentUser", currentUser);
            req.setAttribute("projectFiles", projectFiles);
            req.setAttribute("canEditProject", canEditProject);

            
            req.getRequestDispatcher("/WEB-INF/views/project.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Ошибка при загрузке проекта");
        }
    }
}

