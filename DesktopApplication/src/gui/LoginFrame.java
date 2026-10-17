package gui;

import dao.AuthenticationManager;
import dao.UserManager;
import model.AppUser;
import model.RegistrationResult;

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
    private final UserManager userManager = new UserManager();
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame() {
        setTitle("Waste Management System | Sign in");
        setSize(480, 450);
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
        signIn.setUI(new CleanButtonUI());
        signIn.setOpaque(true);
        signIn.setContentAreaFilled(true);
        signIn.setBorderPainted(true);
        signIn.setBackground(new Color(89, 70, 168));
        signIn.setForeground(Color.WHITE);
        signIn.setFocusPainted(false);
        signIn.setBorder(new EmptyBorder(12, 15, 12, 15));
        signIn.addActionListener(event -> signIn());
        passwordField.addActionListener(event -> signIn());

        page.add(brand, BorderLayout.NORTH);
        page.add(form, BorderLayout.CENTER);
        JButton register = new JButton("Join Clean CET");
        register.setUI(new CleanButtonUI());
        register.setOpaque(true);
        register.setContentAreaFilled(true);
        register.setBackground(new Color(246, 244, 251));
        register.setForeground(new Color(54, 42, 112));
        register.setFocusPainted(false);
        register.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 226)), new EmptyBorder(10, 15, 10, 15)));
        register.addActionListener(event -> registerMember());
        JPanel actions = new JPanel(new GridLayout(2, 1, 0, 8));
        actions.setOpaque(false);
        actions.add(signIn);
        actions.add(register);
        page.add(actions, BorderLayout.SOUTH);
        setContentPane(page);
    }

    private void registerMember() {
        JTextField name = new JTextField();
        JTextField dateOfBirth = new JTextField();
        JTextField registerNumber = new JTextField();
        JTextField department = new JTextField();
        styleInput(name);
        styleInput(dateOfBirth);
        styleInput(registerNumber);
        styleInput(department);

        JPanel fields = new JPanel(new GridLayout(4, 1, 0, 8));
        fields.add(labeled("Full name", name));
        fields.add(labeled("Date of birth (yyyy-MM-dd)", dateOfBirth));
        fields.add(labeled("CET register number (example: TVE25CSE068)", registerNumber));
        fields.add(labeled("Department", department));
        fields.setPreferredSize(new java.awt.Dimension(430, 340));
        if (JOptionPane.showConfirmDialog(this, fields, "Join Clean CET", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        try {
            RegistrationResult registration = userManager.register(name.getText(), dateOfBirth.getText(),
                    registerNumber.getText(), department.getText());
            AppUser newUser = registration.getUser();
            String message = "Your account is ready.\n\n"
                    + "Name: " + newUser.getDisplayName() + "\n"
                    + "Username: " + newUser.getUsername() + "\n"
                    + "Temporary password: " + registration.getTemporaryPassword() + "\n"
                    + "Assigned area: " + newUser.getAssignedArea() + "\n\n"
                    + "This password follows the requested username_clean pattern and is predictable. "
                    + "Use it only for this classroom project. Your password is stored as a salted hash.";
            JOptionPane.showMessageDialog(this, message, "Registration complete", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new WasteManagementGUI(newUser).setVisible(true);
        } catch (IllegalArgumentException | SQLException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Registration could not be completed",
                    JOptionPane.ERROR_MESSAGE);
        }
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

    private JPanel labeled(String text, JTextField field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        JLabel title = label(text);
        wrapper.add(title, BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    private void styleInput(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 226)),
                new EmptyBorder(8, 10, 8, 10)));
    }
}
