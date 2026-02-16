package com.mycompany.perpustakaanjava;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static final String DB_NAME = "perpustakaan_v2";
    private static final String URL = "jdbc:mariadb://localhost:3306/" + DB_NAME;
    public static final String USER = "root"; 
    public static final String PASSWORD = "cianjurtea"; 
    private static Connection connection = null;

    private DatabaseConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Database Connection Failed: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
}
