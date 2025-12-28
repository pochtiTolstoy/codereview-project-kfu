package org.example.codereview.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionUtil {
    private static final String URL = "jdbc:postgresql://localhost:5432/codereview-db";
    private static final String USER = System.getenv().getOrDefault("DB_USER", "reviewuser");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASS", "reviewpass");

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
