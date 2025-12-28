package org.example.codereview.service;

import org.example.codereview.dao.UserDAO;
import org.example.codereview.model.User;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public boolean register(User user) {
        try {
            userDAO.insert(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
