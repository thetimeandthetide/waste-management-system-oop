package dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String SERVER_URL = setting("WASTE_DB_SERVER", "jdbc:mysql://localhost:3306/");
    private static final String DATABASE_NAME = setting("WASTE_DB_NAME", "waste_management_db");
    private static final String DATABASE_URL = SERVER_URL + DATABASE_NAME;
    private static final String USERNAME = setting("WASTE_DB_USER", "root");
    private static final String PASSWORD = setting("WASTE_DB_PASSWORD", "");

    private static final String[][] MEMBERS = {
            {"aman", "Aman Fayad", "Civil canteen"},
            {"joseph", "Joseph Fidel", "Mech front"},
            {"nivedya", "Nivedya", "Dhwani"},
            {"ayshwaria", "Ayshwaria", "Gazebo"},
            {"nida", "Nida", "CS front"},
            {"arya", "Arya", "Golden walkway"},
            {"ahban", "Ahban", "Electrical gate"},
            {"sidharth", "Sidharth", "Indoor court"},
            {"heera", "Heera", "Archie corner"},
            {"keerthana", "Keerthana", "College front"},
            {"farzeen", "Farzeen", "DJ hall"},
            {"merin", "Merin", "CGPU hall"},
            {"shyam", "Shyam", "CETAA hall"}
    };

    private static String setting(String name, String fallback) {
        String value = System.getProperty(name);
        if (value == null || value.trim().isEmpty()) value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
    }

    public static void initializeDatabase() throws SQLException {
        try (Connection connection = DriverManager.getConnection(SERVER_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME
                    + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        createTables();
        migrateWasteTable();
        seedAccounts();
    }

    private static void createTables() throws SQLException {
        String usersSql = "CREATE TABLE IF NOT EXISTS app_users ("
                + "username VARCHAR(40) PRIMARY KEY, display_name VARCHAR(100) NOT NULL, "
                + "assigned_area VARCHAR(100), role VARCHAR(10) NOT NULL, "
                + "password_salt CHAR(32) NOT NULL, password_hash CHAR(64) NOT NULL, "
                + "active BOOLEAN NOT NULL DEFAULT TRUE, "
                + "CHECK (role IN ('ADMIN','USER'))) ENGINE=InnoDB";
        String wasteSql = "CREATE TABLE IF NOT EXISTS waste ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, waste_type VARCHAR(30) NOT NULL, "
                + "description VARCHAR(255) NOT NULL, quantity DECIMAL(10,2) NOT NULL, "
                + "location VARCHAR(100) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'PENDING', "
                + "created_by VARCHAR(40), collected_by VARCHAR(40), collected_at DATETIME NOT NULL, "
                + "invalid_reason VARCHAR(255), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                + "INDEX idx_waste_report (status, collected_at), "
                + "INDEX idx_waste_collector (collected_by, status), "
                + "INDEX idx_waste_area (location, status), "
                + "CONSTRAINT fk_waste_creator FOREIGN KEY (created_by) REFERENCES app_users(username), "
                + "CONSTRAINT fk_waste_collector FOREIGN KEY (collected_by) REFERENCES app_users(username), "
                + "CHECK (waste_type IN ('Organic','Recyclable','Hazardous')), "
                + "CHECK (status IN ('PENDING','COMPLETED','INVALID')), "
                + "CHECK (quantity > 0)) ENGINE=InnoDB";
        String announcementSql = "CREATE TABLE IF NOT EXISTS announcements ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(120) NOT NULL, "
                + "message TEXT NOT NULL, created_by VARCHAR(40) NOT NULL, "
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "CONSTRAINT fk_announcement_author FOREIGN KEY (created_by) REFERENCES app_users(username)) ENGINE=InnoDB";
        String readSql = "CREATE TABLE IF NOT EXISTS announcement_reads ("
                + "announcement_id INT NOT NULL, username VARCHAR(40) NOT NULL, "
                + "read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "PRIMARY KEY (announcement_id, username), "
                + "CONSTRAINT fk_announcement_read_announcement FOREIGN KEY (announcement_id) REFERENCES announcements(id) ON DELETE CASCADE, "
                + "CONSTRAINT fk_announcement_read_user FOREIGN KEY (username) REFERENCES app_users(username) ON DELETE CASCADE) ENGINE=InnoDB";
        String completionSql = "CREATE TABLE IF NOT EXISTS completion_requests ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY, waste_id INT NOT NULL, requested_by VARCHAR(40) NOT NULL, "
                + "requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, request_status VARCHAR(12) NOT NULL DEFAULT 'PENDING', "
                + "reviewed_by VARCHAR(40), reviewed_at TIMESTAMP NULL, "
                + "CONSTRAINT fk_completion_waste FOREIGN KEY (waste_id) REFERENCES waste(id) ON DELETE CASCADE, "
                + "CONSTRAINT fk_completion_requester FOREIGN KEY (requested_by) REFERENCES app_users(username), "
                + "CONSTRAINT fk_completion_reviewer FOREIGN KEY (reviewed_by) REFERENCES app_users(username), "
                + "CHECK (request_status IN ('PENDING','APPROVED','REJECTED','CANCELLED')), "
                + "INDEX idx_completion_status_requested (request_status, requested_at)) ENGINE=InnoDB";
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(usersSql);
            statement.executeUpdate(wasteSql);
            statement.executeUpdate(announcementSql);
            statement.executeUpdate(readSql);
            statement.executeUpdate(completionSql);
        }
    }

    private static void migrateWasteTable() throws SQLException {
        addColumnIfMissing("created_by", "VARCHAR(40) NULL");
        addColumnIfMissing("collected_by", "VARCHAR(40) NULL");
        addColumnIfMissing("collected_at", "DATETIME NULL");
        addColumnIfMissing("invalid_reason", "VARCHAR(255) NULL");
        addColumnIfMissing("created_at", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        addColumnIfMissing("updated_at", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        addIndexIfMissing("idx_waste_report", "status, collected_at");
        addIndexIfMissing("idx_waste_collector", "collected_by, status");
        addIndexIfMissing("idx_waste_area", "location, status");

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE waste SET collected_at = COALESCE(collected_at, created_at, CURRENT_TIMESTAMP)");
            statement.executeUpdate("UPDATE waste SET status = CASE "
                    + "WHEN UPPER(status) IN ('COLLECTED','COMPLETED') THEN 'COMPLETED' "
                    + "WHEN UPPER(status) = 'INVALID' THEN 'INVALID' ELSE 'PENDING' END");
        }
    }

    private static void addColumnIfMissing(String column, String definition) throws SQLException {
        try (Connection connection = getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, "waste", column)) {
                if (columns.next()) return;
            }
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE waste ADD COLUMN " + column + " " + definition);
            }
        }
    }

    private static void addIndexIfMissing(String indexName, String columns) throws SQLException {
        try (Connection connection = getConnection();
             ResultSet indexes = connection.getMetaData().getIndexInfo(connection.getCatalog(), null,
                     "waste", false, false)) {
            while (indexes.next()) {
                if (indexName.equalsIgnoreCase(indexes.getString("INDEX_NAME"))) return;
            }
        }
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE INDEX " + indexName + " ON waste (" + columns + ")");
        }
    }

    private static void seedAccounts() throws SQLException {
        String sql = "INSERT IGNORE INTO app_users "
                + "(username, display_name, assigned_area, role, password_salt, password_hash) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (!accountExists(connection, "deepthirani")) {
                String initialAdminPassword = setting("WASTE_ADMIN_INITIAL_PASSWORD", null);
                if (initialAdminPassword == null) {
                    throw new SQLException("Set WASTE_ADMIN_INITIAL_PASSWORD before the first application launch.");
                }
                String adminSalt = PasswordHasher.newSalt();
                char[] adminPassword = initialAdminPassword.toCharArray();
                String adminHash = PasswordHasher.hash(adminPassword, adminSalt);
                java.util.Arrays.fill(adminPassword, '\0');
                statement.setString(1, "deepthirani");
                statement.setString(2, "Deepthi Rani");
                statement.setString(3, null);
                statement.setString(4, "ADMIN");
                statement.setString(5, adminSalt);
                statement.setString(6, adminHash);
                statement.executeUpdate();
            }

            for (String[] member : MEMBERS) {
                if (accountExists(connection, member[0])) continue;
                String salt = PasswordHasher.newSalt();
                char[] initialPassword = (member[0] + "_clean").toCharArray();
                String hash = PasswordHasher.hash(initialPassword, salt);
                java.util.Arrays.fill(initialPassword, '\0');
                statement.setString(1, member[0]);
                statement.setString(2, member[1]);
                statement.setString(3, member[2]);
                statement.setString(4, "USER");
                statement.setString(5, salt);
                statement.setString(6, hash);
                statement.executeUpdate();
            }
        }
    }

    private static boolean accountExists(Connection connection, String username) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM app_users WHERE username=?")) {
            statement.setString(1, username);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }
}
