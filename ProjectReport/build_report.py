from pathlib import Path
import textwrap

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.platypus import (
    BaseDocTemplate, PageTemplate, Frame, Paragraph, Spacer, PageBreak,
    Table, TableStyle, Image, KeepTogether, Preformatted, NextPageTemplate,
)
from reportlab.platypus.tableofcontents import TableOfContents


ROOT = Path(__file__).resolve().parent
REPO = ROOT.parent
OUT = ROOT / "main.pdf"
PAGE_W, PAGE_H = A4
INK = colors.HexColor("#24242C")
MUTED = colors.HexColor("#5D5D68")
ACCENT = colors.HexColor("#493A88")
PALE = colors.HexColor("#F0EEF7")
GRID = colors.HexColor("#CFCFD6")

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(
    name="CoverTitle", parent=styles["Title"], fontName="Helvetica-Bold",
    fontSize=20, leading=25, alignment=TA_CENTER, textColor=INK, spaceAfter=8,
))
styles.add(ParagraphStyle(
    name="CoverSub", parent=styles["Normal"], fontName="Helvetica",
    fontSize=12, leading=18, alignment=TA_CENTER, textColor=INK,
))
styles.add(ParagraphStyle(
    name="BodyReport", parent=styles["BodyText"], fontName="Helvetica",
    fontSize=9.5, leading=14, alignment=TA_JUSTIFY, textColor=INK,
    spaceAfter=7,
))
styles.add(ParagraphStyle(
    name="SmallBody", parent=styles["BodyReport"], fontSize=8.5, leading=12,
    alignment=TA_LEFT,
))
styles.add(ParagraphStyle(
    name="ChapterTitle", parent=styles["Heading1"], fontName="Helvetica-Bold",
    fontSize=17, leading=21, textColor=INK, spaceBefore=0, spaceAfter=14,
    keepWithNext=True,
))
styles.add(ParagraphStyle(
    name="SectionTitle", parent=styles["Heading2"], fontName="Helvetica-Bold",
    fontSize=12, leading=16, textColor=INK, spaceBefore=12, spaceAfter=6,
    keepWithNext=True,
))
styles.add(ParagraphStyle(
    name="SubsectionTitle", parent=styles["Heading3"], fontName="Helvetica-Bold",
    fontSize=10, leading=13, textColor=INK, spaceBefore=9, spaceAfter=4,
    keepWithNext=True,
))
styles.add(ParagraphStyle(
    name="TableHeader", parent=styles["SmallBody"], fontName="Helvetica-Bold",
    textColor=colors.white, alignment=TA_LEFT,
))
styles.add(ParagraphStyle(
    name="CodeReport", fontName="Courier", fontSize=6.2, leading=8,
    leftIndent=2, rightIndent=2, spaceBefore=2, spaceAfter=4,
))
styles.add(ParagraphStyle(
    name="CaptionReport", parent=styles["SmallBody"], alignment=TA_CENTER,
    fontName="Helvetica-Oblique", fontSize=8, leading=10, spaceBefore=3,
    spaceAfter=8,
))
styles.add(ParagraphStyle(
    name="ArrowReport", parent=styles["SmallBody"], alignment=TA_CENTER,
    fontName="Helvetica-Bold", fontSize=8, leading=10,
))


class ReportDocument(BaseDocTemplate):
    def __init__(self, filename):
        super().__init__(filename, pagesize=A4, leftMargin=25*mm,
                         rightMargin=25*mm, topMargin=23*mm, bottomMargin=20*mm,
                         title="Waste Management System Project Report",
                         author="PBCST304 Project Team")
        frame = Frame(self.leftMargin, self.bottomMargin, self.width, self.height,
                      id="normal", leftPadding=0, rightPadding=0,
                      topPadding=0, bottomPadding=0)
        self.addPageTemplates(PageTemplate(id="body", frames=frame, onPage=self.draw_page))

    def draw_page(self, canvas, doc):
        if doc.page == 1:
            return
        canvas.saveState()
        canvas.setStrokeColor(GRID)
        canvas.setLineWidth(0.45)
        canvas.line(25*mm, PAGE_H-16*mm, PAGE_W-25*mm, PAGE_H-16*mm)
        canvas.setFont("Helvetica", 8)
        canvas.setFillColor(MUTED)
        canvas.drawString(25*mm, PAGE_H-13*mm, "Waste Management System")
        canvas.drawRightString(PAGE_W-25*mm, PAGE_H-13*mm, "PBCST304 - OOP Project")
        canvas.line(25*mm, 15*mm, PAGE_W-25*mm, 15*mm)
        canvas.drawCentredString(PAGE_W/2, 10*mm, self.page_label(doc.page))
        canvas.restoreState()

    @staticmethod
    def page_label(page):
        if page >= 8:
            return str(page - 7)
        values = ((1000, "M"), (900, "CM"), (500, "D"), (400, "CD"),
                  (100, "C"), (90, "XC"), (50, "L"), (40, "XL"),
                  (10, "X"), (9, "IX"), (5, "V"), (4, "IV"), (1, "I"))
        number = page - 1
        result = ""
        for value, symbol in values:
            while number >= value:
                result += symbol
                number -= value
        return result.lower()

    def afterFlowable(self, flowable):
        if isinstance(flowable, Paragraph):
            name = flowable.style.name
            if name in ("ChapterTitle", "SectionTitle", "SubsectionTitle"):
                level = {"ChapterTitle": 0, "SectionTitle": 1,
                         "SubsectionTitle": 2}[name]
                text = flowable.getPlainText()
                key = f"toc-{level}-{self.page}-{len(text)}-{abs(hash(text))}"
                self.canv.bookmarkPage(key)
                self.canv.addOutlineEntry(text, key, level=level, closed=False)
                self.notify("TOCEntry", (level, text, self.page, key))


def para(text, style="BodyReport"):
    return Paragraph(text, styles[style])


def heading(text, level=1):
    style = {1: "ChapterTitle", 2: "SectionTitle", 3: "SubsectionTitle"}[level]
    return Paragraph(text, styles[style])


def make_table(headers, rows, widths, font_size=8):
    data = [[Paragraph(str(v), styles["TableHeader"]) for v in headers]]
    for row in rows:
        data.append([Paragraph(str(v), ParagraphStyle(
            name=f"cell-{font_size}-{len(data)}-{i}", parent=styles["SmallBody"],
            fontSize=font_size, leading=font_size+3, alignment=TA_LEFT))
            for i, v in enumerate(row)])
    table = Table(data, colWidths=widths, repeatRows=1, hAlign="LEFT")
    table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), ACCENT),
        ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
        ("GRID", (0, 0), (-1, -1), 0.45, GRID),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("LEFTPADDING", (0, 0), (-1, -1), 6),
        ("RIGHTPADDING", (0, 0), (-1, -1), 6),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
        *[("BACKGROUND", (0, r), (-1, r), colors.white if r % 2 else PALE)
          for r in range(1, len(data))],
    ]))
    return table


def code_listing(path, title):
    source = path.read_text(encoding="utf-8").splitlines()
    wrapped = []
    for number, line in enumerate(source, 1):
        prefix = f"{number:4d}  "
        chunks = textwrap.wrap(line, width=104, subsequent_indent="      ",
                               replace_whitespace=False, drop_whitespace=False) or [""]
        wrapped.extend((prefix if i == 0 else "      ") + part for i, part in enumerate(chunks))
    return [heading(title, 2), Preformatted("\n".join(wrapped), styles["CodeReport"]), PageBreak()]


story = []
# Cover page follows the supplied department report template.
story += [Spacer(1, 14*mm), para("WASTE MANAGEMENT SYSTEM", "CoverTitle"),
          para("PROJECT REPORT", "CoverSub"), Spacer(1, 10*mm),
          para("Submitted in partial fulfilment of the requirements for the completion of", "CoverSub"),
          Spacer(1, 2*mm), para("PBCST304: OBJECT ORIENTED PROGRAMMING", "CoverTitle"),
          Spacer(1, 7*mm), para("Submitted by", "CoverSub"), Spacer(1, 3*mm)]
members = [
    ("Aman Fayad", "TVE25CS023", "12"),
    ("Joseph Fidel K M", "TVE25CS068", "31"),
    ("Ayshwaria Gopakumar", "TVE25CS041", "21"),
    ("Nivedya Sunil", "TVE25CS101", "51"),
    ("Nida Rahman C", "TVE25CS097", "47"),
]
for name, ktu, roll in members:
    story.append(para(f"<b>{name}</b>  |  KTU ID: {ktu}  |  Roll: {roll}", "CoverSub"))
story += [Spacer(1, 7*mm), Image(str(ROOT / "figures" / "image14.jpg"), width=35*mm, height=39*mm),
          Spacer(1, 6*mm), para("DEPARTMENT OF COMPUTER SCIENCE AND ENGINEERING", "CoverSub"),
          para("COLLEGE OF ENGINEERING TRIVANDRUM", "CoverSub"), Spacer(1, 4*mm),
          para("October 2026", "CoverSub"), PageBreak()]

story += [heading("DECLARATION", 1),
          para("We, the undersigned, declare that the project report entitled <b>Waste Management System</b>, submitted in fulfilment of the requirements for PBCST304: Object Oriented Programming, describes the work carried out by our team at the College of Engineering Trivandrum. The report records the design, implementation, and verification of the updated Java Swing desktop application connected to MySQL using JDBC. Member details below are transcribed from the supplied presentation PDF."),
          Spacer(1, 8*mm), make_table(["Student and KTU ID", "Roll", "Signature"],
          [[f"{n} ({k})", r, ""] for n,k,r in members], [80*mm, 25*mm, 50*mm]),
          Spacer(1, 6*mm), para("The team should check the printed identifiers and sign this declaration before submission."), PageBreak(),
          heading("ACKNOWLEDGEMENT", 1),
          para("We express our sincere thanks to the Department of Computer Science and Engineering, College of Engineering Trivandrum, for providing the academic setting in which this project was completed. We are grateful to the faculty members who introduced the object-oriented programming concepts applied in this work. We also acknowledge Clean CET for the campus-cleanliness context that informed the requirements. Finally, we thank our teammates for their collaboration in planning, implementation, testing, and presentation preparation."),
          PageBreak(), heading("ABSTRACT", 1),
          para("The Waste Management System is a desktop application developed as an Object Oriented Programming mini-project for PBCST304. It provides a graphical interface using Java Swing and stores collection records in MySQL through JDBC and MySQL Connector/J. The model is organized around an abstract Waste class and three concrete categories: organic, recyclable, and hazardous waste. The application supports authenticated member and administrator roles, timestamped record entry, admin-approved completion requests, and date-filtered reports by category, collector, and actual collection area."),
          para("Members can add records, update their own pending entries, request admin approval to mark a collection complete, flag their own pending records as invalid, and view their individual contribution. The administrator can view all records, approve or reject requests, mark records collected directly, delete records, and access overall, member-wise, and area-wise reports. Approval updates request and waste status in one transaction; only completed records count in reports. Collection outside a member's assigned territory is classified as bonus in the member report, while the area report follows actual location and sums recorded kilograms. Totals are derived from the canonical waste table rather than duplicated summaries. Local MySQL installations do not synchronize automatically, and kilograms are not a scientifically measured pollution index."),
          PageBreak(), heading("TABLE OF CONTENTS", 1)]
toc = TableOfContents()
toc.formatter = ReportDocument.page_label
toc.levelStyles = [
    ParagraphStyle(name="TOC0", fontName="Helvetica-Bold", fontSize=9.5, leading=14, leftIndent=0, firstLineIndent=0, spaceBefore=4),
    ParagraphStyle(name="TOC1", fontName="Helvetica", fontSize=8.5, leading=12, leftIndent=14, firstLineIndent=0),
    ParagraphStyle(name="TOC2", fontName="Helvetica", fontSize=8, leading=11, leftIndent=28, firstLineIndent=0),
]
story += [toc, PageBreak(), heading("LIST OF FIGURES", 1),
          make_table(["Figure", "Title", "Page"],
                     [["1", "High-level architecture of the Waste Management System", "1"]],
                     [16*mm, 120*mm, 21*mm]), PageBreak()]

# Main report text uses the updated application source as its authority.
story += [heading("1 Introduction", 1), heading("1.1 Project Background", 2),
          para("Campus waste collection involves recording what was collected, its category, quantity, actual collection location, responsible member, and collection time. Scattered notes make records harder to find, review, and summarize. This project addresses that bounded record-management problem with a desktop application and relational database."),
          para("The system was developed in the Clean CET campus-cleanliness context at the College of Engineering Trivandrum. It is a record system: it does not use sensors to detect waste, dispatch collection vehicles, optimize routes, or calculate pollution from air, soil, or water measurements."),
          heading("1.2 Project Overview", 2),
          make_table(["Layer", "Responsibility", "Implementation"], [
              ["Presentation", "Accept input and show records/reports", "Java Swing frames, panels, tables, event listeners"],
              ["Model and rules", "Represent users, waste, and report data", "Java model classes and manager classes"],
              ["Data access", "Authenticate, query, and update records", "JDBC, PreparedStatement, ResultSet"],
              ["Persistence", "Keep data across application runs", "MySQL database and Connector/J driver"]],
              [35*mm, 62*mm, 60*mm]),
          Spacer(1, 6*mm)]
architecture = Table([[
    Paragraph("<b>Swing GUI</b><br/>Forms and tables", styles["SmallBody"]),
    Paragraph("&gt;", styles["ArrowReport"]),
    Paragraph("<b>Model and managers</b><br/>OOP data and access rules", styles["SmallBody"]),
    Paragraph("&gt;", styles["ArrowReport"]),
    Paragraph("<b>JDBC</b><br/>Prepared SQL", styles["SmallBody"]),
    Paragraph("&gt;", styles["ArrowReport"]),
    Paragraph("<b>MySQL</b><br/>Persistent data", styles["SmallBody"]),
]], colWidths=[34*mm, 5*mm, 42*mm, 5*mm, 24*mm, 5*mm, 38*mm])
architecture.setStyle(TableStyle([
    ("BACKGROUND", (0, 0), (0, 0), PALE),
    ("BACKGROUND", (2, 0), (2, 0), PALE),
    ("BACKGROUND", (4, 0), (4, 0), PALE),
    ("BACKGROUND", (6, 0), (6, 0), PALE),
    ("BOX", (0, 0), (0, 0), 0.8, ACCENT),
    ("BOX", (2, 0), (2, 0), 0.8, ACCENT),
    ("BOX", (4, 0), (4, 0), 0.8, ACCENT),
    ("BOX", (6, 0), (6, 0), 0.8, ACCENT),
    ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
    ("ALIGN", (0, 0), (-1, -1), "CENTER"),
    ("LEFTPADDING", (0, 0), (-1, -1), 4),
    ("RIGHTPADDING", (0, 0), (-1, -1), 4),
    ("TOPPADDING", (0, 0), (-1, -1), 8),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
]))
story += [architecture, para("Figure 1. High-level architecture of the Waste Management System", "CaptionReport"),
          Spacer(1, 4*mm), para("Startup initializes the database and opens LoginFrame. After authentication, WasteManagementGUI presents role-scoped records and reports. A GUI action calls a manager, which runs SQL through JDBC and refreshes the view."),
          heading("1.3 Scope", 2), para("The current desktop application includes login, role-aware add/view/update/delete actions, search, timestamps, pending/completed/invalid states, member completion requests and an admin approval queue, date-filtered reports, member and area summaries, category totals, a Swing chart, CSV export based on the applied date range, logout, and an administrator broadcast inbox with per-member unread/read status."),
          heading("2 Problem Definition", 1), heading("2.1 Problem Statement", 2),
          para("The project provides a consistent way to maintain campus waste-collection records across assigned locations. Each record needs category, description, kilograms, actual location, credited collector, and collection timestamp. The system separates entry from administrative review so incomplete or incorrect records do not count as confirmed collection. Members should see only their own contribution; the administrator needs the wider operational and reporting view."),
          heading("2.2 Objectives", 2)]
story.append(make_table(["No.", "Objective"], [
    ["1", "Build a native desktop GUI with Java Swing."],
    ["2", "Persist collection records in MySQL and connect through JDBC."],
    ["3", "Demonstrate abstraction, inheritance, encapsulation, and polymorphism in the waste model."],
    ["4", "Authenticate administrator and member accounts with role-aware access."],
    ["5", "Record waste category, quantity, actual area, collector, and collection timestamp."],
    ["6", "Separate pending, completed, and invalid records; route member completion requests for admin review."],
    ["7", "Provide personal and administrator-only overall/member/area reports."],
    ["8", "Calculate reports from source records so totals refresh consistently."],
    ["9", "Allow the administrator to broadcast updates and members to track unread notices."]], [15*mm, 142*mm]))
story += [heading("2.3 User Roles", 2),
          para("The seeded account list contains one administrator and thirteen members. The names and territories below reflect the current account configuration."),
          make_table(["Member", "Assigned location"], [
              ["Aman Fayad", "Civil canteen"], ["Joseph Fidel", "Mech front"], ["Nivedya", "Dhwani"],
              ["Ayshwaria", "Gazebo"], ["Nida", "CS front"], ["Arya", "Golden walkway"],
              ["Ahban", "Electrical gate"], ["Sidharth", "Indoor court"], ["Heera", "Archie corner"],
              ["Keerthana", "College front"], ["Farzeen", "DJ hall"], ["Merin", "CGPU hall"],
              ["Shyam", "CETAA hall"]], [55*mm, 102*mm]),
          Spacer(1, 5*mm), make_table(["Action", "Member", "Administrator"], [
              ["View records", "Own authored/credited records", "All records"],
              ["Add and update", "Add as self; update own pending records", "Add/assign and update pending records"],
              ["Mark invalid", "Own pending records", "Any pending record"],
              ["Request completion / approve", "Request own pending record", "Approve/reject; can also mark collected directly"],
              ["Delete", "Not permitted", "Permitted, with confirmation"],
              ["Reports", "Own contribution only", "Overall, each member, and each area"],
              ["Announcements", "Read notices and mark them read", "Publish notices to all members"]],
              [35*mm, 61*mm, 61*mm]),
          heading("3 Methodology", 1), heading("3.1 Layered Design", 2),
          para("Model classes hold domain data and do not manage screen layout or SQL. Swing GUI classes own user interaction and presentation. DAO/manager classes own database operations, authentication, report queries, and role/ownership rules. DatabaseManager centralizes connection settings, startup table creation/migration, and account seeding. This division is small enough for a course project while keeping responsibilities understandable."),
          heading("3.2 Application Workflow", 2),
          para("The Java entry point shows a startup status window and initializes the database in SwingWorker.doInBackground(). On success, its done() method opens LoginFrame on Swing's Event Dispatch Thread. LoginFrame passes credentials to AuthenticationManager, which verifies a salted PBKDF2 hash. On success, an AppUser object is passed to WasteManagementGUI. GUI events validate forms, invoke the appropriate manager, and refresh the view. A member completion request appears in the admin approval queue; WasteManager approves the request and completes the waste record in one transaction. The queue count refreshes every 15 seconds. ReportManager calculates aggregates; AnnouncementManager handles admin broadcasts and member read receipts. Logout disposes the authenticated dashboard and displays a new login window."),
          heading("3.3 Object-Oriented Design", 2), heading("3.3.1 Abstraction and inheritance", 3),
          para("Waste is an abstract superclass with shared record fields and the abstract method getWasteCategory(). OrganicWaste, RecyclableWaste, and HazardousWaste extend Waste and provide category-specific implementations."),
          heading("3.3.2 Encapsulation and polymorphism", 3),
          para("Model fields are private and are accessed through getters and setters. That is the project's direct example of encapsulation. The category method can be called through a Waste reference but returns a value based on the runtime subclass, demonstrating polymorphism. Shared data and behavior live in the parent instead of being repeated."),
          heading("3.3.3 Other Java concepts", 3),
          para("Classes are instantiated for users, waste entries, and report rows. Packages separate model, dao, gui, and main responsibilities. SQL and parsing errors are caught and reported. Swing ActionListeners connect button events to GUI methods. PreparedStatement binds user data to parameterized SQL. Swing components are created on the Event Dispatch Thread; lengthy operations on that thread can make a window unresponsive, so background database work is a production improvement."),
          heading("3.4 Record State and Permissions", 2),
          para("New records use PENDING. A member or administrator can flag an eligible pending record INVALID; the record is retained and excluded from completed reports. Members may request completion for their own pending records. The administrator may approve/reject these requests or mark a selected pending record collected directly. Approval changes request and waste status in one transaction; only completed entries count in reports. Editing or invalidating a record cancels an outstanding request. The administrator can delete records. Member ownership rules are enforced in the manager layer as well as by hiding unavailable controls in the interface."),
          para("created_by records which account entered a row; collected_by identifies the member credited for the collection. This allows an administrator to enter a record for a member. The report compares actual location to the member's assigned area: off-territory collection is bonus in the member view, while the area report attributes that same collection to its actual location."),
          heading("3.5 Database Design", 2),
          para("The schema uses app_users for accounts, waste for collection records, announcements for administrator broadcasts, announcement_reads for per-user read receipts, and completion_requests for member-submitted completion requests and admin review outcomes. The user table contains username, display name, role, assigned area, salt, password hash, and active state. Waste stores category, description, quantity, actual location, status, author, collector, collection timestamp, optional invalid reason, and database-managed creation/update times. Startup applies additive migrations and creates missing communication tables without dropping existing waste contents."),
          para("Reports are not stored as one table per member. ReportManager uses SQL aggregation (SUM, COUNT, grouping, joins, and date bounds) over canonical waste rows. It avoids duplicate totals that could become stale after updates or status changes. PreparedStatement binds values, ResultSet reads query results, and Connector/J provides the JDBC driver implementation for MySQL [1, 2]."),
          heading("3.6 Report Calculation", 2),
          para("For a selected date interval, report queries use only completed records whose collection timestamp lies in the interval. Apply Dates refreshes the summary and member/area tables; CSV export uses the last successfully applied range. Category, collector, and actual location determine grouping. Area totals sum kilograms and count records at the actual location. They describe recorded waste accumulation, not area population or a scientifically measured pollution score. Assigned-versus-bonus classification is a member-report view and does not add another physical collection to the overall total."),
          heading("4 Implementation Details", 1), heading("4.1 System Requirements", 2),
          para("The application requires a Java runtime and a running MySQL Server. Rebuilding source requires a JDK. The MySQL Connector/J JAR is bundled under DesktopApplication/lib. The GUI uses Swing and AWT; no web framework or JavaFX is used by the desktop program. A database account needs privileges for initial database/table creation and migration."),
          heading("4.2 Experimental Setup", 2),
          para("The verified development environment was macOS 26.6.2 (Apple silicon), Eclipse Temurin OpenJDK 21.0.5 LTS, and the local Homebrew MySQL server reported as version 26.7.0. The project was compiled with the supplied shell build script and exercised against the local database. The source includes Windows batch launch/build instructions, but execution on a physical Windows computer was not independently verified in this audit."),
          heading("4.3 Packages and Classes", 2),
          make_table(["Package or class", "Responsibility"], [
              ["model.Waste", "Abstract parent with shared private fields and category contract."],
              ["OrganicWaste, RecyclableWaste, HazardousWaste", "Concrete categories overriding getWasteCategory()."],
              ["AppUser", "Authenticated identity, role, assigned area, DOB, register number, and department."],
              ["RegistrationResult", "New member identity and one-time display value for the generated password."],
              ["ReportSummary, ReportRow", "Typed report totals and member/area output rows."],
              ["DatabaseManager", "JDBC configuration, schema creation/migration, account seeding."],
              ["PasswordHasher, AuthenticationManager", "Salted PBKDF2 password hashing and credential verification."],
              ["UserManager", "Registration validation, random area allocation, admin-only member search, and area management."],
              ["WasteManager", "CRUD, role/ownership checks, and transactional completion-request approval."],
              ["ReportManager", "Date-filtered aggregate report queries."],
              ["AnnouncementManager", "Admin-only broadcast publishing, inbox queries, unread counts, and read receipts."],
              ["Announcement", "Immutable announcement data shown in the inbox."],
              ["LoginFrame", "Swing login and registration form with sign-in event handling."],
              ["CleanButtonUI", "Shared button painter for consistent fills and readable interaction states."],
              ["WasteManagementGUI", "Dashboard, records, admin approval queue, applied-date reports, CSV export, announcements, and logout."],
              ["Main", "Application entry point and startup error handling."]], [58*mm, 99*mm]),
          heading("4.4 Database Fields", 2),
          make_table(["Table", "Fields", "Meaning"], [
              ["app_users", "username, display_name, role", "Login identity and access role."],
              ["app_users", "assigned_area", "Member's regular territory."],
              ["app_users", "date_of_birth, register_number, department", "Registration profile; register number is unique."],
              ["app_users", "password_salt, password_hash, active", "Salted password hash and account state."],
              ["waste", "id, waste_type, description, quantity", "Record identity, category, notes, and kg."],
              ["waste", "location, status, collected_at", "Actual place, review state, collection date/time."],
              ["waste", "created_by, collected_by", "Account that entered the row and member credited."],
              ["waste", "invalid_reason, created_at, updated_at", "Optional invalid explanation and audit timestamps."],
              ["announcements", "title, message, created_by, created_at", "Administrator-to-member broadcasts."],
              ["announcement_reads", "announcement_id, username, read_at", "Per-member read receipts and unread counts."],
              ["registration_areas", "area_name, assigned_username", "Available areas for random member allocation; admin can add areas."],
              ["completion_requests", "waste_id, requested_by, status, reviewed_by, timestamps", "Member requests and admin review history."]], [32*mm, 67*mm, 58*mm]),
          heading("4.5 Authentication and Configuration", 2),
          para("The default JDBC host is localhost:3306 and the database is waste_management_db. WASTE_DB_SERVER, WASTE_DB_NAME, WASTE_DB_USER, and WASTE_DB_PASSWORD can override connection configuration. On a fresh database, WASTE_ADMIN_INITIAL_PASSWORD is read once to initialize the admin hash; it should be set privately and never committed. Password hashes are stored with per-account salts, not plaintext passwords. Join Clean CET collects full name, date of birth, CET register number, and department; the lowercase first name becomes the username. Registration checks the register-number format and uniqueness, but does not verify it with an official registry. The requested username_clean password is predictable, shown once, and unsuitable for production. DOB and register numbers are personal data and require informed consent."),
          para("The fifteen initial registration locations are Industrial department front, EC department front, Rohith square, Sargam stage, Library front, Ohm's villa, Industrial base, College ground, Research block, Old EC department, Hydraulics lab front, Back gate, Mh front, Lh front, and Flag post. Each location is allocated to at most one newly registered member until an administrator adds more locations."),
          para("Localhost means each laptop has a separate database by default. Shared team data requires all clients to use a common reachable MySQL host with secure networking and appropriate privileges. The Java desktop app is not a hosted synchronization service."),
          heading("4.6 Build and Run", 2),
          para("On Windows, run build.bat and then run.bat from DesktopApplication. On macOS/Linux, run ./build.sh followed by ./run.sh. The build compiles against the bundled Connector/J JAR and creates dist/WasteManagementSystem.jar. A running MySQL service is required when launching the application."),
          heading("4.7 Security and Operational Considerations", 2),
          para("PBKDF2-derived salted hashes protect stored passwords. Join Clean CET collects full name, date of birth, CET register number, and department; the lowercase first name becomes the username. Registration validates the register-number format and uniqueness but does not check an official registry. The requested username_clean password is predictable, displayed once, and suitable only for a classroom prototype. DOB and register numbers are personal data and should be collected only with informed consent. Role checks in the desktop client and DAO layer do not protect someone with direct database credentials. A production system would put authorization behind a trusted service, protect secrets, implement account recovery, and maintain audit logs and backups."),
          heading("5 Results", 1), heading("5.1 Verification Performed", 2),
          para("The Java source compiled successfully with the supplied build script. A JDBC connection using environment-configured local MySQL credentials was verified; secrets are not included in this report. MySQL integration checks confirmed the original thirteen USER accounts and one ADMIN remain intact. A temporary registration test verified lowercase first-name account creation, DOB/register/department persistence, assignment of an available area, successful authentication, duplicate-register rejection, and admin-only profile search; the test account was deleted afterward. Existing users' new profile fields remain blank and display as not provided. Additional MySQL checks covered authorization, update, admin-only completion/deletion, invalid states, reports, date filtering, bonus attribution, completion approval/rejection, and direct admin completion."),
          heading("5.2 Functional Test Cases", 2),
          make_table(["Test", "Input/action", "Expected result"], [
              ["Login", "Valid member credentials", "Member identity and assigned area load."],
              ["Bad password", "Incorrect password", "Access denied; no workspace opens."],
              ["Member restrictions", "Attempt complete or delete", "Operation is denied; admin-only controls are unavailable."],
              ["Add record", "Category, positive kg, location, timestamp", "Pending record is stored and shown."],
              ["Invalid record", "Member flags own pending entry", "Status is INVALID; excluded from completed reports."],
              ["Request completion", "Member requests completion", "Waste stays pending and appears in admin approval queue."],
              ["Approval", "Admin approves member request", "Request and waste statuses update atomically; reports include it."],
              ["Reject and retry", "Admin rejects; member requests again", "Waste stays pending and resubmission is accepted."],
              ["Direct admin collection", "Admin selects pending row and marks collected", "Status changes to COMPLETED; reports include it."],
              ["Bonus attribution", "Member records collection outside assigned area", "Member report calls it bonus; area report uses actual location."],
              ["Date filter", "Apply start/end dates", "Dashboard aggregates completed records in the applied range."],
              ["Logout", "Log out and sign in as another role", "Login returns; next dashboard uses the newly authenticated role."],
              ["Announcements", "Admin publishes; member opens notice", "Member sees unread notice, read receipt updates, member cannot publish."],
              ["CSV export", "Apply a date range, then export", "Export uses the last successfully applied range."],
              ["Join Clean CET", "Submit a valid new member profile", "Lowercase first-name username, salted password hash, random unused area, and sign-in-ready account are created."],
              ["Duplicate registration", "Reuse an existing register number or first-name username", "Registration is rejected without assigning or consuming an area."],
              ["Member directory permissions", "Open directory as admin, then attempt member access", "Admin sees member profile/area/status; regular member search is denied."]], [27*mm, 58*mm, 72*mm], font_size=7.5),
          heading("5.3 Observed Results", 2),
          para("The administrator interface shows total and category summary values, member-wise contribution, and area-wise collection with a Swing chart and CSV export. The administrator can review requests in a queue whose count refreshes every 15 seconds, or mark records collected directly. Admin broadcasts and member read receipts remain available. Members see their own report and progress. Reports include approved completed records only and show recorded kilograms by actual location; these are not measurements of pollution or population."),
          heading("6 Conclusion and Future Works", 1), heading("6.1 Conclusion", 2),
          para("The project delivers a role-based campus waste register as a Java Swing desktop application with MySQL persistence through JDBC. Its structure makes the required OOP concepts visible, separates UI/model/data responsibilities, and supports authentication, timestamped entries, review states, and report generation. SQL aggregation over one source-of-truth waste table keeps member, area, and overall summaries aligned with current records."),
          heading("6.2 Limitations", 2),
          para("Default installations are local and do not synchronize; team-wide requests require a shared reachable MySQL server. Announcements are administrator-to-member broadcasts, not two-way chat, and the approval count polls every 15 seconds rather than using push. Reports summarize kilograms by category and location, not a validated pollution score or population estimate. Initial member credentials follow a predictable convention. Client-side authorization can be bypassed by direct database access. Synchronous JDBC work can block the Swing event thread on a slow server. No formal performance, usability, or environmental-impact study is claimed."),
          heading("6.3 Future Enhancements", 2),
          para("Future work could add a secured shared service, push notifications, first-login password changes and account recovery, audit logs, backups, and background database operations using SwingWorker. Verified environmental measurements could support a carefully defined pollution indicator. PDF export, configurable time zones, and formal usability and performance testing are also possible extensions."),
          heading("References", 1),
          para("[1] Oracle Corporation and MySQL, <i>MySQL Connector/J Developer Guide: Overview</i>. https://dev.mysql.com/doc/connector-j/en/connector-j-overview.html (accessed 17 October 2026).", "SmallBody"),
          para("[2] Oracle Corporation, <i>Using Prepared Statements</i>, The Java Tutorials: JDBC Database Access. https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html (accessed 17 October 2026).", "SmallBody"),
          para("[3] Oracle Corporation, <i>The Event Dispatch Thread</i>, The Java Tutorials: Creating a GUI With Swing. https://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html (accessed 17 October 2026).", "SmallBody"),
          para("[4] Oracle Corporation, <i>Abstract Methods and Classes</i>, The Java Tutorials: Inheritance. https://docs.oracle.com/javase/tutorial/java/IandI/abstract.html (accessed 17 October 2026).", "SmallBody"),
          heading("Appendix A: Database Setup and Inspection", 1),
          para("The application creates or migrates its tables at startup. The following read-oriented SQL commands can be used to inspect the resulting schema and data."),
          Preformatted("USE waste_management_db;\nSHOW TABLES;\nDESCRIBE app_users;\nDESCRIBE waste;\nDESCRIBE announcements;\nDESCRIBE announcement_reads;\nDESCRIBE completion_requests;\nSELECT username, display_name, role, assigned_area FROM app_users;\nSELECT id, waste_type, quantity, location, status, collected_at, collected_by\nFROM waste ORDER BY collected_at DESC;\nSELECT waste_type, SUM(quantity) AS kilograms\nFROM waste WHERE status = 'COMPLETED' GROUP BY waste_type;", styles["CodeReport"]),
          heading("Appendix B: Source Code", 1),
          para("The source files below are read directly from the updated DesktopApplication/src tree. This appendix therefore corresponds to the desktop application version described in this report.")]

source_files = [
    ("src/main/Main.java", "B.1 Main"),
    ("src/model/Waste.java", "B.2 Waste model"),
    ("src/model/OrganicWaste.java", "B.3 OrganicWaste"),
    ("src/model/RecyclableWaste.java", "B.4 RecyclableWaste"),
    ("src/model/HazardousWaste.java", "B.5 HazardousWaste"),
    ("src/model/AppUser.java", "B.6 AppUser"),
    ("src/model/RegistrationResult.java", "B.7 RegistrationResult"),
    ("src/model/ReportSummary.java", "B.8 ReportSummary"),
    ("src/model/ReportRow.java", "B.9 ReportRow"),
    ("src/dao/DatabaseManager.java", "B.10 DatabaseManager"),
    ("src/dao/PasswordHasher.java", "B.11 PasswordHasher"),
    ("src/dao/AuthenticationManager.java", "B.12 AuthenticationManager"),
    ("src/dao/UserManager.java", "B.13 UserManager"),
    ("src/dao/WasteManager.java", "B.14 WasteManager"),
    ("src/dao/ReportManager.java", "B.15 ReportManager"),
    ("src/dao/AnnouncementManager.java", "B.16 AnnouncementManager"),
    ("src/model/Announcement.java", "B.17 Announcement model"),
    ("src/gui/LoginFrame.java", "B.18 LoginFrame"),
    ("src/gui/CleanButtonUI.java", "B.19 CleanButtonUI"),
    ("src/gui/WasteManagementGUI.java", "B.20 WasteManagementGUI"),
]
for relative, title in source_files:
    story.extend(code_listing(REPO / "DesktopApplication" / relative, title))
    if story and isinstance(story[-1], PageBreak):
        story.pop()


document = ReportDocument(str(OUT))
document.multiBuild(story)
print(OUT)
