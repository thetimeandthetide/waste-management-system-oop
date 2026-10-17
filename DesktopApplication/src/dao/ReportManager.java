package dao;

import model.AppUser;
import model.ReportRow;
import model.ReportSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportManager {
    public ReportSummary getSummary(AppUser user, LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity),0), "
                + "COALESCE(SUM(CASE WHEN waste_type='Organic' THEN quantity ELSE 0 END),0), "
                + "COALESCE(SUM(CASE WHEN waste_type='Recyclable' THEN quantity ELSE 0 END),0), "
                + "COALESCE(SUM(CASE WHEN waste_type='Hazardous' THEN quantity ELSE 0 END),0), COUNT(*) "
                + "FROM waste WHERE status='COMPLETED' AND collected_at>=? AND collected_at<?"
                + (user.isAdmin() ? "" : " AND collected_by=?");
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDateRange(statement, from, to, 1);
            if (!user.isAdmin()) statement.setString(3, user.getUsername());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return new ReportSummary(result.getDouble(1), result.getDouble(2), result.getDouble(3),
                        result.getDouble(4), result.getInt(5));
            }
        }
    }

    public ReportRow getUserReport(String username, LocalDate from, LocalDate to, AppUser actor) throws SQLException {
        if (!actor.isAdmin() && !actor.getUsername().equals(username)) {
            throw new SQLException("Members can only view their own report.");
        }
        String sql = "SELECT u.display_name, "
                + "COALESCE(SUM(CASE WHEN w.waste_type='Organic' THEN w.quantity ELSE 0 END),0), "
                + "COALESCE(SUM(CASE WHEN w.waste_type='Recyclable' THEN w.quantity ELSE 0 END),0), "
                + "COALESCE(SUM(CASE WHEN w.waste_type='Hazardous' THEN w.quantity ELSE 0 END),0), COUNT(w.id), "
                + "COALESCE(SUM(CASE WHEN LOWER(w.location)=LOWER(u.assigned_area) THEN w.quantity ELSE 0 END),0), "
                + "COALESCE(SUM(CASE WHEN LOWER(w.location)<>LOWER(u.assigned_area) THEN w.quantity ELSE 0 END),0) "
                + "FROM app_users u LEFT JOIN waste w ON w.collected_by=u.username AND w.status='COMPLETED' "
                + "AND w.collected_at>=? AND w.collected_at<? WHERE u.username=? AND u.role='USER' "
                + "GROUP BY u.username,u.display_name";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDateRange(statement, from, to, 1);
            statement.setString(3, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SQLException("User report was not found.");
                return new ReportRow(result.getString(1), result.getDouble(2), result.getDouble(3),
                        result.getDouble(4), result.getInt(5), result.getDouble(6), result.getDouble(7));
            }
        }
    }

    public List<ReportRow> getAllUserReports(LocalDate from, LocalDate to, AppUser actor) throws SQLException {
        requireAdmin(actor);
        List<ReportRow> reports = new ArrayList<>();
        String sql = "SELECT u.display_name, "
                + "COALESCE(SUM(CASE WHEN w.waste_type='Organic' THEN w.quantity ELSE 0 END),0), "
                + "COALESCE(SUM(CASE WHEN w.waste_type='Recyclable' THEN w.quantity ELSE 0 END),0), "
                + "COALESCE(SUM(CASE WHEN w.waste_type='Hazardous' THEN w.quantity ELSE 0 END),0), COUNT(w.id), "
                + "COALESCE(SUM(CASE WHEN LOWER(w.location)=LOWER(u.assigned_area) THEN w.quantity ELSE 0 END),0), "
                + "COALESCE(SUM(CASE WHEN LOWER(w.location)<>LOWER(u.assigned_area) THEN w.quantity ELSE 0 END),0) "
                + "FROM app_users u LEFT JOIN waste w ON w.collected_by=u.username AND w.status='COMPLETED' "
                + "AND w.collected_at>=? AND w.collected_at<? WHERE u.role='USER' AND u.active=TRUE "
                + "GROUP BY u.username,u.display_name ORDER BY u.display_name";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDateRange(statement, from, to, 1);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) reports.add(new ReportRow(result.getString(1), result.getDouble(2),
                        result.getDouble(3), result.getDouble(4), result.getInt(5),
                        result.getDouble(6), result.getDouble(7)));
            }
        }
        return reports;
    }

    public List<ReportRow> getAreaReports(LocalDate from, LocalDate to, AppUser actor) throws SQLException {
        requireAdmin(actor);
        List<ReportRow> reports = new ArrayList<>();
        String sql = "SELECT location, "
                + "COALESCE(SUM(CASE WHEN waste_type='Organic' THEN quantity ELSE 0 END),0), "
                + "COALESCE(SUM(CASE WHEN waste_type='Recyclable' THEN quantity ELSE 0 END),0), "
                + "COALESCE(SUM(CASE WHEN waste_type='Hazardous' THEN quantity ELSE 0 END),0), COUNT(*) "
                + "FROM waste WHERE status='COMPLETED' AND collected_at>=? AND collected_at<? "
                + "GROUP BY location ORDER BY SUM(quantity) DESC";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDateRange(statement, from, to, 1);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) reports.add(new ReportRow(result.getString(1), result.getDouble(2),
                        result.getDouble(3), result.getDouble(4), result.getInt(5), 0, 0));
            }
        }
        return reports;
    }

    private static void requireAdmin(AppUser actor) throws SQLException {
        if (!actor.isAdmin()) throw new SQLException("Only an administrator can view organization-wide reports.");
    }

    private static void bindDateRange(PreparedStatement statement, LocalDate from, LocalDate to, int firstIndex)
            throws SQLException {
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("A valid report date range is required.");
        }
        statement.setTimestamp(firstIndex, Timestamp.valueOf(from.atStartOfDay()));
        statement.setTimestamp(firstIndex + 1, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
    }
}
