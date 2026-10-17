package main;

import dao.DatabaseManager;
import gui.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseManager.initializeDatabase();
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(
                        null,
                        "Could not connect to MySQL.\n\n"
                                + "Please make sure MySQL is installed and running.\n"
                                + "Check the WASTE_DB_USER and WASTE_DB_PASSWORD settings.\n\n"
                                + "Error: " + exception.getMessage(),
                        "Database Connection Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
