package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String DATABASE_NAME = "waste_management_db";
    private static final String DATABASE_URL = SERVER_URL + DATABASE_NAME;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
    }

    public static void initializeDatabase() throws SQLException {
        createDatabaseIfNeeded();
        createWasteTableIfNeeded();
        insertStarterRecordsIfEmpty();
    }

    private static void createDatabaseIfNeeded() throws SQLException {
        String sql = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;

        try (Connection connection = DriverManager.getConnection(SERVER_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
        }
    }

    private static void createWasteTableIfNeeded() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS waste ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "waste_type VARCHAR(50) NOT NULL, "
                + "description VARCHAR(255) NOT NULL, "
                + "quantity DOUBLE NOT NULL, "
                + "location VARCHAR(100) NOT NULL, "
                + "status VARCHAR(50) NOT NULL"
                + ")";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
        }
    }

    private static void insertStarterRecordsIfEmpty() throws SQLException {
        String countSql = "SELECT COUNT(*) FROM waste";
        String insertSql = "INSERT INTO waste (waste_type, description, quantity, location, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             Statement countStatement = connection.createStatement();
             ResultSet resultSet = countStatement.executeQuery(countSql)) {

            resultSet.next();
            if (resultSet.getInt(1) > 0) {
                return;
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                insertStarterRecord(insertStatement, "Organic", "Food waste from cafeteria", 12.5, "College Canteen", "Pending");
                insertStarterRecord(insertStatement, "Recyclable", "Plastic bottles", 8.0, "Block A", "Pending");
                insertStarterRecord(insertStatement, "Hazardous", "Used batteries", 3.0, "Lab Room 2", "Collected");
            }
        }
    }

    private static void insertStarterRecord(PreparedStatement statement, String type, String description,
                                            double quantity, String location, String status) throws SQLException {
        statement.setString(1, type);
        statement.setString(2, description);
        statement.setDouble(3, quantity);
        statement.setString(4, location);
        statement.setString(5, status);
        statement.executeUpdate();
    }
}
