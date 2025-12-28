package org.example.codereview.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import org.example.codereview.model.User;
import org.example.codereview.service.CommentService;
import org.example.codereview.service.ProjectService;
import org.example.codereview.service.ReviewService;
import org.example.codereview.service.ProjectFileService;
import org.example.codereview.service.AuthService;
import org.example.codereview.dao.UserDAO;

@WebListener
public class OnlineUsersListener implements 
        ServletContextListener, 
        HttpSessionListener, 
        HttpSessionAttributeListener {

    private static final String ONLINE_ATTR = "onlineUsers";


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        ctx.setAttribute(ONLINE_ATTR, 0);
        ctx.setAttribute("projectService", new ProjectService());
        ctx.setAttribute("reviewService", new ReviewService());
        ctx.setAttribute("commentService", new CommentService());
        ctx.setAttribute("projectFileService", new ProjectFileService());
        ctx.setAttribute("authService", new AuthService());
        ctx.setAttribute("userDAO", new UserDAO());
        ctx.setAttribute("projectFileDAO", new org.example.codereview.dao.ProjectFileDAO());
        ctx.setAttribute("reviewFileDAO", new org.example.codereview.dao.ReviewFileDAO());
        ctx.setAttribute("projectDAO", new org.example.codereview.dao.ProjectDAO());
        System.out.println("[OnlineUsersListener] contextInitialized: onlineUsers=0");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[OnlineUsersListener] contextDestroyed");
    }


    @Override
    public void sessionCreated(HttpSessionEvent se) {
        System.out.println("[OnlineUsersListener] sessionCreated: id=" + se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User) {
            decrement(session.getServletContext());
            System.out.println("[OnlineUsersListener] sessionDestroyed: user logged out, onlineUsers decremented");
        } else {
            System.out.println("[OnlineUsersListener] sessionDestroyed: no user attribute");
        }
    }


    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        if ("user".equals(event.getName()) && event.getValue() instanceof User) {
            increment(event.getSession().getServletContext());
            System.out.println("[OnlineUsersListener] attributeAdded user -> +1 online");
        }
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        if ("user".equals(event.getName()) && event.getValue() instanceof User) {
            decrement(event.getSession().getServletContext());
            System.out.println("[OnlineUsersListener] attributeRemoved user -> -1 online");
        }
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        System.out.println("[OnlineUsersListener] attributeReplaced: " + event.getName());
    }

    private void increment(ServletContext ctx) {
        synchronized (ctx) {
            Integer current = (Integer) ctx.getAttribute(ONLINE_ATTR);
            if (current == null) current = 0;
            int updated = current + 1;
            ctx.setAttribute(ONLINE_ATTR, updated);
            System.out.println("[OnlineUsersListener] increment -> onlineUsers=" + updated);
        }
    }

    private void decrement(ServletContext ctx) {
        synchronized (ctx) {
            Integer current = (Integer) ctx.getAttribute(ONLINE_ATTR);
            if (current == null) current = 0;
            int updated = Math.max(0, current - 1);
            ctx.setAttribute(ONLINE_ATTR, updated);
            System.out.println("[OnlineUsersListener] decrement -> onlineUsers=" + updated);
        }
    }
}

