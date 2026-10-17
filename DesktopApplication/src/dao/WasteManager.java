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
        String sql = "SELECT w.*, u.display_name AS collector_name, EXISTS (SELECT 1 FROM completion_requests cr "
                + "WHERE cr.waste_id=w.id AND cr.request_status='PENDING') AS completion_requested, "
                + "(SELECT cr.request_status FROM completion_requests cr WHERE cr.waste_id=w.id "
                + "ORDER BY cr.id DESC LIMIT 1) AS latest_request_status FROM waste w "
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
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
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
            cancelPendingRequests(connection, waste.getId());
            connection.commit();
        } catch (SQLException exception) {
            throw exception;
        }
    }

    public void markInvalid(int id, String reason, AppUser actor) throws SQLException {
        String sql = "UPDATE waste SET status='INVALID', invalid_reason=? WHERE id=? AND status='PENDING'"
                + (actor.isAdmin() ? "" : " AND (created_by=? OR collected_by=?)");
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reason == null ? "" : reason.trim());
            statement.setInt(2, id);
            if (!actor.isAdmin()) {
                statement.setString(3, actor.getUsername());
                statement.setString(4, actor.getUsername());
            }
                requireOneRow(statement.executeUpdate(), "Only pending records you submitted can be marked invalid.");
            }
            cancelPendingRequests(connection, id);
            connection.commit();
        }
    }

    public void markCompleted(int id, AppUser actor) throws SQLException {
        if (!actor.isAdmin()) throw new SQLException("Only an administrator can mark a record completed.");
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE waste SET status='COMPLETED' WHERE id=? AND status='PENDING'")) {
            statement.setInt(1, id);
            requireOneRow(statement.executeUpdate(), "Only a pending record can be completed.");
            }
            cancelPendingRequests(connection, id);
            connection.commit();
        }
    }

    public void requestCompletion(int wasteId, AppUser actor) throws SQLException {
        if (actor.isAdmin()) throw new SQLException("Administrators can mark records collected directly.");
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String ownerSql = "SELECT status, created_by, collected_by FROM waste WHERE id=? FOR UPDATE";
                try (PreparedStatement statement = connection.prepareStatement(ownerSql)) {
                    statement.setInt(1, wasteId);
                    try (ResultSet row = statement.executeQuery()) {
                        if (!row.next() || !"PENDING".equals(row.getString("status"))) {
                            throw new SQLException("Only a pending record can be sent for approval.");
                        }
                        if (!actor.getUsername().equals(row.getString("created_by"))
                                && !actor.getUsername().equals(row.getString("collected_by"))) {
                            throw new SQLException("You can request completion only for your own records.");
                        }
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT 1 FROM completion_requests WHERE waste_id=? AND request_status='PENDING' FOR UPDATE")) {
                    statement.setInt(1, wasteId);
                    try (ResultSet row = statement.executeQuery()) {
                        if (row.next()) throw new SQLException("This record is already waiting for admin approval.");
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO completion_requests (waste_id, requested_by) VALUES (?, ?)")) {
                    statement.setInt(1, wasteId);
                    statement.setString(2, actor.getUsername());
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public int getPendingCompletionRequestCount(AppUser actor) throws SQLException {
        requireAdmin(actor);
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM completion_requests WHERE request_status='PENDING'")) {
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getInt(1);
            }
        }
    }

    public List<Object[]> getPendingCompletionRequests(AppUser actor) throws SQLException {
        requireAdmin(actor);
        List<Object[]> requests = new ArrayList<>();
        String sql = "SELECT cr.id, w.id, w.waste_type, w.description, w.quantity, w.location, w.collected_at, "
                + "u.display_name, cr.requested_at FROM completion_requests cr "
                + "JOIN waste w ON w.id=cr.waste_id JOIN app_users u ON u.username=cr.requested_by "
                + "WHERE cr.request_status='PENDING' AND w.status='PENDING' ORDER BY cr.requested_at, cr.id";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) requests.add(new Object[]{rows.getLong(1), rows.getInt(2), rows.getString(3),
                    rows.getString(4), rows.getDouble(5), rows.getString(6), rows.getTimestamp(7).toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), rows.getString(8),
                    rows.getTimestamp(9).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))});
        }
        return requests;
    }

    public void approveCompletionRequest(long requestId, AppUser actor) throws SQLException {
        requireAdmin(actor);
        updateCompletionRequest(requestId, actor, true);
    }

    public void rejectCompletionRequest(long requestId, AppUser actor) throws SQLException {
        requireAdmin(actor);
        updateCompletionRequest(requestId, actor, false);
    }

    private void updateCompletionRequest(long requestId, AppUser actor, boolean approve) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int wasteId;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT waste_id FROM completion_requests WHERE id=? AND request_status='PENDING' FOR UPDATE")) {
                    statement.setLong(1, requestId);
                    try (ResultSet row = statement.executeQuery()) {
                        if (!row.next()) throw new SQLException("This approval request is no longer pending.");
                        wasteId = row.getInt(1);
                    }
                }
                if (approve) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE waste SET status='COMPLETED' WHERE id=? AND status='PENDING'")) {
                        statement.setInt(1, wasteId);
                        requireOneRow(statement.executeUpdate(), "The waste record is no longer pending.");
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE completion_requests SET request_status=?, reviewed_by=?, reviewed_at=CURRENT_TIMESTAMP "
                                + "WHERE id=? AND request_status='PENDING'")) {
                    statement.setString(1, approve ? "APPROVED" : "REJECTED");
                    statement.setString(2, actor.getUsername());
                    statement.setLong(3, requestId);
                    requireOneRow(statement.executeUpdate(), "This approval request was already reviewed.");
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
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
        waste.setCompletionRequested(rows.getBoolean("completion_requested"));
        waste.setCompletionRequestStatus(rows.getString("latest_request_status"));
        return waste;
    }

    private static void cancelPendingRequests(Connection connection, int wasteId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE completion_requests SET request_status='CANCELLED' WHERE waste_id=? AND request_status='PENDING'")) {
            statement.setInt(1, wasteId);
            statement.executeUpdate();
        }
    }

    private static void requireAdmin(AppUser actor) throws SQLException {
        if (!actor.isAdmin()) throw new SQLException("Only an administrator can review completion requests.");
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
