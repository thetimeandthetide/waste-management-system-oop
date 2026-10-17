package main;

import dao.DatabaseManager;
import gui.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.concurrent.ExecutionException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame startup = new JFrame("Waste Management System");
            JLabel status = new JLabel("Connecting to MySQL and preparing the workspace...", JLabel.CENTER);
            startup.add(status, BorderLayout.CENTER);
            startup.setSize(new Dimension(440, 100));
            startup.setLocationRelativeTo(null);
            startup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            startup.setVisible(true);

            SwingWorker<Void, Void> initializer = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws SQLException {
                    DatabaseManager.initializeDatabase();
                    return null;
                }

                @Override
                protected void done() {
                    startup.dispose();
                    try {
                        get();
                        new LoginFrame().setVisible(true);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        showDatabaseError(exception);
                    } catch (ExecutionException exception) {
                        showDatabaseError(exception.getCause());
                    }
                }
            };
            initializer.execute();
        });
    }

    private static void showDatabaseError(Throwable exception) {
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
}
