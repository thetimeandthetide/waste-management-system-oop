package gui;

import dao.WasteManager;
import model.HazardousWaste;
import model.OrganicWaste;
import model.RecyclableWaste;
import model.Waste;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

public class WasteManagementGUI extends JFrame {
    private static final Color BACKGROUND_TOP = new Color(232, 248, 241);
    private static final Color BACKGROUND_BOTTOM = new Color(190, 226, 214);
    private static final Color PANEL_COLOR = new Color(250, 255, 252);
    private static final Color ROW_ALT_COLOR = new Color(242, 250, 246);
    private static final Color PRIMARY_COLOR = new Color(28, 117, 91);
    private static final Color PRIMARY_HOVER = new Color(36, 139, 107);
    private static final Color PRIMARY_DARK = new Color(17, 86, 69);
    private static final Color TEXT_COLOR = new Color(38, 57, 50);
    private static final Color BORDER_COLOR = new Color(188, 214, 203);

    private final WasteManager wasteManager;

    private JTextField idField;
    private JComboBox<String> typeComboBox;
    private JTextField descriptionField;
    private JTextField quantityField;
    private JTextField locationField;
    private JComboBox<String> statusComboBox;
    private JTextField searchField;
    private JTable wasteTable;
    private DefaultTableModel tableModel;

    public WasteManagementGUI() {
        wasteManager = new WasteManager();

        setModernLookAndFeel();
        setTitle("Waste Management System");
        setSize(1040, 680);
        setMinimumSize(new Dimension(920, 590));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(18, 18));
        mainPanel.setBorder(new EmptyBorder(18, 22, 18, 22));
        setContentPane(mainPanel);

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        loadAllWaste();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new HeaderPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(22, 28, 22, 28));
        JLabel titleLabel = new JLabel("Waste Management System");
        JLabel subtitleLabel = new JLabel("Track, update, and collect waste records with Java Swing and MySQL");

        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 31));
        titleLabel.setForeground(Color.WHITE);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(221, 245, 237));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        return headerPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = transparentPanel(new BorderLayout(18, 18));
        centerPanel.add(createFormPanel(), BorderLayout.NORTH);
        centerPanel.add(createTablePanel(), BorderLayout.CENTER);
        return centerPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = cardPanel(new GridLayout(3, 4, 14, 10));

        idField = new JTextField();
        idField.setEditable(false);

        typeComboBox = new JComboBox<>(new String[]{"Organic", "Recyclable", "Hazardous"});
        descriptionField = new JTextField();
        quantityField = new JTextField();
        locationField = new JTextField();
        statusComboBox = new JComboBox<>(new String[]{"Pending", "Collected"});
        searchField = new JTextField();

        styleInput(idField);
        styleInput(descriptionField);
        styleInput(quantityField);
        styleInput(locationField);
        styleInput(searchField);
        styleComboBox(typeComboBox);
        styleComboBox(statusComboBox);

        formPanel.add(label("ID"));
        formPanel.add(idField);
        formPanel.add(label("Waste Type"));
        formPanel.add(typeComboBox);

        formPanel.add(label("Description"));
        formPanel.add(descriptionField);
        formPanel.add(label("Quantity (kg)"));
        formPanel.add(quantityField);

        formPanel.add(label("Location"));
        formPanel.add(locationField);
        formPanel.add(label("Status"));
        formPanel.add(statusComboBox);

        return formPanel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"ID", "Type", "Description", "Quantity", "Location", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        wasteTable = new JTable(tableModel);
        wasteTable.setRowHeight(34);
        wasteTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        wasteTable.setForeground(TEXT_COLOR);
        wasteTable.setSelectionBackground(new Color(201, 237, 223));
        wasteTable.setSelectionForeground(TEXT_COLOR);
        wasteTable.setShowGrid(false);
        wasteTable.setIntercellSpacing(new Dimension(0, 0));
        wasteTable.setDefaultRenderer(Object.class, new WasteTableRenderer());
        wasteTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        wasteTable.getTableHeader().setBackground(PRIMARY_COLOR);
        wasteTable.getTableHeader().setForeground(Color.WHITE);
        wasteTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
        wasteTable.getTableHeader().setReorderingAllowed(false);

        wasteTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && wasteTable.getSelectedRow() != -1) {
                fillFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(wasteTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 0, 0, 0)
        ));
        scrollPane.getViewport().setBackground(PANEL_COLOR);
        return scrollPane;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = cardPanel(new BorderLayout(12, 12));
        JPanel actionPanel = transparentPanel(new GridLayout(1, 7, 10, 10));
        JPanel searchPanel = transparentPanel(new BorderLayout(10, 0));

        JButton addButton = createButton("Add");
        JButton viewButton = createButton("View All");
        JButton updateButton = createButton("Update");
        JButton deleteButton = createButton("Delete");
        JButton searchButton = createButton("Search");
        JButton collectedButton = createButton("Mark Collected");
        JButton clearButton = createButton("Clear");

        // Swing event handling: button clicks call methods.
        addButton.addActionListener(event -> addWaste());
        viewButton.addActionListener(event -> loadAllWaste());
        updateButton.addActionListener(event -> updateWaste());
        deleteButton.addActionListener(event -> deleteWaste());
        searchButton.addActionListener(event -> searchWaste());
        collectedButton.addActionListener(event -> markAsCollected());
        clearButton.addActionListener(event -> clearForm());

        actionPanel.add(addButton);
        actionPanel.add(viewButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        actionPanel.add(collectedButton);
        actionPanel.add(clearButton);
        actionPanel.add(searchButton);

        searchPanel.add(label("Search"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        buttonPanel.add(searchPanel, BorderLayout.NORTH);
        buttonPanel.add(actionPanel, BorderLayout.CENTER);
        return buttonPanel;
    }

    private void addWaste() {
        try {
            Waste waste = createWasteObject(0);
            wasteManager.addWaste(waste);
            showMessage("Waste record added successfully.");
            clearForm();
            loadAllWaste();
        } catch (NumberFormatException exception) {
            showError("Quantity must be a valid number.");
        } catch (SQLException exception) {
            showError("Database error: " + exception.getMessage());
        }
    }

    private void loadAllWaste() {
        try {
            List<Waste> wasteList = wasteManager.getAllWaste();
            showWasteInTable(wasteList);
        } catch (SQLException exception) {
            showError("Database error: " + exception.getMessage());
        }
    }

    private void updateWaste() {
        try {
            int id = Integer.parseInt(idField.getText());
            Waste waste = createWasteObject(id);
            wasteManager.updateWaste(waste);
            showMessage("Waste record updated successfully.");
            clearForm();
            loadAllWaste();
        } catch (NumberFormatException exception) {
            showError("Select a record and enter a valid quantity.");
        } catch (SQLException exception) {
            showError("Database error: " + exception.getMessage());
        }
    }

    private void deleteWaste() {
        try {
            int id = Integer.parseInt(idField.getText());
            wasteManager.deleteWaste(id);
            showMessage("Waste record deleted successfully.");
            clearForm();
            loadAllWaste();
        } catch (NumberFormatException exception) {
            showError("Select a record to delete.");
        } catch (SQLException exception) {
            showError("Database error: " + exception.getMessage());
        }
    }

    private void searchWaste() {
        try {
            List<Waste> wasteList = wasteManager.searchWaste(searchField.getText());
            showWasteInTable(wasteList);
        } catch (SQLException exception) {
            showError("Database error: " + exception.getMessage());
        }
    }

    private void markAsCollected() {
        try {
            int id = Integer.parseInt(idField.getText());
            wasteManager.markAsCollected(id);
            showMessage("Waste marked as collected.");
            clearForm();
            loadAllWaste();
        } catch (NumberFormatException exception) {
            showError("Select a record to mark as collected.");
        } catch (SQLException exception) {
            showError("Database error: " + exception.getMessage());
        }
    }

    private Waste createWasteObject(int id) {
        String type = (String) typeComboBox.getSelectedItem();
        String description = descriptionField.getText();
        double quantity = Double.parseDouble(quantityField.getText());
        String location = locationField.getText();
        String status = (String) statusComboBox.getSelectedItem();

        if ("Organic".equals(type)) {
            return new OrganicWaste(id, description, quantity, location, status);
        } else if ("Recyclable".equals(type)) {
            return new RecyclableWaste(id, description, quantity, location, status);
        } else {
            return new HazardousWaste(id, description, quantity, location, status);
        }
    }

    private void showWasteInTable(List<Waste> wasteList) {
        tableModel.setRowCount(0);

        for (Waste waste : wasteList) {
            Object[] row = {
                    waste.getId(),
                    waste.getWasteCategory(),
                    waste.getDescription(),
                    waste.getQuantity(),
                    waste.getLocation(),
                    waste.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void fillFormFromSelectedRow() {
        int row = wasteTable.getSelectedRow();

        idField.setText(tableModel.getValueAt(row, 0).toString());
        typeComboBox.setSelectedItem(tableModel.getValueAt(row, 1).toString());
        descriptionField.setText(tableModel.getValueAt(row, 2).toString());
        quantityField.setText(tableModel.getValueAt(row, 3).toString());
        locationField.setText(tableModel.getValueAt(row, 4).toString());
        statusComboBox.setSelectedItem(tableModel.getValueAt(row, 5).toString());
    }

    private void clearForm() {
        idField.setText("");
        typeComboBox.setSelectedIndex(0);
        descriptionField.setText("");
        quantityField.setText("");
        locationField.setText("");
        statusComboBox.setSelectedIndex(0);
        searchField.setText("");
        wasteTable.clearSelection();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void setModernLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            System.out.println("Default look and feel will be used.");
        }
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(TEXT_COLOR);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }

    private JPanel cardPanel(java.awt.LayoutManager layout) {
        JPanel panel = new RoundedPanel(layout);
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        return panel;
    }

    private JPanel transparentPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private void styleInput(JTextField textField) {
        textField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textField.setForeground(TEXT_COLOR);
        textField.setBackground(Color.WHITE);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setBackground(Color.WHITE);
    }

    private JButton createButton(String text) {
        JButton button = new RoundedButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 10, 10, 10));
        button.setMargin(new Insets(8, 8, 8, 8));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        return button;
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D graphics2D = (Graphics2D) graphics;
            GradientPaint gradient = new GradientPaint(
                    0, 0, BACKGROUND_TOP,
                    0, getHeight(), BACKGROUND_BOTTOM
            );
            graphics2D.setPaint(gradient);
            graphics2D.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private static class HeaderPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_DARK,
                    getWidth(), getHeight(), PRIMARY_COLOR
            );
            graphics2D.setPaint(gradient);
            graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    private static class RoundedPanel extends JPanel {
        public RoundedPanel(java.awt.LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(PANEL_COLOR);
            graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            graphics2D.setColor(BORDER_COLOR);
            graphics2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(getBackground());
            graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class WasteTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setHorizontalAlignment(column == 0 || column == 3 ? SwingConstants.CENTER : SwingConstants.LEFT);

            if (isSelected) {
                component.setBackground(table.getSelectionBackground());
                component.setForeground(TEXT_COLOR);
            } else {
                component.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT_COLOR);
                component.setForeground(TEXT_COLOR);
            }

            if (!isSelected && column == 1 && value != null) {
                String type = value.toString();
                if ("Organic".equals(type)) {
                    component.setForeground(new Color(25, 116, 77));
                } else if ("Recyclable".equals(type)) {
                    component.setForeground(new Color(28, 99, 139));
                } else if ("Hazardous".equals(type)) {
                    component.setForeground(new Color(159, 76, 52));
                }
            }

            if (!isSelected && column == 5 && "Collected".equals(String.valueOf(value))) {
                component.setForeground(PRIMARY_DARK);
            }

            return component;
        }
    }
}
