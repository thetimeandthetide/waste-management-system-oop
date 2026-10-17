package dao;

import model.Announcement;
import model.AppUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementManager {
    public void publish(String title, String message, AppUser actor) throws SQLException {
        if (!actor.isAdmin()) throw new SQLException("Only an administrator can publish announcements.");
        String cleanTitle = title == null ? "" : title.trim();
        String cleanMessage = message == null ? "" : message.trim();
        if (cleanTitle.isEmpty() || cleanMessage.isEmpty()) {
            throw new SQLException("Announcement title and message are required.");
        }
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO announcements (title, message, created_by) VALUES (?, ?, ?)")) {
            statement.setString(1, cleanTitle);
            statement.setString(2, cleanMessage);
            statement.setString(3, actor.getUsername());
            statement.executeUpdate();
        }
    }

    public List<Announcement> getAnnouncements(AppUser user) throws SQLException {
        String sql = "SELECT a.id, a.title, a.message, u.display_name, a.created_at, "
                + "CASE WHEN r.username IS NULL THEN FALSE ELSE TRUE END AS is_read "
                + "FROM announcements a JOIN app_users u ON u.username=a.created_by "
                + "LEFT JOIN announcement_reads r ON r.announcement_id=a.id AND r.username=? "
                + "ORDER BY a.created_at DESC, a.id DESC";
        List<Announcement> announcements = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    Timestamp timestamp = rows.getTimestamp("created_at");
                    announcements.add(new Announcement(rows.getInt("id"), rows.getString("title"),
                            rows.getString("message"), rows.getString("display_name"),
                            timestamp.toLocalDateTime(), rows.getBoolean("is_read")));
                }
            }
        }
        return announcements;
    }

    public int getUnreadCount(AppUser user) throws SQLException {
        if (user.isAdmin()) return 0;
        String sql = "SELECT COUNT(*) FROM announcements a LEFT JOIN announcement_reads r "
                + "ON r.announcement_id=a.id AND r.username=? WHERE r.username IS NULL";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getInt(1);
            }
        }
    }

    public void markRead(int announcementId, AppUser user) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT IGNORE INTO announcement_reads (announcement_id, username) "
                             + "SELECT id, ? FROM announcements WHERE id=?")) {
            statement.setString(1, user.getUsername());
            statement.setInt(2, announcementId);
            statement.executeUpdate();
        }
    }
}
