package dao;

import model.HazardousWaste;
import model.OrganicWaste;
import model.RecyclableWaste;
import model.Waste;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class WasteManager {
    public void addWaste(Waste waste) throws SQLException {
        String sql = "INSERT INTO waste (waste_type, description, quantity, location, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, waste.getWasteCategory());
            statement.setString(2, waste.getDescription());
            statement.setDouble(3, waste.getQuantity());
            statement.setString(4, waste.getLocation());
            statement.setString(5, waste.getStatus());
            statement.executeUpdate();
        }
    }

    public List<Waste> getAllWaste() throws SQLException {
        List<Waste> wasteList = new ArrayList<>();
        String sql = "SELECT * FROM waste ORDER BY id";

        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                wasteList.add(createWasteFromResultSet(resultSet));
            }
        }

        return wasteList;
    }

    public List<Waste> searchWaste(String keyword) throws SQLException {
        List<Waste> wasteList = new ArrayList<>();
        String sql = "SELECT * FROM waste WHERE description LIKE ? OR location LIKE ? OR waste_type LIKE ? OR status LIKE ? ORDER BY id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchText = "%" + keyword + "%";
            statement.setString(1, searchText);
            statement.setString(2, searchText);
            statement.setString(3, searchText);
            statement.setString(4, searchText);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    wasteList.add(createWasteFromResultSet(resultSet));
                }
            }
        }

        return wasteList;
    }

    public void updateWaste(Waste waste) throws SQLException {
        String sql = "UPDATE waste SET waste_type = ?, description = ?, quantity = ?, location = ?, status = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, waste.getWasteCategory());
            statement.setString(2, waste.getDescription());
            statement.setDouble(3, waste.getQuantity());
            statement.setString(4, waste.getLocation());
            statement.setString(5, waste.getStatus());
            statement.setInt(6, waste.getId());
            statement.executeUpdate();
        }
    }

    public void deleteWaste(int id) throws SQLException {
        String sql = "DELETE FROM waste WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void markAsCollected(int id) throws SQLException {
        String sql = "UPDATE waste SET status = 'Collected' WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private Waste createWasteFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String wasteType = resultSet.getString("waste_type");
        String description = resultSet.getString("description");
        double quantity = resultSet.getDouble("quantity");
        String location = resultSet.getString("location");
        String status = resultSet.getString("status");

        if ("Organic".equalsIgnoreCase(wasteType)) {
            return new OrganicWaste(id, description, quantity, location, status);
        } else if ("Recyclable".equalsIgnoreCase(wasteType)) {
            return new RecyclableWaste(id, description, quantity, location, status);
        } else {
            return new HazardousWaste(id, description, quantity, location, status);
        }
    }
}
