package org.example.codereview.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class CsrfUtil {

    public static boolean isValid(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }
        String sessionToken = (String) session.getAttribute("csrf");
        String token = req.getParameter("csrf");
        return sessionToken != null && sessionToken.equals(token);
    }

    public static boolean rejectIfInvalid(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isValid(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return true;
        }
        return false;
    }
}

