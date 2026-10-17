package dao;

import model.AppUser;
import model.HazardousWaste;
import model.OrganicWaste;
import model.RecyclableWaste;
import model.Waste;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WasteManager {
    public void addWaste(Waste waste, AppUser actor, String collectorUsername) throws SQLException {
        String collector = actor.isAdmin() ? emptyToNull(collectorUsername) : actor.getUsername();
        String sql = "INSERT INTO waste (waste_type, description, quantity, location, status, created_by, collected_by, collected_at) "
                + "VALUES (?, ?, ?, ?, 'PENDING', ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, waste.getWasteCategory());
            statement.setString(2, waste.getDescription().trim());
            statement.setDouble(3, waste.getQuantity());
            statement.setString(4, waste.getLocation());
            statement.setString(5, actor.getUsername());
            statement.setString(6, collector);
            statement.setTimestamp(7, Timestamp.valueOf(parseCollectedAt(waste.getCollectedAt())));
            statement.executeUpdate();
        }
    }

    public List<Waste> getVisibleWaste(AppUser actor, String keyword) throws SQLException {
        List<Waste> records = new ArrayList<>();
        String sql = "SELECT w.*, u.display_name AS collector_name FROM waste w "
                + "LEFT JOIN app_users u ON u.username = w.collected_by WHERE "
                + (actor.isAdmin() ? "1=1" : "(w.created_by = ? OR w.collected_by = ?)")
                + " AND (w.description LIKE ? OR w.location LIKE ? OR w.waste_type LIKE ? OR w.status LIKE ? "
                + "OR COALESCE(u.display_name,'') LIKE ?) ORDER BY w.id DESC";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int parameter = 1;
            if (!actor.isAdmin()) {
                statement.setString(parameter++, actor.getUsername());
                statement.setString(parameter++, actor.getUsername());
            }
            String match = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            for (int i = 0; i < 5; i++) statement.setString(parameter++, match);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) records.add(createWaste(rows));
            }
        }
        return records;
    }

    public List<String[]> getCollectors() throws SQLException {
        List<String[]> users = new ArrayList<>();
        String sql = "SELECT username, display_name FROM app_users WHERE role='USER' AND active=TRUE ORDER BY display_name";
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            while (rows.next()) users.add(new String[]{rows.getString(1), rows.getString(2)});
        }
        return users;
    }

    public void updateWaste(Waste waste, AppUser actor, String collectorUsername) throws SQLException {
        String access = actor.isAdmin() ? "" : " AND (created_by = ? OR collected_by = ?)";
        String sql = "UPDATE waste SET waste_type=?, description=?, quantity=?, location=?, collected_at=?, collected_by=? "
                + "WHERE id=? AND status='PENDING'" + access;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, waste.getWasteCategory());
            statement.setString(2, waste.getDescription().trim());
            statement.setDouble(3, waste.getQuantity());
            statement.setString(4, waste.getLocation());
            statement.setTimestamp(5, Timestamp.valueOf(parseCollectedAt(waste.getCollectedAt())));
            statement.setString(6, actor.isAdmin() ? emptyToNull(collectorUsername) : actor.getUsername());
            statement.setInt(7, waste.getId());
            if (!actor.isAdmin()) {
                statement.setString(8, actor.getUsername());
                statement.setString(9, actor.getUsername());
            }
            requireOneRow(statement.executeUpdate(), "Only your own pending records can be edited.");
        }
    }

    public void markInvalid(int id, String reason, AppUser actor) throws SQLException {
        String sql = "UPDATE waste SET status='INVALID', invalid_reason=? WHERE id=? AND status='PENDING'"
                + (actor.isAdmin() ? "" : " AND (created_by=? OR collected_by=?)");
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reason == null ? "" : reason.trim());
            statement.setInt(2, id);
            if (!actor.isAdmin()) {
                statement.setString(3, actor.getUsername());
                statement.setString(4, actor.getUsername());
            }
            requireOneRow(statement.executeUpdate(), "Only pending records you submitted can be marked invalid.");
        }
    }

    public void markCompleted(int id, AppUser actor) throws SQLException {
        if (!actor.isAdmin()) throw new SQLException("Only an administrator can mark a record completed.");
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE waste SET status='COMPLETED' WHERE id=? AND status='PENDING'")) {
            statement.setInt(1, id);
            requireOneRow(statement.executeUpdate(), "Only a pending record can be completed.");
        }
    }

    public void deleteWaste(int id, AppUser actor) throws SQLException {
        if (!actor.isAdmin()) throw new SQLException("Only an administrator can delete records.");
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM waste WHERE id=?")) {
            statement.setInt(1, id);
            requireOneRow(statement.executeUpdate(), "Record was not found.");
        }
    }

    private Waste createWaste(ResultSet rows) throws SQLException {
        int id = rows.getInt("id");
        String type = rows.getString("waste_type");
        String description = rows.getString("description");
        double quantity = rows.getDouble("quantity");
        String location = rows.getString("location");
        String status = rows.getString("status");
        Waste waste;
        if ("Organic".equalsIgnoreCase(type)) waste = new OrganicWaste(id, description, quantity, location, status);
        else if ("Recyclable".equalsIgnoreCase(type)) waste = new RecyclableWaste(id, description, quantity, location, status);
        else waste = new HazardousWaste(id, description, quantity, location, status);
        waste.setCollectedBy(rows.getString("collected_by"));
        waste.setCreatedBy(rows.getString("created_by"));
        Timestamp timestamp = rows.getTimestamp("collected_at");
        waste.setCollectedAt(timestamp == null ? "" : timestamp.toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        waste.setInvalidReason(rows.getString("invalid_reason"));
        return waste;
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value;
    }

    private static LocalDateTime parseCollectedAt(String value) {
        String input = value.trim();
        if (input.indexOf('T') >= 0) return LocalDateTime.parse(input);
        return LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private static void requireOneRow(int count, String message) throws SQLException {
        if (count != 1) throw new SQLException(message);
    }
}
