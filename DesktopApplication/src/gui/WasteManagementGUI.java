package gui;

import dao.ReportManager;
import dao.WasteManager;
import model.AppUser;
import model.ReportRow;
import model.ReportSummary;
import model.Waste;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class WasteManagementGUI extends JFrame {
    private static final Color PURPLE = new Color(82, 63, 153);
    private static final Color PURPLE_DARK = new Color(54, 42, 112);
    private static final Color PURPLE_LIGHT = new Color(239, 235, 251);
    private static final Color PAGE = new Color(246, 246, 249);
    private static final Color INK = new Color(40, 40, 52);
    private static final Color MUTED = new Color(111, 111, 126);
    private static final Color LINE = new Color(226, 225, 234);
    private static final String[] AREAS = {
            "Civil canteen", "Mech front", "Dhwani", "Gazebo", "CS front", "Golden walkway",
            "Electrical gate", "Indoor court", "Archie corner", "College front", "DJ hall", "CGPU hall", "CETAA hall"
    };
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AppUser user;
    private final WasteManager wasteManager = new WasteManager();
    private final ReportManager reportManager = new ReportManager();
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageHost = new JPanel(pageLayout);

    private JTextField descriptionField;
    private JTextField quantityField;
    private JTextField collectedAtField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> areaCombo;
    private JComboBox<String> collectorCombo;
    private JTextField searchField;
    private JTable recordsTable;
    private DefaultTableModel recordsModel;
    private int selectedId = -1;
    private String selectedStatus;

    private JTextField fromField;
    private JTextField toField;
    private JLabel totalValue;
    private JLabel organicValue;
    private JLabel recyclableValue;
    private JLabel hazardousValue;
    private JLabel recordCountValue;
    private JTable memberReportTable;
    private DefaultTableModel memberReportModel;
    private JTable areaReportTable;
    private DefaultTableModel areaReportModel;
    private JTable personalReportTable;
    private DefaultTableModel personalReportModel;
    private AreaChart areaChart;
    private List<ReportRow> currentMemberReports = new ArrayList<>();
    private List<ReportRow> currentAreaReports = new ArrayList<>();

    public WasteManagementGUI(AppUser user) {
        this.user = user;
        setTitle("Clean CET | Waste Management");
        setSize(1240, 790);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(PAGE);

        pageHost.setBackground(PAGE);
        pageHost.add(createRecordsPage(), "records");
        pageHost.add(createReportsPage(), "reports");
        add(createSidebar(), BorderLayout.WEST);
        add(createTopBar(), BorderLayout.NORTH);
        add(pageHost, BorderLayout.CENTER);

        refreshRecords();
        refreshReports();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 22));
        sidebar.setBackground(PURPLE_DARK);
        sidebar.setPreferredSize(new Dimension(205, 0));
        sidebar.setBorder(new EmptyBorder(24, 15, 20, 15));

        JLabel brand = new JLabel("CLEAN CET", SwingConstants.CENTER);
        brand.setFont(new Font("SansSerif", Font.BOLD, 19));
        brand.setForeground(Color.WHITE);

        JPanel navigation = new JPanel(new GridLayout(0, 1, 0, 8));
        navigation.setOpaque(false);
        JButton records = navButton("Waste records");
        JButton reports = navButton(user.isAdmin() ? "Reports dashboard" : "My progress");
        records.addActionListener(event -> pageLayout.show(pageHost, "records"));
        reports.addActionListener(event -> {
            refreshReports();
            pageLayout.show(pageHost, "reports");
        });
        navigation.add(records);
        navigation.add(reports);

        JLabel footer = new JLabel("Waste collection tracker", SwingConstants.CENTER);
        footer.setForeground(new Color(211, 205, 235));
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(navigation, BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton navButton(String text) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(Color.WHITE);
        button.setBackground(PURPLE);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 12, 12, 10));
        return button;
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                new EmptyBorder(15, 22, 15, 24)));
        JLabel title = new JLabel("Waste collection workspace");
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(INK);
        String identity = user.isAdmin() ? user.getDisplayName() + "  |  Administrator"
                : user.getDisplayName() + "  |  " + user.getAssignedArea();
        JLabel profile = new JLabel(identity);
        profile.setForeground(MUTED);
        JPanel profileControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        profileControls.setOpaque(false);
        JButton logout = button("Log out", false);
        logout.addActionListener(event -> logout());
        profileControls.add(profile);
        profileControls.add(logout);
        top.add(title, BorderLayout.WEST);
        top.add(profileControls, BorderLayout.EAST);
        return top;
    }

    private void logout() {
        new LoginFrame().setVisible(true);
        dispose();
    }

    private JPanel createRecordsPage() {
        JPanel page = pagePanel(new BorderLayout(16, 16));
        JPanel heading = heading("Waste records", "Log collection activity and follow each record's status.");
        page.add(heading, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setOpaque(false);
        content.add(createRecordTablePanel(), BorderLayout.CENTER);
        content.add(createRecordFormPanel(), BorderLayout.SOUTH);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JScrollPane createRecordTablePanel() {
        JPanel tableWrap = card(new BorderLayout(0, 12));
        JPanel search = new JPanel(new BorderLayout(10, 0));
        search.setOpaque(false);
        searchField = input(new JTextField());
        JButton find = button("Search", false);
        JButton all = button("Refresh", false);
        find.addActionListener(event -> refreshRecords());
        all.addActionListener(event -> { searchField.setText(""); refreshRecords(); });
        search.add(new JLabel("Search records"), BorderLayout.WEST);
        search.add(searchField, BorderLayout.CENTER);
        JPanel searchActions = new JPanel(new GridLayout(1, 2, 8, 0));
        searchActions.setOpaque(false);
        searchActions.add(find);
        searchActions.add(all);
        search.add(searchActions, BorderLayout.EAST);
        tableWrap.add(search, BorderLayout.NORTH);

        String[] columns = {"ID", "Type", "Description", "kg", "Collection area", "Collected at", "Collector", "Status"};
        recordsModel = readOnlyModel(columns);
        recordsTable = new JTable(recordsModel);
        styleTable(recordsTable);
        recordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recordsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && recordsTable.getSelectedRow() >= 0) loadSelectedRecord();
        });
        JScrollPane scroll = new JScrollPane(recordsTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        tableWrap.add(scroll, BorderLayout.CENTER);
        return wrapCard(tableWrap);
    }

    private JScrollPane wrapCard(JPanel panel) {
        JScrollPane wrapper = new JScrollPane(panel);
        wrapper.setBorder(null);
        wrapper.getViewport().setOpaque(false);
        wrapper.setOpaque(false);
        return wrapper;
    }

    private JPanel createRecordFormPanel() {
        JPanel form = card(new BorderLayout(12, 12));
        JLabel title = sectionTitle("Collection details");
        form.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 4, 12, 8));
        fields.setOpaque(false);
        typeCombo = new JComboBox<>(new String[]{"Organic", "Recyclable", "Hazardous"});
        descriptionField = input(new JTextField());
        quantityField = input(new JTextField());
        areaCombo = new JComboBox<>(AREAS);
        areaCombo.setEditable(true);
        collectedAtField = input(new JTextField(LocalDateTime.now().format(DATE_TIME_FORMAT)));
        collectorCombo = new JComboBox<>();
        loadCollectors();

        fields.add(labeled("Waste type", typeCombo));
        fields.add(labeled("Description", descriptionField));
        fields.add(labeled("Weight (kg)", quantityField));
        fields.add(labeled("Collection area", areaCombo));
        fields.add(labeled("Collected at (yyyy-MM-dd HH:mm)", collectedAtField));
        if (user.isAdmin()) fields.add(labeled("Attributed collector", collectorCombo));
        else fields.add(labeled("Your assigned area", new JLabel(user.getAssignedArea())));
        form.add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 0, 8, 0));
        actions.setOpaque(false);
        JButton add = button("Add record", true);
        JButton update = button("Update selected", false);
        JButton invalid = button("Mark invalid", false);
        add.addActionListener(event -> addRecord());
        update.addActionListener(event -> updateRecord());
        invalid.addActionListener(event -> invalidateRecord());
        actions.add(add);
        actions.add(update);
        actions.add(invalid);
        if (user.isAdmin()) {
            JButton complete = button("Mark completed", true);
            JButton delete = button("Delete selected", false);
            complete.addActionListener(event -> completeRecord());
            delete.addActionListener(event -> deleteRecord());
            actions.add(complete);
            actions.add(delete);
        }
        JButton clear = button("Clear", false);
        clear.addActionListener(event -> clearForm());
        actions.add(clear);
        form.add(actions, BorderLayout.SOUTH);
        return form;
    }

    private JPanel createReportsPage() {
        JPanel page = pagePanel(new BorderLayout(16, 16));
        page.add(createReportHeading(), BorderLayout.NORTH);
        JPanel reportContent = new JPanel(new BorderLayout(14, 14));
        reportContent.setOpaque(false);
        reportContent.add(createSummaryCards(), BorderLayout.NORTH);
        if (user.isAdmin()) reportContent.add(createAdminReports(), BorderLayout.CENTER);
        else reportContent.add(createPersonalReport(), BorderLayout.CENTER);
        page.add(reportContent, BorderLayout.CENTER);
        return page;
    }

    private JPanel createReportHeading() {
        JPanel area = new JPanel(new BorderLayout(12, 10));
        area.setOpaque(false);
        JPanel title = heading(user.isAdmin() ? "Reports dashboard" : "My collection progress",
                user.isAdmin() ? "Completed collection totals by member and area." : "Your completed collection and additional contribution.");
        area.add(title, BorderLayout.WEST);
        JPanel filters = new JPanel(new GridLayout(2, 2, 8, 4));
        filters.setOpaque(false);
        fromField = input(new JTextField(LocalDate.now().withDayOfMonth(1).toString()));
        toField = input(new JTextField(LocalDate.now().toString()));
        JButton apply = button("Apply dates", true);
        JButton export = button("Export CSV", false);
        apply.addActionListener(event -> refreshReports());
        export.addActionListener(event -> exportCurrentReport());
        filters.add(new JLabel("From date")); filters.add(new JLabel("To date"));
        filters.add(fromField); filters.add(toField);
        JPanel control = new JPanel(new BorderLayout(8, 0));
        control.setOpaque(false);
        control.add(filters, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false); buttons.add(apply); buttons.add(export);
        control.add(buttons, BorderLayout.EAST);
        area.add(control, BorderLayout.EAST);
        return area;
    }

    private JPanel createSummaryCards() {
        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 0));
        cards.setOpaque(false);
        totalValue = new JLabel("0 kg");
        organicValue = new JLabel("0 kg");
        recyclableValue = new JLabel("0 kg");
        hazardousValue = new JLabel("0 kg");
        recordCountValue = new JLabel("0");
        cards.add(metric("Total collected", totalValue));
        cards.add(metric("Organic", organicValue));
        cards.add(metric("Recyclable", recyclableValue));
        cards.add(metric("Hazardous", hazardousValue));
        cards.add(metric("Completed records", recordCountValue));
        return cards;
    }

    private JPanel createAdminReports() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Member reports", createMemberReportPanel());
        tabs.addTab("Area reports", createAreaReportPanel());
        tabs.setFont(new Font("SansSerif", Font.BOLD, 12));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(tabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createMemberReportPanel() {
        JPanel panel = card(new BorderLayout(12, 12));
        panel.add(sectionTitle("Individual contribution by member"), BorderLayout.NORTH);
        String[] cols = {"Member", "Total kg", "Organic kg", "Recyclable kg", "Hazardous kg", "Assigned area kg", "Bonus kg", "Records"};
        memberReportModel = readOnlyModel(cols);
        memberReportTable = new JTable(memberReportModel);
        styleTable(memberReportTable);
        panel.add(new JScrollPane(memberReportTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAreaReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        JPanel chartPanel = card(new BorderLayout(8, 8));
        chartPanel.add(sectionTitle("Area-wise collected weight"), BorderLayout.NORTH);
        areaChart = new AreaChart();
        chartPanel.add(areaChart, BorderLayout.CENTER);
        chartPanel.setPreferredSize(new Dimension(270, 500));

        JPanel tableCard = card(new BorderLayout(10, 10));
        tableCard.add(sectionTitle("Collection by actual area"), BorderLayout.NORTH);
        areaReportModel = readOnlyModel(new String[]{"Area", "Total kg", "Organic kg", "Recyclable kg", "Hazardous kg", "Records"});
        areaReportTable = new JTable(areaReportModel);
        styleTable(areaReportTable);
        tableCard.add(new JScrollPane(areaReportTable), BorderLayout.CENTER);
        panel.add(chartPanel, BorderLayout.WEST);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPersonalReport() {
        JPanel panel = card(new BorderLayout(12, 12));
        panel.add(sectionTitle("Your individual report"), BorderLayout.NORTH);
        personalReportModel = readOnlyModel(new String[]{"Contribution", "Total kg", "Organic kg", "Recyclable kg", "Hazardous kg", "Records"});
        personalReportTable = new JTable(personalReportModel);
        styleTable(personalReportTable);
        panel.add(new JScrollPane(personalReportTable), BorderLayout.CENTER);
        JLabel note = new JLabel("Assigned area and bonus collection are shown separately in the contribution rows.");
        note.setForeground(MUTED);
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    private void loadCollectors() {
        if (!user.isAdmin() || collectorCombo == null) return;
        try {
            collectorCombo.removeAllItems();
            collectorCombo.addItem("Unassigned (admin entry)");
            for (String[] collector : wasteManager.getCollectors()) collectorCombo.addItem(collector[1] + " (" + collector[0] + ")");
        } catch (SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private String selectedCollectorUsername() {
        if (!user.isAdmin() || collectorCombo.getSelectedIndex() <= 0) return null;
        String selected = (String) collectorCombo.getSelectedItem();
        int start = selected.lastIndexOf('(');
        return selected.substring(start + 1, selected.length() - 1);
    }

    private void addRecord() {
        try {
            Waste waste = wasteFromForm(0);
            wasteManager.addWaste(waste, user, selectedCollectorUsername());
            showMessage("Waste record added. It will count toward reports after an administrator marks it completed.");
            clearForm();
            refreshAll();
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        } catch (SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void updateRecord() {
        if (selectedId < 0) { showError("Select one of your pending records first."); return; }
        try {
            wasteManager.updateWaste(wasteFromForm(selectedId), user, selectedCollectorUsername());
            showMessage("Pending record updated.");
            clearForm();
            refreshAll();
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void invalidateRecord() {
        if (selectedId < 0) { showError("Select a pending record first."); return; }
        String reason = JOptionPane.showInputDialog(this, "Why is this record invalid?", "Mark invalid", JOptionPane.QUESTION_MESSAGE);
        if (reason == null) return;
        try {
            wasteManager.markInvalid(selectedId, reason, user);
            clearForm();
            refreshAll();
        } catch (SQLException exception) { showError(exception.getMessage()); }
    }

    private void completeRecord() {
        if (!user.isAdmin()) return;
        if (selectedId < 0) { showError("Select a pending record first."); return; }
        try {
            wasteManager.markCompleted(selectedId, user);
            clearForm();
            refreshAll();
        } catch (SQLException exception) { showError(exception.getMessage()); }
    }

    private void deleteRecord() {
        if (!user.isAdmin()) return;
        if (selectedId < 0) { showError("Select a record first."); return; }
        int answer = JOptionPane.showConfirmDialog(this, "Permanently delete record #" + selectedId + "?",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            wasteManager.deleteWaste(selectedId, user);
            clearForm();
            refreshAll();
        } catch (SQLException exception) { showError(exception.getMessage()); }
    }

    private Waste wasteFromForm(int id) {
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) throw new IllegalArgumentException("Description is required.");
        double quantity;
        try { quantity = Double.parseDouble(quantityField.getText().trim()); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("Enter a valid weight in kilograms."); }
        if (!Double.isFinite(quantity) || quantity <= 0) throw new IllegalArgumentException("Weight must be greater than zero.");
        String area = String.valueOf(areaCombo.getEditor().getItem()).trim();
        if (area.isEmpty()) throw new IllegalArgumentException("Collection area is required.");
        LocalDateTime collectedAt;
        try { collectedAt = LocalDateTime.parse(collectedAtField.getText().trim(), DATE_TIME_FORMAT); }
        catch (DateTimeParseException exception) { throw new IllegalArgumentException("Use timestamp format yyyy-MM-dd HH:mm."); }
        if (collectedAt.isAfter(LocalDateTime.now().plusMinutes(1))) throw new IllegalArgumentException("Collection timestamp cannot be in the future.");
        String type = (String) typeCombo.getSelectedItem();
        String status = selectedStatus == null ? "PENDING" : selectedStatus;
        Waste waste;
        if ("Organic".equals(type)) waste = new model.OrganicWaste(id, description, quantity, area, status);
        else if ("Recyclable".equals(type)) waste = new model.RecyclableWaste(id, description, quantity, area, status);
        else waste = new model.HazardousWaste(id, description, quantity, area, status);
        waste.setCollectedAt(collectedAt.toString());
        return waste;
    }

    private void loadSelectedRecord() {
        int row = recordsTable.convertRowIndexToModel(recordsTable.getSelectedRow());
        selectedId = (Integer) recordsModel.getValueAt(row, 0);
        typeCombo.setSelectedItem(recordsModel.getValueAt(row, 1));
        descriptionField.setText(String.valueOf(recordsModel.getValueAt(row, 2)));
        quantityField.setText(String.valueOf(recordsModel.getValueAt(row, 3)));
        areaCombo.setSelectedItem(recordsModel.getValueAt(row, 4));
        collectedAtField.setText(String.valueOf(recordsModel.getValueAt(row, 5)));
        selectedStatus = String.valueOf(recordsModel.getValueAt(row, 7));
        if (user.isAdmin()) {
            collectorCombo.setSelectedIndex(0);
        }
        if (user.isAdmin() && recordsModel.getValueAt(row, 6) != null) {
            String collector = String.valueOf(recordsModel.getValueAt(row, 6));
            for (int i = 0; i < collectorCombo.getItemCount(); i++) {
                if (collectorCombo.getItemAt(i).contains("(" + collector + ")")) collectorCombo.setSelectedIndex(i);
            }
        }
    }

    private void clearForm() {
        selectedId = -1;
        selectedStatus = null;
        descriptionField.setText("");
        quantityField.setText("");
        typeCombo.setSelectedIndex(0);
        areaCombo.setSelectedItem(user.isAdmin() ? AREAS[0] : user.getAssignedArea());
        collectedAtField.setText(LocalDateTime.now().format(DATE_TIME_FORMAT));
        if (user.isAdmin() && collectorCombo.getItemCount() > 0) collectorCombo.setSelectedIndex(0);
        recordsTable.clearSelection();
    }

    private void refreshAll() {
        refreshRecords();
        refreshReports();
    }

    private void refreshRecords() {
        if (recordsModel == null) return;
        try {
            recordsModel.setRowCount(0);
            for (Waste waste : wasteManager.getVisibleWaste(user, searchField == null ? "" : searchField.getText())) {
                recordsModel.addRow(new Object[]{waste.getId(), waste.getWasteCategory(), waste.getDescription(),
                        waste.getQuantity(), waste.getLocation(), waste.getCollectedAt(), waste.getCollectedBy(), waste.getStatus()});
            }
        } catch (SQLException exception) { showError(exception.getMessage()); }
    }

    private void refreshReports() {
        if (totalValue == null) return;
        try {
            LocalDate from = LocalDate.parse(fromField.getText().trim());
            LocalDate to = LocalDate.parse(toField.getText().trim());
            if (from.isAfter(to)) throw new IllegalArgumentException("From date must be on or before To date.");
            ReportSummary summary = reportManager.getSummary(user, from, to);
            totalValue.setText(kg(summary.getTotalKg()));
            organicValue.setText(kg(summary.getOrganicKg()));
            recyclableValue.setText(kg(summary.getRecyclableKg()));
            hazardousValue.setText(kg(summary.getHazardousKg()));
            recordCountValue.setText(String.valueOf(summary.getCompletedRecords()));

            if (user.isAdmin()) {
                currentMemberReports = reportManager.getAllUserReports(from, to);
                currentAreaReports = reportManager.getAreaReports(from, to);
                fillMemberRows();
                fillAreaRows();
                areaChart.setReports(currentAreaReports);
            } else {
                ReportRow own = reportManager.getUserReport(user.getUsername(), from, to);
                personalReportModel.setRowCount(0);
                personalReportModel.addRow(new Object[]{"Assigned area", kg(own.getAssignedKg()), "-", "-", "-", "-"});
                personalReportModel.addRow(new Object[]{"Additional / bonus", kg(own.getBonusKg()), "-", "-", "-", "-"});
                personalReportModel.addRow(new Object[]{"All your collections", kg(own.getTotalKg()), kg(own.getOrganic()),
                        kg(own.getRecyclable()), kg(own.getHazardous()), own.getRecords()});
            }
        } catch (DateTimeParseException exception) {
            showError("Enter report dates using yyyy-MM-dd.");
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void fillMemberRows() {
        memberReportModel.setRowCount(0);
        for (ReportRow row : currentMemberReports) memberReportModel.addRow(new Object[]{row.getLabel(), kg(row.getTotalKg()),
                kg(row.getOrganic()), kg(row.getRecyclable()), kg(row.getHazardous()), kg(row.getAssignedKg()),
                kg(row.getBonusKg()), row.getRecords()});
    }

    private void fillAreaRows() {
        areaReportModel.setRowCount(0);
        for (ReportRow row : currentAreaReports) areaReportModel.addRow(new Object[]{row.getLabel(), kg(row.getTotalKg()),
                kg(row.getOrganic()), kg(row.getRecyclable()), kg(row.getHazardous()), row.getRecords()});
    }

    private void exportCurrentReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(user.isAdmin() ? "clean-cet-waste-report.csv" : user.getUsername() + "-waste-report.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chooser.getSelectedFile()))) {
            if (user.isAdmin()) {
                writer.write("Member,Total kg,Organic kg,Recyclable kg,Hazardous kg,Assigned area kg,Bonus kg,Completed records\n");
                for (ReportRow row : currentMemberReports) writer.write(csv(row.getLabel()) + "," + row.getTotalKg() + "," + row.getOrganic() + "," + row.getRecyclable() + "," + row.getHazardous() + "," + row.getAssignedKg() + "," + row.getBonusKg() + "," + row.getRecords() + "\n");
                writer.write("\nArea,Total kg,Organic kg,Recyclable kg,Hazardous kg,Completed records\n");
                for (ReportRow row : currentAreaReports) writer.write(csv(row.getLabel()) + "," + row.getTotalKg() + "," + row.getOrganic() + "," + row.getRecyclable() + "," + row.getHazardous() + "," + row.getRecords() + "\n");
            } else {
                writer.write("Member,Total kg,Organic kg,Recyclable kg,Hazardous kg,Assigned area kg,Bonus kg,Completed records\n");
                ReportRow row = reportManager.getUserReport(user.getUsername(), LocalDate.parse(fromField.getText()), LocalDate.parse(toField.getText()));
                writer.write(csv(user.getDisplayName()) + "," + row.getTotalKg() + "," + row.getOrganic() + "," + row.getRecyclable() + "," + row.getHazardous() + "," + row.getAssignedKg() + "," + row.getBonusKg() + "," + row.getRecords() + "\n");
            }
            showMessage("Report exported successfully.");
        } catch (Exception exception) { showError("Could not export report: " + exception.getMessage()); }
    }

    private static String csv(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }
    private static String kg(double amount) { return String.format(java.util.Locale.ROOT, "%.2f kg", amount); }

    private JPanel pagePanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(PAGE);
        panel.setBorder(new EmptyBorder(22, 24, 24, 24));
        return panel;
    }

    private JPanel heading(String title, String subtitle) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 4));
        panel.setOpaque(false);
        JLabel titleLabel = sectionTitle(title);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(MUTED);
        panel.add(titleLabel);
        panel.add(subtitleLabel);
        return panel;
    }

    private JLabel sectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 17));
        label.setForeground(INK);
        return label;
    }

    private JPanel card(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                new EmptyBorder(15, 16, 15, 16)));
        return panel;
    }

    private JPanel metric(String name, JLabel value) {
        JPanel panel = card(new GridLayout(2, 1, 0, 8));
        JLabel caption = new JLabel(name);
        caption.setForeground(MUTED);
        value.setForeground(PURPLE_DARK);
        value.setFont(new Font("SansSerif", Font.BOLD, 21));
        panel.add(caption);
        panel.add(value);
        return panel;
    }

    private JPanel labeled(String name, java.awt.Component component) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(name);
        label.setForeground(MUTED);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JTextField input(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                new EmptyBorder(8, 9, 8, 9)));
        return field;
    }

    private JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setBackground(primary ? PURPLE : new Color(248, 247, 251));
        button.setForeground(primary ? Color.WHITE : PURPLE_DARK);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? PURPLE : LINE),
                new EmptyBorder(8, 12, 8, 12)));
        return button;
    }

    private static DefaultTableModel readOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private static void styleTable(JTable table) {
        table.setRowHeight(31);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setForeground(INK);
        table.setSelectionBackground(PURPLE_LIGHT);
        table.setSelectionForeground(PURPLE_DARK);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(PURPLE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);
    }

    private void showMessage(String message) { JOptionPane.showMessageDialog(this, message); }
    private void showError(String message) { JOptionPane.showMessageDialog(this, message, "Waste Management", JOptionPane.ERROR_MESSAGE); }

    private static class AreaChart extends JPanel {
        private List<ReportRow> reports = new ArrayList<>();

        AreaChart() { setBackground(Color.WHITE); setPreferredSize(new Dimension(260, 440)); }

        void setReports(List<ReportRow> reports) { this.reports = new ArrayList<>(reports); repaint(); }

        @Override protected void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 10, right = 42, top = 12, rowHeight = 29;
            double max = 1;
            for (ReportRow row : reports) max = Math.max(max, row.getTotalKg());
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int i = 0; i < reports.size(); i++) {
                ReportRow row = reports.get(i);
                int y = top + i * rowHeight;
                String label = row.getLabel().length() > 17 ? row.getLabel().substring(0, 16) + "…" : row.getLabel();
                g.setColor(INK);
                g.drawString(label, left, y + 10);
                int barX = left;
                int barY = y + 15;
                int width = Math.max(2, (int) ((getWidth() - left - right) * row.getTotalKg() / max));
                g.setColor(PURPLE_LIGHT);
                g.fillRoundRect(barX, barY, Math.max(2, getWidth() - left - right), 8, 8, 8);
                g.setColor(PURPLE);
                g.fillRoundRect(barX, barY, width, 8, 8, 8);
                g.setColor(MUTED);
                g.drawString(String.format(java.util.Locale.ROOT, "%.1f", row.getTotalKg()), getWidth() - right + 5, barY + 8);
            }
            g.dispose();
        }
    }
}
