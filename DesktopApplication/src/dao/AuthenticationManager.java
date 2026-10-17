package dao;

import model.AppUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticationManager {
    public AppUser authenticate(String username, char[] password) throws SQLException {
        String sql = "SELECT username, display_name, assigned_area, role, date_of_birth, register_number, department, "
                + "password_salt, password_hash "
                + "FROM app_users WHERE username = ? AND active = TRUE";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim().toLowerCase());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !PasswordHasher.matches(password,
                        result.getString("password_salt"), result.getString("password_hash"))) {
                    return null;
                }
                return new AppUser(result.getString("username"), result.getString("display_name"),
                        result.getString("assigned_area"), result.getString("role"),
                        result.getString("date_of_birth"), result.getString("register_number"),
                        result.getString("department"));
            }
        }
    }
}
