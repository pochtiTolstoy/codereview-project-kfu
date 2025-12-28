package org.example.codereview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import org.example.codereview.model.Project;
import org.example.codereview.model.User;
import org.example.codereview.service.ProjectService;
import org.example.codereview.service.ProjectFileService;
import org.example.codereview.util.CsrfUtil;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@WebServlet("/project/import")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,     
    maxFileSize = 1024 * 1024 * 50,      
    maxRequestSize = 1024 * 1024 * 60    
)
public class ProjectImportServlet extends HttpServlet {
    
    private ProjectService projectService;
    private ProjectFileService projectFileService;
    
    @Override
    public void init() throws ServletException {
        projectService = (ProjectService) getServletContext().getAttribute("projectService");
        projectFileService = (ProjectFileService) getServletContext().getAttribute("projectFileService");
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
        
        String projectIdParam = req.getParameter("projectId");
        if (projectIdParam == null || projectIdParam.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
            return;
        }
        
        try {
            int projectId = Integer.parseInt(projectIdParam);
            Project project = projectService.getProject(projectId);
            
            if (project == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                return;
            }
            
            
            boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
            boolean isOwner = project.getOwnerId() == currentUser.getId();
            
            if (!isAdmin && !isOwner) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "Only project owner can import files");
                return;
            }
            
            
            Part filePart = req.getPart("zipFile");
            if (filePart == null || filePart.getSize() == 0) {
                String error = URLEncoder.encode("Файл не выбран", StandardCharsets.UTF_8);
                resp.sendRedirect(req.getContextPath() + "/project?id=" + projectId + "&error=" + error);
                return;
            }
            
            
            if (filePart.getSize() > 50 * 1024 * 1024) {
                String error = URLEncoder.encode("Файл слишком большой (макс. 50MB)", StandardCharsets.UTF_8);
                resp.sendRedirect(req.getContextPath() + "/project?id=" + projectId + "&error=" + error);
                return;
            }
            
            
            ImportResult result = importZipArchive(filePart.getInputStream(), project);
            
            
            String message;
            if (result.skippedCount > 0) {
                message = URLEncoder.encode(
                    "Импортировано файлов: " + result.importedCount + 
                    ". Пропущено неподдерживаемых: " + result.skippedCount, 
                    StandardCharsets.UTF_8);
            } else {
                message = URLEncoder.encode("Импортировано файлов: " + result.importedCount, StandardCharsets.UTF_8);
            }
            resp.sendRedirect(req.getContextPath() + "/project?id=" + projectId + "&success=" + message);
            
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID");
        } catch (IllegalArgumentException e) {
            String error = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/project?id=" + projectIdParam + "&error=" + error);
        } catch (SQLException e) {
            e.printStackTrace();
            String error = URLEncoder.encode("Ошибка БД при импорте", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/project?id=" + projectIdParam + "&error=" + error);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Ошибка при импорте: " + e.getMessage());
        }
    }
    
    
    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>();
    static {
        
        SUPPORTED_EXTENSIONS.add(".java");
        SUPPORTED_EXTENSIONS.add(".c");
        SUPPORTED_EXTENSIONS.add(".h");
        SUPPORTED_EXTENSIONS.add(".cpp");
        SUPPORTED_EXTENSIONS.add(".hpp");
        SUPPORTED_EXTENSIONS.add(".cc");
        SUPPORTED_EXTENSIONS.add(".cxx");
        SUPPORTED_EXTENSIONS.add(".py");
        SUPPORTED_EXTENSIONS.add(".js");
        SUPPORTED_EXTENSIONS.add(".ts");
        SUPPORTED_EXTENSIONS.add(".go");
        SUPPORTED_EXTENSIONS.add(".rs");
        SUPPORTED_EXTENSIONS.add(".php");
        SUPPORTED_EXTENSIONS.add(".rb");
        SUPPORTED_EXTENSIONS.add(".swift");
        SUPPORTED_EXTENSIONS.add(".kt");
        SUPPORTED_EXTENSIONS.add(".scala");
        
        SUPPORTED_EXTENSIONS.add(".html");
        SUPPORTED_EXTENSIONS.add(".htm");
        SUPPORTED_EXTENSIONS.add(".css");
        SUPPORTED_EXTENSIONS.add(".scss");
        SUPPORTED_EXTENSIONS.add(".sass");
        SUPPORTED_EXTENSIONS.add(".less");
        SUPPORTED_EXTENSIONS.add(".xml");
        SUPPORTED_EXTENSIONS.add(".svg"); 
        SUPPORTED_EXTENSIONS.add(".json");
        SUPPORTED_EXTENSIONS.add(".yaml");
        SUPPORTED_EXTENSIONS.add(".yml");
        
        SUPPORTED_EXTENSIONS.add(".properties");
        SUPPORTED_EXTENSIONS.add(".conf");
        SUPPORTED_EXTENSIONS.add(".config");
        SUPPORTED_EXTENSIONS.add(".ini");
        SUPPORTED_EXTENSIONS.add(".toml");
        
        SUPPORTED_EXTENSIONS.add(".sh");
        SUPPORTED_EXTENSIONS.add(".bash");
        SUPPORTED_EXTENSIONS.add(".bat");
        SUPPORTED_EXTENSIONS.add(".cmd");
        SUPPORTED_EXTENSIONS.add(".ps1");
        
        SUPPORTED_EXTENSIONS.add(".sql");
        
        SUPPORTED_EXTENSIONS.add(".md");
        SUPPORTED_EXTENSIONS.add(".txt");
        SUPPORTED_EXTENSIONS.add(".rst");
        
        SUPPORTED_EXTENSIONS.add(".log");
        SUPPORTED_EXTENSIONS.add(".gradle");
        SUPPORTED_EXTENSIONS.add(".maven");
        SUPPORTED_EXTENSIONS.add(".pom");
        SUPPORTED_EXTENSIONS.add(".lock");
        SUPPORTED_EXTENSIONS.add(".dockerfile");
        SUPPORTED_EXTENSIONS.add(".gitignore");
        SUPPORTED_EXTENSIONS.add(".gitattributes");
    }
    
    
    private static final Set<String> BINARY_EXTENSIONS = new HashSet<>();
    static {
        
        BINARY_EXTENSIONS.add(".png");
        BINARY_EXTENSIONS.add(".jpg");
        BINARY_EXTENSIONS.add(".jpeg");
        BINARY_EXTENSIONS.add(".gif");
        BINARY_EXTENSIONS.add(".bmp");
        BINARY_EXTENSIONS.add(".ico");
        BINARY_EXTENSIONS.add(".webp");
        
        BINARY_EXTENSIONS.add(".zip");
        BINARY_EXTENSIONS.add(".rar");
        BINARY_EXTENSIONS.add(".7z");
        BINARY_EXTENSIONS.add(".tar");
        BINARY_EXTENSIONS.add(".gz");
        BINARY_EXTENSIONS.add(".jar");
        BINARY_EXTENSIONS.add(".war");
        BINARY_EXTENSIONS.add(".ear");
        
        BINARY_EXTENSIONS.add(".exe");
        BINARY_EXTENSIONS.add(".dll");
        BINARY_EXTENSIONS.add(".so");
        BINARY_EXTENSIONS.add(".dylib");
        
        BINARY_EXTENSIONS.add(".pdf");
        BINARY_EXTENSIONS.add(".doc");
        BINARY_EXTENSIONS.add(".docx");
        BINARY_EXTENSIONS.add(".xls");
        BINARY_EXTENSIONS.add(".xlsx");
        BINARY_EXTENSIONS.add(".ppt");
        BINARY_EXTENSIONS.add(".pptx");
        
        BINARY_EXTENSIONS.add(".mp3");
        BINARY_EXTENSIONS.add(".mp4");
        BINARY_EXTENSIONS.add(".avi");
        BINARY_EXTENSIONS.add(".mov");
        
        BINARY_EXTENSIONS.add(".class");
        BINARY_EXTENSIONS.add(".o");
        BINARY_EXTENSIONS.add(".obj");
        BINARY_EXTENSIONS.add(".bin");
    }
    
    private boolean isFileSupported(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        String lowerName = filename.toLowerCase();
        
        
        for (String ext : BINARY_EXTENSIONS) {
            if (lowerName.endsWith(ext)) {
                return false;
            }
        }
        
        
        int lastDot = lowerName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < lowerName.length() - 1) {
            String ext = lowerName.substring(lastDot);
            return SUPPORTED_EXTENSIONS.contains(ext);
        }
        
        
        return true;
    }
    
    
    private static class ImportResult {
        int importedCount;
        int skippedCount;
        
        ImportResult(int importedCount, int skippedCount) {
            this.importedCount = importedCount;
            this.skippedCount = skippedCount;
        }
    }
    
    private ImportResult importZipArchive(InputStream inputStream, Project project) 
            throws IOException, SQLException {
        int count = 0;
        int skippedCount = 0;
        long totalSize = 0;
        final long MAX_TOTAL_SIZE = 100 * 1024 * 1024; 
        
        try (ZipInputStream zis = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                
                if (entry.isDirectory()) {
                    continue;
                }
                
                String name = entry.getName();
                
                
                if (name.contains("..") || name.startsWith("/") || name.contains("\\")) {
                    throw new IllegalArgumentException("Недопустимый путь в архиве: " + name);
                }
                
                
                if (name.startsWith(".git/") || name.equals(".gitignore") || 
                    name.startsWith("__MACOSX/") || name.endsWith(".DS_Store")) {
                    continue;
                }
                
                
                if (!isFileSupported(name)) {
                    skippedCount++;
                    zis.closeEntry();
                    continue;
                }
                
                
                byte[] buffer = new byte[8192];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                
                while ((len = zis.read(buffer)) > 0) {
                    totalSize += len;
                    
                    
                    if (totalSize > MAX_TOTAL_SIZE) {
                        throw new IllegalArgumentException(
                            "Архив слишком большой после распаковки (макс. 100MB)");
                    }
                    
                    baos.write(buffer, 0, len);
                }
                
                
                String content;
                try {
                    content = baos.toString(StandardCharsets.UTF_8);
                } catch (Exception e) {
                    
                    skippedCount++;
                    zis.closeEntry();
                    continue;
                }
                
                
                String normalizedPath = name.replace('\\', '/');
                
                
                try {
                    projectFileService.createOrUpdateFile(project.getId(), normalizedPath, content);
                    count++;
                } catch (Exception e) {
                    
                    System.err.println("Ошибка при импорте файла " + name + ": " + e.getMessage());
                    skippedCount++;
                }
                
                zis.closeEntry();
            }
        }
        
        if (skippedCount > 0) {
            System.out.println("Пропущено неподдерживаемых файлов: " + skippedCount);
        }
        
        return new ImportResult(count, skippedCount);
    }
}

