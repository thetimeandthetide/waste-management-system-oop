package dao;

import model.AppUser;
import model.RegistrationResult;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class UserManager {
    private static final SecureRandom RANDOM = new SecureRandom();

    public RegistrationResult register(String fullName, String dateOfBirth, String registerNumber,
                                       String department) throws SQLException {
        String name = normalizeName(fullName);
        String dobText = required(dateOfBirth, "Date of birth is required.");
        String register = required(registerNumber, "CET register number is required.").toUpperCase(Locale.ROOT);
        String course = required(department, "Department is required.");
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobText);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Enter date of birth as yyyy-MM-dd.");
        }
        if (!dob.isBefore(LocalDate.now())) throw new IllegalArgumentException("Date of birth must be in the past.");
        if (register.length() > 20 || !register.matches("TVE\\d{2}[A-Z]{2,5}\\d{3}")) {
            throw new IllegalArgumentException("Use a CET register number such as TVE25CSE068.");
        }
        if (course.length() > 80) throw new IllegalArgumentException("Department name must be 80 characters or fewer.");

        String firstName = name.split(" ")[0];
        if (!firstName.matches("[\\p{L}][\\p{L}\\p{N}'_-]{0,39}")) {
            throw new IllegalArgumentException("The first name must start with a letter and be 40 characters or fewer.");
        }
        String username = firstName.toLowerCase(Locale.ROOT);
        String temporaryPassword = username + "_clean";
        String salt = PasswordHasher.newSalt();
        char[] password = temporaryPassword.toCharArray();
        String hash;
        try {
            hash = PasswordHasher.hash(password, salt);
        } finally {
            java.util.Arrays.fill(password, '\0');
        }

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ensureNotRegistered(connection, username, register);
                List<String> areas = lockUnassignedAreas(connection);
                if (areas.isEmpty()) throw new SQLException("All registration areas are assigned. Ask the administrator to add more areas.");
                Collections.shuffle(areas, RANDOM);
                String assignedArea = areas.get(0);

                String insert = "INSERT INTO app_users (username, display_name, date_of_birth, register_number, "
                        + "department, assigned_area, role, password_salt, password_hash) "
                        + "VALUES (?, ?, ?, ?, ?, ?, 'USER', ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insert)) {
                    statement.setString(1, username);
                    statement.setString(2, name);
                    statement.setDate(3, Date.valueOf(dob));
                    statement.setString(4, register);
                    statement.setString(5, course);
                    statement.setString(6, assignedArea);
                    statement.setString(7, salt);
                    statement.setString(8, hash);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE registration_areas SET assigned_username=? WHERE area_name=? AND assigned_username IS NULL")) {
                    statement.setString(1, username);
                    statement.setString(2, assignedArea);
                    if (statement.executeUpdate() != 1) throw new SQLException("Area assignment changed. Please try registration again.");
                }
                connection.commit();
                AppUser user = new AppUser(username, name, assignedArea, "USER", dob.toString(), register, course);
                return new RegistrationResult(user, temporaryPassword);
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                if (exception instanceof SQLException) {
                    SQLException sqlException = (SQLException) exception;
                    if (sqlException.getSQLState() != null && sqlException.getSQLState().startsWith("23")) {
                        throw new SQLException("That username or CET register number is already registered.");
                    }
                }
                throw exception;
            }
        }
    }

    public List<Object[]> getMembers(AppUser actor, String keyword) throws SQLException {
        requireAdmin(actor);
        List<Object[]> members = new ArrayList<>();
        String sql = "SELECT display_name, username, COALESCE(DATE_FORMAT(date_of_birth,'%Y-%m-%d'),'Not provided'), "
                + "COALESCE(register_number,'Not provided'), COALESCE(department,'Not provided'), "
                + "COALESCE(assigned_area,'Not assigned'), IF(active,'Active','Inactive') "
                + "FROM app_users WHERE role='USER' AND (display_name LIKE ? OR username LIKE ? "
                + "OR COALESCE(register_number,'') LIKE ? OR COALESCE(department,'') LIKE ? "
                + "OR COALESCE(assigned_area,'') LIKE ?) ORDER BY display_name, username";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String match = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            for (int i = 1; i <= 5; i++) statement.setString(i, match);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) members.add(new Object[]{rows.getString(1), rows.getString(2), rows.getString(3),
                        rows.getString(4), rows.getString(5), rows.getString(6), rows.getString(7)});
            }
        }
        return members;
    }

    public void addRegistrationArea(String areaName, AppUser actor) throws SQLException {
        requireAdmin(actor);
        String area = required(areaName, "Area name is required.");
        if (area.length() > 100) throw new IllegalArgumentException("Area name must be 100 characters or fewer.");
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO registration_areas (area_name) VALUES (?)")) {
            statement.setString(1, area);
            statement.executeUpdate();
        } catch (SQLException exception) {
            if (exception.getSQLState() != null && exception.getSQLState().startsWith("23")) {
                throw new SQLException("That registration area already exists.");
            }
            throw exception;
        }
    }

    private static String normalizeName(String value) {
        String name = required(value, "Full name is required.").replaceAll("\\s+", " ");
        if (name.length() > 100) {
            throw new IllegalArgumentException("Name must be 100 characters or fewer.");
        }
        return name;
    }

    private static String required(String value, String message) {
        String result = value == null ? "" : value.trim();
        if (result.isEmpty()) throw new IllegalArgumentException(message);
        return result;
    }

    private static List<String> lockUnassignedAreas(Connection connection) throws SQLException {
        List<String> areas = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT area_name FROM registration_areas WHERE assigned_username IS NULL FOR UPDATE");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) areas.add(rows.getString(1));
        }
        return areas;
    }

    private static void ensureNotRegistered(Connection connection, String username, String register) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT username, register_number FROM app_users WHERE username=? OR register_number=?")) {
            statement.setString(1, username);
            statement.setString(2, register);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    if (username.equalsIgnoreCase(rows.getString("username"))) {
                        throw new SQLException("The username '" + username + "' already exists. Ask the administrator for access.");
                    }
                    if (register.equalsIgnoreCase(rows.getString("register_number"))) {
                        throw new SQLException("That CET register number is already registered.");
                    }
                }
            }
        }
    }

    private static void requireAdmin(AppUser actor) throws SQLException {
        if (actor == null || !actor.isAdmin()) throw new SQLException("Only an administrator can view the member directory.");
    }
}
