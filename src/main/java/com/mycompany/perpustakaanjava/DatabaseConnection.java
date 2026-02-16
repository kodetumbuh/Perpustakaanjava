package com.mycompany.perpustakaanjava;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static final String DB_NAME = "perpustakaan_v2";
    private static final String URL = "jdbc:mariadb://localhost:3306/" + DB_NAME;
    // Default credentials, but will be overwritten by Preferences
    public static String USER = "root"; 
    public static String PASSWORD = ""; 

    static {
        // Load from Preferences
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(DatabaseConnection.class);
        USER = prefs.get("DB_USER", "root");
        PASSWORD = prefs.get("DB_PASS", "");
    }

    private static Connection connection = null;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void updateCredentials(String newUser, String newPass) {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(DatabaseConnection.class);
        prefs.put("DB_USER", newUser);
        prefs.put("DB_PASS", newPass);
        
        USER = newUser;
        PASSWORD = newPass;
        
        // Reset connection to force reconnection with new credentials next time
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection = null;
    }
    
    public static boolean checkConnection() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean checkServerConnection() {
        // Connect to server only (no database selected)
        String serverUrl = "jdbc:mariadb://localhost:3306/";
        try (Connection conn = DriverManager.getConnection(serverUrl, USER, PASSWORD)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
