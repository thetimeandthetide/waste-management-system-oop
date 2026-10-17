package gui;

import dao.AuthenticationManager;
import model.AppUser;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private final AuthenticationManager authenticationManager = new AuthenticationManager();
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame() {
        setTitle("Waste Management System | Sign in");
        setSize(470, 390);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setBackground(new Color(246, 246, 250));
        page.setBorder(new EmptyBorder(25, 30, 28, 30));

        JPanel brand = new JPanel(new GridLayout(2, 1, 0, 7));
        brand.setOpaque(false);
        JLabel title = new JLabel("Waste Management", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(new Color(48, 42, 105));
        JLabel subtitle = new JLabel("Sign in to your Clean CET workspace", SwingConstants.CENTER);
        subtitle.setForeground(new Color(103, 103, 119));
        brand.add(title);
        brand.add(subtitle);

        JPanel form = new JPanel(new GridLayout(4, 1, 0, 8));
        form.setOpaque(false);
        form.add(label("Username"));
        form.add(usernameField);
        form.add(label("Password"));
        form.add(passwordField);
        styleInput(usernameField);
        styleInput(passwordField);

        JButton signIn = new JButton("Sign in");
        signIn.setBackground(new Color(89, 70, 168));
        signIn.setForeground(Color.WHITE);
        signIn.setFocusPainted(false);
        signIn.setBorder(new EmptyBorder(12, 15, 12, 15));
        signIn.addActionListener(event -> signIn());
        passwordField.addActionListener(event -> signIn());

        page.add(brand, BorderLayout.NORTH);
        page.add(form, BorderLayout.CENTER);
        page.add(signIn, BorderLayout.SOUTH);
        setContentPane(page);
    }

    private void signIn() {
        char[] password = passwordField.getPassword();
        try {
            AppUser user = authenticationManager.authenticate(usernameField.getText(), password);
            if (user == null) {
                passwordField.setText("");
                JOptionPane.showMessageDialog(this, "Username or password is incorrect.", "Sign-in failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
            new WasteManagementGUI(user).setVisible(true);
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Could not access the database: " + exception.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(58, 57, 72));
        return label;
    }

    private void styleInput(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 226)),
                new EmptyBorder(8, 10, 8, 10)));
    }
}
