# Waste Management System Code Study Pack

> Update notice: the original notes below describe the first CRUD-only version. The current application adds login, roles, collected timestamps, invalid/completed states, reports, and CSV export. Use [ROLE_REPORTS_VIVA_ADDENDUM.md](ROLE_REPORTS_VIVA_ADDENDUM.md) as the source of truth for these new files and behaviors; some older GUI/database details below no longer match the current source.

Use this document for tomorrow's presentation and viva. It follows the same flow as your PPT: idea, design, OOP, database, Swing GUI, implementation, and run commands.

## 1. One-Minute Project Explanation

This project is a Java desktop application for managing waste records. The GUI is built using Java Swing. Data is stored in a MySQL database. Java connects to MySQL using JDBC and the MySQL Connector/J driver. The project demonstrates OOP using an abstract Waste class and three child classes: OrganicWaste, RecyclableWaste and HazardousWaste. The GUI supports add, view, update, delete, search and mark collected operations.

## 2. How The Code Connects Together

```text
Main.java
  calls DatabaseManager.initializeDatabase()
  creates WasteManagementGUI

WasteManagementGUI.java
  displays Swing form/table/buttons
  creates Waste child objects from user input
  calls WasteManager methods

WasteManager.java
  runs CRUD SQL queries using JDBC
  converts ResultSet rows back into Waste objects

DatabaseManager.java
  opens MySQL connection
  creates database/table automatically

MySQL waste table
  stores id, waste_type, description, quantity, location, status
```

## 3. Fast Viva Answers

- Swing: Java toolkit for desktop GUI applications.

- JDBC: Java API for connecting to databases.

- MySQL Connector/J: Driver that allows JDBC to communicate with MySQL.

- DAO: Data Access Object, a class that separates database logic from GUI logic.

- PreparedStatement: Safer SQL object that uses placeholders instead of directly joining user input into SQL.

- JFrame: Main application window.

- JTable: Swing component used to display rows and columns.

- Event handling: Button clicks use addActionListener().

- Encapsulation: private fields with getters/setters in Waste.

- Abstraction: abstract Waste class and abstract getWasteCategory().

- Inheritance: OrganicWaste, RecyclableWaste and HazardousWaste extend Waste.

- Polymorphism: getWasteCategory() gives different output depending on child class.

## 4. How To Run

### macOS/Linux
```bash
cd /Users/josephfidel/Desktop/WasteManagementSystem/DesktopApplication
brew services start mysql
./build.sh
./run.sh
```

### Windows
```bat
cd DesktopApplication
build.bat
run.bat
```

### MySQL Check
```sql
SHOW DATABASES;
USE waste_management_db;
SHOW TABLES;
DESCRIBE waste;
SELECT * FROM waste;
```

## 5. File By File Code And Explanation

## Model: Waste.java

**Purpose:** Parent abstract class for all waste records. Important viva points: abstraction because the class is abstract, encapsulation because fields are private, and polymorphism because it declares getWasteCategory() for child classes to override.

**Path:** `src/model/Waste.java`

```java
package model;

// Abstraction: Waste is abstract because a general "Waste" record needs a real category.
public abstract class Waste {
    // Encapsulation: fields are private and accessed through getters/setters.
    private int id;
    private String description;
    private double quantity;
    private String location;
    private String status;

    public Waste() {
    }

    public Waste(int id, String description, double quantity, String location, String status) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.location = location;
        this.status = status;
    }

    // Polymorphism: each child class gives its own category.
    public abstract String getWasteCategory();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

```

## Model: OrganicWaste.java

**Purpose:** Child class of Waste. It demonstrates inheritance and method overriding. getWasteCategory() returns Organic.

**Path:** `src/model/OrganicWaste.java`

```java
package model;

// Inheritance: OrganicWaste extends the abstract Waste class.
public class OrganicWaste extends Waste {
    public OrganicWaste() {
    }

    public OrganicWaste(int id, String description, double quantity, String location, String status) {
        super(id, description, quantity, location, status);
    }

    @Override
    public String getWasteCategory() {
        return "Organic";
    }
}

```

## Model: RecyclableWaste.java

**Purpose:** Child class of Waste. It reuses all common fields from Waste and only changes category behavior.

**Path:** `src/model/RecyclableWaste.java`

```java
package model;

// Inheritance: RecyclableWaste extends the abstract Waste class.
public class RecyclableWaste extends Waste {
    public RecyclableWaste() {
    }

    public RecyclableWaste(int id, String description, double quantity, String location, String status) {
        super(id, description, quantity, location, status);
    }

    @Override
    public String getWasteCategory() {
        return "Recyclable";
    }
}

```

## Model: HazardousWaste.java

**Purpose:** Child class of Waste. It returns Hazardous as the category.

**Path:** `src/model/HazardousWaste.java`

```java
package model;

// Inheritance: HazardousWaste extends the abstract Waste class.
public class HazardousWaste extends Waste {
    public HazardousWaste() {
    }

    public HazardousWaste(int id, String description, double quantity, String location, String status) {
        super(id, description, quantity, location, status);
    }

    @Override
    public String getWasteCategory() {
        return "Hazardous";
    }
}

```

## DAO: DatabaseManager.java

**Purpose:** Central JDBC connection and automatic database setup class. It stores the JDBC URL, username and password, creates the database/table if missing, and inserts starter data if the table is empty.

**Path:** `src/dao/DatabaseManager.java`

```java
package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String DATABASE_NAME = "waste_management_db";
    private static final String DATABASE_URL = SERVER_URL + DATABASE_NAME;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
    }

    public static void initializeDatabase() throws SQLException {
        createDatabaseIfNeeded();
        createWasteTableIfNeeded();
        insertStarterRecordsIfEmpty();
    }

    private static void createDatabaseIfNeeded() throws SQLException {
        String sql = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;

        try (Connection connection = DriverManager.getConnection(SERVER_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
        }
    }

    private static void createWasteTableIfNeeded() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS waste ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "waste_type VARCHAR(50) NOT NULL, "
                + "description VARCHAR(255) NOT NULL, "
                + "quantity DOUBLE NOT NULL, "
                + "location VARCHAR(100) NOT NULL, "
                + "status VARCHAR(50) NOT NULL"
                + ")";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
        }
    }

    private static void insertStarterRecordsIfEmpty() throws SQLException {
        String countSql = "SELECT COUNT(*) FROM waste";
        String insertSql = "INSERT INTO waste (waste_type, description, quantity, location, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             Statement countStatement = connection.createStatement();
             ResultSet resultSet = countStatement.executeQuery(countSql)) {

            resultSet.next();
            if (resultSet.getInt(1) > 0) {
                return;
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                insertStarterRecord(insertStatement, "Organic", "Food waste from cafeteria", 12.5, "College Canteen", "Pending");
                insertStarterRecord(insertStatement, "Recyclable", "Plastic bottles", 8.0, "Block A", "Pending");
                insertStarterRecord(insertStatement, "Hazardous", "Used batteries", 3.0, "Lab Room 2", "Collected");
            }
        }
    }

    private static void insertStarterRecord(PreparedStatement statement, String type, String description,
                                            double quantity, String location, String status) throws SQLException {
        statement.setString(1, type);
        statement.setString(2, description);
        statement.setDouble(3, quantity);
        statement.setString(4, location);
        statement.setString(5, status);
        statement.executeUpdate();
    }
}

```

## DAO: WasteManager.java

**Purpose:** DAO class for CRUD. It contains INSERT, SELECT, SEARCH, UPDATE, DELETE and mark collected logic. It uses PreparedStatement for safer SQL.

**Path:** `src/dao/WasteManager.java`

```java
package dao;

import model.HazardousWaste;
import model.OrganicWaste;
import model.RecyclableWaste;
import model.Waste;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class WasteManager {
    public void addWaste(Waste waste) throws SQLException {
        String sql = "INSERT INTO waste (waste_type, description, quantity, location, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, waste.getWasteCategory());
            statement.setString(2, waste.getDescription());
            statement.setDouble(3, waste.getQuantity());
            statement.setString(4, waste.getLocation());
            statement.setString(5, waste.getStatus());
            statement.executeUpdate();
        }
    }

    public List<Waste> getAllWaste() throws SQLException {
        List<Waste> wasteList = new ArrayList<>();
        String sql = "SELECT * FROM waste ORDER BY id";

        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                wasteList.add(createWasteFromResultSet(resultSet));
            }
        }

        return wasteList;
    }

    public List<Waste> searchWaste(String keyword) throws SQLException {
        List<Waste> wasteList = new ArrayList<>();
        String sql = "SELECT * FROM waste WHERE description LIKE ? OR location LIKE ? OR waste_type LIKE ? OR status LIKE ? ORDER BY id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchText = "%" + keyword + "%";
            statement.setString(1, searchText);
            statement.setString(2, searchText);
            statement.setString(3, searchText);
            statement.setString(4, searchText);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    wasteList.add(createWasteFromResultSet(resultSet));
                }
            }
        }

        return wasteList;
    }

    public void updateWaste(Waste waste) throws SQLException {
        String sql = "UPDATE waste SET waste_type = ?, description = ?, quantity = ?, location = ?, status = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, waste.getWasteCategory());
            statement.setString(2, waste.getDescription());
            statement.setDouble(3, waste.getQuantity());
            statement.setString(4, waste.getLocation());
            statement.setString(5, waste.getStatus());
            statement.setInt(6, waste.getId());
            statement.executeUpdate();
        }
    }

    public void deleteWaste(int id) throws SQLException {
        String sql = "DELETE FROM waste WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void markAsCollected(int id) throws SQLException {
        String sql = "UPDATE waste SET status = 'Collected' WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private Waste createWasteFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String wasteType = resultSet.getString("waste_type");
        String description = resultSet.getString("description");
        double quantity = resultSet.getDouble("quantity");
        String location = resultSet.getString("location");
        String status = resultSet.getString("status");

        if ("Organic".equalsIgnoreCase(wasteType)) {
            return new OrganicWaste(id, description, quantity, location, status);
        } else if ("Recyclable".equalsIgnoreCase(wasteType)) {
            return new RecyclableWaste(id, description, quantity, location, status);
        } else {
            return new HazardousWaste(id, description, quantity, location, status);
        }
    }
}

```

## GUI: WasteManagementGUI.java

**Purpose:** Swing interface. It extends JFrame, creates form inputs, table, buttons, event listeners and custom styling. Button clicks call DAO methods through WasteManager.

**Path:** `src/gui/WasteManagementGUI.java`

```java
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

```

## Main: Main.java

**Purpose:** Program entry point. It starts Swing through SwingUtilities.invokeLater(), initializes the database, creates the GUI object and opens the window.

**Path:** `src/main/Main.java`

```java
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

```

## SQL: setup.sql

**Purpose:** Manual database setup script. The app can auto-create the database, but this SQL helps show the schema in viva.

**Path:** `sql/setup.sql`

```sql
DROP DATABASE IF EXISTS waste_management_db;
CREATE DATABASE waste_management_db;

USE waste_management_db;

CREATE TABLE IF NOT EXISTS waste (
    id INT AUTO_INCREMENT PRIMARY KEY,
    waste_type VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity DOUBLE NOT NULL,
    location VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL
);

INSERT INTO waste (waste_type, description, quantity, location, status)
VALUES
('Organic', 'Food waste from cafeteria', 12.5, 'College Canteen', 'Pending'),
('Recyclable', 'Plastic bottles', 8.0, 'Block A', 'Pending'),
('Hazardous', 'Used batteries', 3.0, 'Lab Room 2', 'Collected');

```

## Build: build.sh

**Purpose:** macOS/Linux build script. It compiles Java source files and creates the runnable JAR.

**Path:** `build.sh`

```bash
#!/bin/sh
set -e

mkdir -p build dist
find build -type f -name "*.class" -delete

javac -cp "lib/mysql-connector-j.jar" -d build $(find src -name "*.java")
jar cfm dist/WasteManagementSystem.jar manifest.txt -C build .

echo "Build complete: dist/WasteManagementSystem.jar"

```

## Run: run.sh

**Purpose:** macOS/Linux run script. It runs the generated JAR.

**Path:** `run.sh`

```bash
#!/bin/sh
set -e

java -jar dist/WasteManagementSystem.jar

```

## Windows Build: build.bat

**Purpose:** Windows build script. Same purpose as build.sh but for Command Prompt.

**Path:** `build.bat`

```bat
@echo off
if not exist build mkdir build
if not exist dist mkdir dist

for /R build %%f in (*.class) do del "%%f"

javac -cp "lib\mysql-connector-j.jar" -d build src\model\*.java src\dao\*.java src\gui\*.java src\main\*.java
jar cfm dist\WasteManagementSystem.jar manifest.txt -C build .

echo Build complete: dist\WasteManagementSystem.jar

```

## Windows Run: run.bat

**Purpose:** Windows run script. It starts the JAR on Windows.

**Path:** `run.bat`

```bat
@echo off
java -jar dist\WasteManagementSystem.jar

```

## JAR Manifest: manifest.txt

**Purpose:** JAR metadata. It tells Java the main class and where the MySQL connector JAR is located.

**Path:** `manifest.txt`

```text
Main-Class: main.Main
Class-Path: ../lib/mysql-connector-j.jar


```

## 6. Questions Sir May Ask During Presentation

**Q: Why did you use Swing?**

A: Because the requirement is a Java desktop application. Swing is built into Java and provides JFrame, JButton, JTable and other GUI components.

**Q: Why not web application?**

A: The project requirement is desktop only, so we used Swing instead of HTML, CSS frameworks, Spring or web technologies.

**Q: Where is abstraction?**

A: Waste.java is abstract and has abstract method getWasteCategory().

**Q: Where is encapsulation?**

A: Waste fields are private and accessed through getters and setters.

**Q: Where is inheritance?**

A: OrganicWaste, RecyclableWaste and HazardousWaste extend Waste.

**Q: Where is polymorphism?**

A: getWasteCategory() is overridden in each subclass and returns different values.

**Q: How does Add work?**

A: GUI reads form values, creates a Waste subclass object, calls WasteManager.addWaste(), which runs INSERT using PreparedStatement.

**Q: How does View work?**

A: WasteManager.getAllWaste() runs SELECT, converts ResultSet rows into Waste objects, and GUI displays them in JTable.

**Q: How does Search work?**

A: WasteManager.searchWaste() uses SQL LIKE placeholders for description, location, waste_type and status.

**Q: Why use PreparedStatement?**

A: It separates query structure from values and reduces SQL injection risk.

**Q: What is ResultSet?**

A: ResultSet stores rows returned by a SELECT query.

**Q: What is the role of DatabaseManager?**

A: It stores connection settings, returns Connection objects and initializes database/table.

**Q: What is the role of WasteManager?**

A: It is the DAO class that performs CRUD database operations.

**Q: What happens if MySQL is not running?**

A: Main catches SQLException and shows a JOptionPane database connection error.

**Q: Why is ID not editable?**

A: Because MySQL auto-generates ID using AUTO_INCREMENT.

**Q: How does table row selection work?**

A: A ListSelectionListener calls fillFormFromSelectedRow(), which copies selected row data to the form.

**Q: How is collected status changed?**

A: markAsCollected(id) runs UPDATE waste SET status = Collected WHERE id = ?.

## 7. Presentation Flow To Speak

1. Introduce project as Java Swing desktop app with MySQL backend.

2. Explain the problem: manual waste records are hard to search and update.

3. Explain features: add, view, update, delete, search, mark collected.

4. Explain architecture: GUI, model, DAO, MySQL.

5. Explain OOP: Waste abstract class and three child classes.

6. Explain JDBC: DatabaseManager connects and WasteManager performs SQL.

7. Explain Swing: JFrame window, form inputs, JTable, buttons and listeners.

8. Explain MySQL table: waste with id, waste_type, description, quantity, location, status.

9. Show run commands and database verification commands.

10. Finish with limitations and future enhancements from PPT.
