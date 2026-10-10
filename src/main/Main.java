package main;

import dao.DatabaseManager;
import gui.WasteManagementGUI;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseManager.initializeDatabase();
                WasteManagementGUI gui = new WasteManagementGUI();
                gui.setVisible(true);
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(
                        null,
                        "Could not connect to MySQL.\n\n"
                                + "Please make sure MySQL is installed and running.\n"
                                + "Then check username/password in dao/DatabaseManager.java.\n\n"
                                + "Error: " + exception.getMessage(),
                        "Database Connection Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
