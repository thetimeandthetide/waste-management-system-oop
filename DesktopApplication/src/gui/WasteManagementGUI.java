package gui;

import dao.ReportManager;
import dao.UserManager;
import dao.AnnouncementManager;
import dao.WasteManager;
import model.Announcement;
import model.AppUser;
import model.ReportRow;
import model.ReportSummary;
import model.Waste;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
            "Electrical gate", "Indoor court", "Archie corner", "College front", "DJ hall", "CGPU hall", "CETAA hall",
            "Industrial department front", "EC department front", "Rohith square", "Sargam stage", "Library front",
            "Ohm's villa", "Industrial base", "College ground", "Research block", "Old EC department",
            "Hydraulics lab front", "Back gate", "Mh front", "Lh front", "Flag post"
    };
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AppUser user;
    private final WasteManager wasteManager = new WasteManager();
    private final ReportManager reportManager = new ReportManager();
    private final AnnouncementManager announcementManager = new AnnouncementManager();
    private final UserManager userManager = new UserManager();
    private Timer approvalRefreshTimer;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageHost = new JPanel(pageLayout);
    private JButton announcementsNavigation;
    private JButton completionRequestsNavigation;
    private JTable announcementsTable;
    private DefaultTableModel announcementsModel;
    private JTextArea announcementDetails;
    private List<Announcement> announcements = new ArrayList<>();
    private JTable completionRequestsTable;
    private DefaultTableModel completionRequestsModel;
    private JTextField memberSearchField;
    private DefaultTableModel memberDirectoryModel;

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
    private JLabel appliedRangeLabel;
    private JTable memberReportTable;
    private DefaultTableModel memberReportModel;
    private JTable areaReportTable;
    private DefaultTableModel areaReportModel;
    private JTable personalReportTable;
    private DefaultTableModel personalReportModel;
    private AreaChart areaChart;
    private List<ReportRow> currentMemberReports = new ArrayList<>();
    private List<ReportRow> currentAreaReports = new ArrayList<>();
    private LocalDate appliedFrom;
    private LocalDate appliedTo;

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
        pageHost.add(createAnnouncementsPage(), "announcements");
        if (user.isAdmin()) {
            pageHost.add(createApprovalsPage(), "approvals");
            pageHost.add(createMembersPage(), "members");
        }
        add(createSidebar(), BorderLayout.WEST);
        add(createTopBar(), BorderLayout.NORTH);
        add(pageHost, BorderLayout.CENTER);

        refreshRecords();
        refreshReports();
        refreshAnnouncements();
        refreshApprovalRequests();
        refreshMembers();
        if (user.isAdmin()) {
            approvalRefreshTimer = new Timer(15000, event -> refreshApprovalCount());
            approvalRefreshTimer.start();
        }
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
        JButton members = user.isAdmin() ? navButton("Member directory") : null;
        announcementsNavigation = navButton("Announcements");
        if (user.isAdmin()) completionRequestsNavigation = navButton("Collection approvals (0)");
        records.addActionListener(event -> pageLayout.show(pageHost, "records"));
        reports.addActionListener(event -> {
            refreshReports();
            pageLayout.show(pageHost, "reports");
        });
        if (user.isAdmin()) members.addActionListener(event -> {
            refreshMembers();
            pageLayout.show(pageHost, "members");
        });
        announcementsNavigation.addActionListener(event -> {
            refreshAnnouncements();
            pageLayout.show(pageHost, "announcements");
        });
        if (user.isAdmin()) completionRequestsNavigation.addActionListener(event -> {
            refreshApprovalRequests();
            pageLayout.show(pageHost, "approvals");
        });
        navigation.add(records);
        navigation.add(reports);
        if (user.isAdmin()) navigation.add(members);
        navigation.add(announcementsNavigation);
        if (user.isAdmin()) navigation.add(completionRequestsNavigation);

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
        button.setUI(new CleanButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
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
        if (approvalRefreshTimer != null) approvalRefreshTimer.stop();
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
            JButton complete = button("Mark as collected", true);
            JButton delete = button("Delete selected", false);
            complete.addActionListener(event -> completeRecord());
            delete.addActionListener(event -> deleteRecord());
            actions.add(complete);
            actions.add(delete);
        } else {
            JButton request = button("Request approval", true);
            request.addActionListener(event -> requestCompletion());
            actions.add(request);
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

    private JPanel createApprovalsPage() {
        JPanel page = pagePanel(new BorderLayout(16, 16));
        JPanel heading = new JPanel(new BorderLayout(12, 0));
        heading.setOpaque(false);
        heading.add(heading("Collection approvals", "Member requests stay pending until an administrator approves them."), BorderLayout.WEST);
        JButton refresh = button("Refresh requests", false);
        refresh.addActionListener(event -> refreshApprovalRequests());
        heading.add(refresh, BorderLayout.EAST);
        page.add(heading, BorderLayout.NORTH);

        JPanel panel = card(new BorderLayout(12, 12));
        panel.add(sectionTitle("Waiting for review"), BorderLayout.NORTH);
        completionRequestsModel = readOnlyModel(new String[]{"Request", "Record", "Type", "Description", "kg", "Actual area", "Collected at", "Requested by", "Requested at"});
        completionRequestsTable = new JTable(completionRequestsModel);
        styleTable(completionRequestsTable);
        completionRequestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(completionRequestsTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton reject = button("Reject request", false);
        JButton approve = button("Approve and mark collected", true);
        reject.addActionListener(event -> rejectCompletionRequest());
        approve.addActionListener(event -> approveCompletionRequest());
        actions.add(reject);
        actions.add(approve);
        panel.add(actions, BorderLayout.SOUTH);
        page.add(panel, BorderLayout.CENTER);
        return page;
    }

    private JPanel createMembersPage() {
        JPanel page = pagePanel(new BorderLayout(16, 16));
        page.add(heading("Member directory", "Registered Clean CET members and their assigned locations."), BorderLayout.NORTH);
        JPanel panel = card(new BorderLayout(10, 10));
        JPanel search = new JPanel(new BorderLayout(10, 0));
        search.setOpaque(false);
        memberSearchField = input(new JTextField());
        JButton find = button("Search members", false);
        JButton refresh = button("Refresh", false);
        JButton addArea = button("Add registration area", true);
        find.addActionListener(event -> refreshMembers());
        refresh.addActionListener(event -> { memberSearchField.setText(""); refreshMembers(); });
        addArea.addActionListener(event -> addRegistrationArea());
        search.add(new JLabel("Search members"), BorderLayout.WEST);
        search.add(memberSearchField, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0));
        actions.setOpaque(false);
        actions.add(find);
        actions.add(refresh);
        actions.add(addArea);
        search.add(actions, BorderLayout.EAST);
        panel.add(search, BorderLayout.NORTH);
        memberDirectoryModel = readOnlyModel(new String[]{"Name", "Username", "Date of birth", "CET register number",
                "Department", "Assigned area", "Account"});
        JTable members = new JTable(memberDirectoryModel);
        styleTable(members);
        members.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {150, 100, 115, 155, 170, 190, 85};
        for (int i = 0; i < widths.length; i++) members.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        panel.add(new JScrollPane(members), BorderLayout.CENTER);
        JLabel note = new JLabel("Passwords are stored as salted hashes and are never visible in the directory.");
        note.setForeground(MUTED);
        panel.add(note, BorderLayout.SOUTH);
        page.add(panel, BorderLayout.CENTER);
        return page;
    }

    private void refreshMembers() {
        if (!user.isAdmin() || memberDirectoryModel == null) return;
        try {
            memberDirectoryModel.setRowCount(0);
            for (Object[] member : userManager.getMembers(user, memberSearchField.getText())) {
                memberDirectoryModel.addRow(member);
            }
        } catch (SQLException exception) {
            showError("Could not load member directory: " + exception.getMessage());
        }
    }

    private void addRegistrationArea() {
        String area = JOptionPane.showInputDialog(this, "New area for future member assignment:",
                "Add registration area", JOptionPane.QUESTION_MESSAGE);
        if (area == null) return;
        try {
            userManager.addRegistrationArea(area, user);
            showMessage("Registration area added. It is now available for random assignment to a new member.");
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private JPanel createAnnouncementsPage() {
        JPanel page = pagePanel(new BorderLayout(16, 16));
        JPanel heading = new JPanel(new BorderLayout(12, 0));
        heading.setOpaque(false);
        heading.add(heading("Announcements", "Updates from the Clean CET administrator."), BorderLayout.WEST);
        if (user.isAdmin()) {
            JButton publish = button("New announcement", true);
            publish.addActionListener(event -> publishAnnouncement());
            heading.add(publish, BorderLayout.EAST);
        }
        page.add(heading, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(14, 0));
        content.setOpaque(false);
        announcementsModel = readOnlyModel(new String[]{"Title", "From", "Date", "Status"});
        announcementsTable = new JTable(announcementsModel);
        styleTable(announcementsTable);
        announcementsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        announcementsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && announcementsTable.getSelectedRow() >= 0) {
                showAnnouncement(announcementsTable.convertRowIndexToModel(announcementsTable.getSelectedRow()));
            }
        });
        JPanel list = card(new BorderLayout(0, 10));
        list.add(sectionTitle("Message inbox"), BorderLayout.NORTH);
        list.add(new JScrollPane(announcementsTable), BorderLayout.CENTER);
        JPanel details = card(new BorderLayout(0, 10));
        details.add(sectionTitle("Announcement details"), BorderLayout.NORTH);
        announcementDetails = new JTextArea();
        announcementDetails.setEditable(false);
        announcementDetails.setLineWrap(true);
        announcementDetails.setWrapStyleWord(true);
        announcementDetails.setFont(new Font("SansSerif", Font.PLAIN, 13));
        announcementDetails.setForeground(INK);
        announcementDetails.setBorder(new EmptyBorder(10, 10, 10, 10));
        details.add(new JScrollPane(announcementDetails), BorderLayout.CENTER);
        content.add(list, BorderLayout.CENTER);
        content.add(details, BorderLayout.EAST);
        details.setPreferredSize(new Dimension(330, 0));
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private void refreshAnnouncements() {
        if (announcementsModel == null) return;
        try {
            announcements = announcementManager.getAnnouncements(user);
            announcementsModel.setRowCount(0);
            for (Announcement announcement : announcements) {
                announcementsModel.addRow(new Object[]{announcement.getTitle(), announcement.getAuthor(),
                        announcement.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        user.isAdmin() ? "Published" : announcement.isRead() ? "Read" : "New"});
            }
            announcementsNavigation.setText("Announcements (" + announcementManager.getUnreadCount(user) + ")");
            if (announcements.isEmpty()) announcementDetails.setText("No announcements yet.");
        } catch (SQLException exception) {
            showError("Could not load announcements: " + exception.getMessage());
        }
    }

    private void showAnnouncement(int index) {
        if (index < 0 || index >= announcements.size()) return;
        Announcement announcement = announcements.get(index);
        announcementDetails.setText(announcement.getTitle() + "\n\n"
                + announcement.getMessage() + "\n\nFrom: " + announcement.getAuthor()
                + "\nPosted: " + announcement.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        if (!user.isAdmin() && !announcement.isRead()) {
            try {
                announcementManager.markRead(announcement.getId(), user);
                refreshAnnouncements();
                announcementsTable.setRowSelectionInterval(index, index);
            } catch (SQLException exception) {
                showError("Could not mark announcement as read: " + exception.getMessage());
            }
        }
    }

    private void publishAnnouncement() {
        JTextField title = input(new JTextField());
        JTextArea message = new JTextArea(6, 28);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        JPanel form = new JPanel(new BorderLayout(0, 8));
        form.add(labeled("Title", title), BorderLayout.NORTH);
        form.add(labeled("Message", new JScrollPane(message)), BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, form, "New announcement",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            announcementManager.publish(title.getText(), message.getText(), user);
            refreshAnnouncements();
            showMessage("Announcement sent to all users.");
        } catch (SQLException exception) {
            showError(exception.getMessage());
        }
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
        apply.addActionListener(event -> applyReportDates());
        export.addActionListener(event -> exportCurrentReport());
        filters.add(new JLabel("From date")); filters.add(new JLabel("To date"));
        filters.add(fromField); filters.add(toField);
        JPanel control = new JPanel(new BorderLayout(8, 0));
        control.setOpaque(false);
        control.add(filters, BorderLayout.CENTER);
        apply.setPreferredSize(new Dimension(108, 40));
        export.setPreferredSize(new Dimension(108, 40));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        buttons.setOpaque(false); buttons.add(apply); buttons.add(export);
        control.add(buttons, BorderLayout.EAST);
        appliedRangeLabel = new JLabel("Applied range: not applied");
        appliedRangeLabel.setForeground(MUTED);
        control.add(appliedRangeLabel, BorderLayout.SOUTH);
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
        chartPanel.add(sectionTitle("Collected weight by area (kg)"), BorderLayout.NORTH);
        areaChart = new AreaChart();
        chartPanel.add(areaChart, BorderLayout.CENTER);
        chartPanel.setPreferredSize(new Dimension(310, 500));

        JPanel tableCard = card(new BorderLayout(10, 10));
        tableCard.add(sectionTitle("Collected weight and record count by area"), BorderLayout.NORTH);
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
            showMessage(user.isAdmin()
                    ? "Waste record added as pending. Mark it collected or approve a member request to include it in reports."
                    : "Waste record added as pending. Request collection approval after reviewing the record.");
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

    private void requestCompletion() {
        if (selectedId < 0) { showError("Select one of your pending records first."); return; }
        try {
            wasteManager.requestCompletion(selectedId, user);
            clearForm();
            refreshAll();
            showMessage("Completion request sent. The record will count in reports after admin approval.");
        } catch (SQLException exception) { showError(exception.getMessage()); }
    }

    private void completeRecord() {
        if (!user.isAdmin()) return;
        if (selectedId < 0) { showError("Select a pending record first."); return; }
        try {
            wasteManager.markCompleted(selectedId, user);
            clearForm();
            refreshAll();
            showMessage("Record marked as collected. Reports have been updated.");
        } catch (SQLException exception) { showError(exception.getMessage()); }
    }

    private void refreshApprovalCount() {
        if (!user.isAdmin() || completionRequestsNavigation == null) return;
        try {
            completionRequestsNavigation.setText("Collection approvals (" + wasteManager.getPendingCompletionRequestCount(user) + ")");
        } catch (SQLException exception) {
            completionRequestsNavigation.setToolTipText("Could not refresh approval count: " + exception.getMessage());
        }
    }

    private void refreshApprovalRequests() {
        if (!user.isAdmin() || completionRequestsModel == null) return;
        try {
            completionRequestsModel.setRowCount(0);
            for (Object[] request : wasteManager.getPendingCompletionRequests(user)) completionRequestsModel.addRow(request);
            refreshApprovalCount();
        } catch (SQLException exception) { showError("Could not load collection approvals: " + exception.getMessage()); }
    }

    private Long selectedRequestId() {
        int row = completionRequestsTable.getSelectedRow();
        if (row < 0) {
            showError("Select a completion request first.");
            return null;
        }
        return ((Number) completionRequestsModel.getValueAt(completionRequestsTable.convertRowIndexToModel(row), 0)).longValue();
    }

    private void approveCompletionRequest() {
        Long requestId = selectedRequestId();
        if (requestId == null) return;
        try {
            wasteManager.approveCompletionRequest(requestId, user);
            refreshAll();
            showMessage("Request approved. The record is now marked collected and included in reports.");
        } catch (SQLException exception) { showError(exception.getMessage()); }
    }

    private void rejectCompletionRequest() {
        Long requestId = selectedRequestId();
        if (requestId == null) return;
        try {
            wasteManager.rejectCompletionRequest(requestId, user);
            refreshApprovalRequests();
            showMessage("Request rejected. The waste record remains pending and can be corrected or resubmitted.");
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
        refreshApprovalRequests();
    }

    private void refreshRecords() {
        if (recordsModel == null) return;
        try {
            recordsModel.setRowCount(0);
            for (Waste waste : wasteManager.getVisibleWaste(user, searchField == null ? "" : searchField.getText())) {
                recordsModel.addRow(new Object[]{waste.getId(), waste.getWasteCategory(), waste.getDescription(),
                        waste.getQuantity(), waste.getLocation(), waste.getCollectedAt(), waste.getCollectedBy(),
                        displayStatus(waste)});
            }
        } catch (SQLException exception) { showError(exception.getMessage()); }
    }

    private void refreshReports() {
        applyReportDates();
    }

    private void applyReportDates() {
        if (totalValue == null) return;
        try {
            LocalDate from = LocalDate.parse(fromField.getText().trim());
            LocalDate to = LocalDate.parse(toField.getText().trim());
            if (from.isAfter(to)) throw new IllegalArgumentException("From date must be on or before To date.");
            ReportSummary summary = reportManager.getSummary(user, from, to);
            List<ReportRow> memberRows = null;
            List<ReportRow> areaRows = null;
            ReportRow own = null;
            if (user.isAdmin()) {
                memberRows = reportManager.getAllUserReports(from, to, user);
                areaRows = reportManager.getAreaReports(from, to, user);
            } else {
                own = reportManager.getUserReport(user.getUsername(), from, to, user);
            }

            totalValue.setText(kg(summary.getTotalKg()));
            organicValue.setText(kg(summary.getOrganicKg()));
            recyclableValue.setText(kg(summary.getRecyclableKg()));
            hazardousValue.setText(kg(summary.getHazardousKg()));
            recordCountValue.setText(String.valueOf(summary.getCompletedRecords()));
            if (user.isAdmin()) {
                currentMemberReports = memberRows;
                currentAreaReports = areaRows;
                fillMemberRows();
                fillAreaRows();
                areaChart.setReports(currentAreaReports);
            } else {
                personalReportModel.setRowCount(0);
                personalReportModel.addRow(new Object[]{"Assigned area", kg(own.getAssignedKg()), "-", "-", "-", "-"});
                personalReportModel.addRow(new Object[]{"Additional / bonus", kg(own.getBonusKg()), "-", "-", "-", "-"});
                personalReportModel.addRow(new Object[]{"All your collections", kg(own.getTotalKg()), kg(own.getOrganic()),
                        kg(own.getRecyclable()), kg(own.getHazardous()), own.getRecords()});
            }
            appliedFrom = from;
            appliedTo = to;
            appliedRangeLabel.setText("Applied range: " + from + " to " + to);
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

    private static String displayStatus(Waste waste) {
        if (waste.isCompletionRequested()) return "PENDING - AWAITING APPROVAL";
        if ("REJECTED".equals(waste.getCompletionRequestStatus())) return "PENDING - REQUEST REJECTED";
        return waste.getStatus();
    }

    private void fillAreaRows() {
        areaReportModel.setRowCount(0);
        for (ReportRow row : currentAreaReports) areaReportModel.addRow(new Object[]{row.getLabel(), kg(row.getTotalKg()),
                kg(row.getOrganic()), kg(row.getRecyclable()), kg(row.getHazardous()), row.getRecords()});
    }

    private void exportCurrentReport() {
        if (appliedFrom == null || appliedTo == null) {
            showError("Apply a valid date range before exporting.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(user.isAdmin() ? "clean-cet-waste-report.csv" : user.getUsername() + "-waste-report.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            LocalDate from = appliedFrom;
            LocalDate to = appliedTo;
            List<ReportRow> memberRows = user.isAdmin() ? reportManager.getAllUserReports(from, to, user) : null;
            List<ReportRow> areaRows = user.isAdmin() ? reportManager.getAreaReports(from, to, user) : null;
            ReportRow personalRow = user.isAdmin() ? null
                    : reportManager.getUserReport(user.getUsername(), from, to, user);
            File output = chooser.getSelectedFile();
            if (!output.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".csv")) {
                output = new File(output.getAbsolutePath() + ".csv");
            }
            if (output.exists() && JOptionPane.showConfirmDialog(this,
                    "Replace " + output.getName() + "?", "Confirm export", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
            try (BufferedWriter writer = Files.newBufferedWriter(output.toPath(), StandardCharsets.UTF_8)) {
                writer.write("Report period," + csv(from.toString()) + "," + csv(to.toString()) + "\n\n");
                if (user.isAdmin()) {
                    writer.write("Member,Total kg,Organic kg,Recyclable kg,Hazardous kg,Assigned area kg,Bonus kg,Completed records\n");
                    for (ReportRow row : memberRows) writer.write(csv(row.getLabel()) + "," + row.getTotalKg() + "," + row.getOrganic() + "," + row.getRecyclable() + "," + row.getHazardous() + "," + row.getAssignedKg() + "," + row.getBonusKg() + "," + row.getRecords() + "\n");
                    writer.write("\nArea,Total kg,Organic kg,Recyclable kg,Hazardous kg,Completed records\n");
                    for (ReportRow row : areaRows) writer.write(csv(row.getLabel()) + "," + row.getTotalKg() + "," + row.getOrganic() + "," + row.getRecyclable() + "," + row.getHazardous() + "," + row.getRecords() + "\n");
                } else {
                    writer.write("Member,Total kg,Organic kg,Recyclable kg,Hazardous kg,Assigned area kg,Bonus kg,Completed records\n");
                    writer.write(csv(user.getDisplayName()) + "," + personalRow.getTotalKg() + "," + personalRow.getOrganic() + "," + personalRow.getRecyclable() + "," + personalRow.getHazardous() + "," + personalRow.getAssignedKg() + "," + personalRow.getBonusKg() + "," + personalRow.getRecords() + "\n");
                }
            }
            showMessage("Report exported successfully.");
        } catch (Exception exception) { showError("Could not export report: " + exception.getMessage()); }
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value;
        if (safe.matches("^[\\s]*[=+@\\-].*")) safe = "'" + safe;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
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
        button.setUI(new CleanButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setBackground(primary ? PURPLE : new Color(248, 247, 251));
        button.setForeground(primary ? Color.WHITE : PURPLE_DARK);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 6, 5, 6));
        button.setMinimumSize(new Dimension(0, 38));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? PURPLE : LINE),
                new EmptyBorder(6, 8, 6, 8)));
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
            if (reports.isEmpty()) {
                g.setColor(MUTED);
                g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g.drawString("No completed collections in this date range.", 12, 24);
                g.dispose();
                return;
            }
            int left = 10, right = 42, top = 12;
            int rowHeight = Math.max(24, Math.min(29, (getHeight() - top) / reports.size()));
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
