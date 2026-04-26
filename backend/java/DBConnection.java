package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = getEnv("DB_URL", "jdbc:mysql://localhost:3306/finance_tracker");
    private static final String USER = getEnv("DB_USER", "finance_tracker_user");
    private static final String PASSWORD = getEnv("DB_PASSWORD", "");

    private DBConnection() {}

    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    public static Connection getConnection() throws SQLException {
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. " +
                    "Add mysql-connector-java.jar to your classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Warning: Failed to close DB connection: " + e.getMessage());
            }
        }
    }
}
