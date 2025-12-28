package org.example.codereview.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.example.codereview.model.User;

import java.io.IOException;
import java.util.UUID;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        
        String path;
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            path = uri.substring(contextPath.length());
        } else {
            path = uri;
        }
        
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        boolean isStaticResource = path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/favicon")
                || path.contains(".css")
                || path.contains(".js")
                || path.contains(".png")
                || path.contains(".jpg")
                || path.contains(".jpeg")
                || path.contains(".gif")
                || path.contains(".ico")
                || path.contains(".svg")
                || path.contains(".woff")
                || path.contains(".woff2")
                || path.contains(".ttf")
                || path.contains(".eot");
        
        if (isStaticResource) {
            chain.doFilter(request, response);
            return;
        }

        if (path.equals("/")
                || path.startsWith("/login")
                || path.startsWith("/register")
                || path.startsWith("/help")
                || path.startsWith("/error")) {
            ensureCsrfToken(req);
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            resp.sendRedirect(contextPath + "/login");
            return;
        }

        ensureCsrfToken(req);
        chain.doFilter(request, response);
    }

    private void ensureCsrfToken(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        Object token = session.getAttribute("csrf");
        if (token == null) {
            session.setAttribute("csrf", UUID.randomUUID().toString());
        }
    }

    @Override
    public void destroy() {
    }
}

