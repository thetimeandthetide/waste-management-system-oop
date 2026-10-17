# Waste Management System

A Java Swing desktop application backed by MySQL through JDBC. The repository keeps the desktop application and the separate viva learning website in distinct folders.

```text
DesktopApplication/   Java source, JDBC driver, SQL setup, build/run scripts
Website/              Static viva and code-study website
```

## Desktop application

Requirements: Java 8 or newer, MySQL Server, and the bundled `lib/mysql-connector-j.jar`.

1. Start MySQL Server.
2. In a terminal opened in `DesktopApplication`, build and launch:

```sh
./build.sh
./run.sh
```

On Windows, run `build.bat`, then `run.bat` from Command Prompt in `DesktopApplication`.

At first startup the application creates `waste_management_db`, creates or migrates its tables, and seeds the administrator and 13 member accounts. It shows a startup status window while MySQL setup runs in a background `SwingWorker`. Set `WASTE_ADMIN_INITIAL_PASSWORD` privately before the first launch to initialize the admin; after the admin row exists, this variable is no longer needed. Data stays in the MySQL server configured for that machine. The default connection is `localhost`, user `root`, empty password. Override it without editing source:

```text
WASTE_DB_SERVER=jdbc:mysql://localhost:3306/
WASTE_DB_NAME=waste_management_db
WASTE_DB_USER=root
WASTE_DB_PASSWORD=your_mysql_password
WASTE_ADMIN_INITIAL_PASSWORD=choose-a-private-admin-password
```

On Windows Command Prompt, use `set WASTE_DB_PASSWORD=your_mysql_password` before `run.bat`. For teammates to see one shared live dataset, all installs must connect to the same reachable MySQL server, and its network access/firewall/user privileges must be configured. The bundled default is local-only; it does not synchronize separate computers. Never commit real MySQL credentials.

The admin username is `deepthirani`; the initial password is supplied privately on first setup and stored as a salted PBKDF2 hash. Member usernames are lowercase first names. The member initial-password convention is for classroom use only; change credentials before real use. Never commit passwords or production database credentials.

### Inspect MySQL data

```sh
mysql -u root -p
```

```sql
SHOW DATABASES;
USE waste_management_db;
SHOW TABLES;
DESCRIBE waste;
SELECT id, waste_type, quantity, location, status, collected_at, collected_by FROM waste ORDER BY collected_at DESC;
SELECT username, display_name, assigned_area, role FROM app_users;
```

Reports are calculated from completed records in `waste`; per-person and per-area report tables are intentionally not duplicated. This keeps totals current and avoids stale summary rows. The admin can broadcast announcements; members can see unread notices and read receipts in their inbox. Announcements are not real-time push or two-way chat.

## Viva website

Open `Website/index.html` locally or use the hosted site: [Waste Management Viva Guide](https://waste-viva-joseph-1791639455.web.app). The detailed file guide is at [Full Code Walkthrough](https://waste-viva-joseph-1791639455.web.app/code-walkthrough).

The updated implementation and presentation notes are in [DesktopApplication/ROLE_REPORTS_VIVA_ADDENDUM.md](DesktopApplication/ROLE_REPORTS_VIVA_ADDENDUM.md). The website is a teaching companion only; the actual application is a native Swing desktop program.

## Important report rules

- User reports show only that signed-in member's completed contributions.
- Admin reports show the overall totals, member-by-member breakdown, and area-by-area breakdown.
- The assigned area is credited as assigned collection; a different actual location is credited as bonus collection for the member, while area totals follow the actual location.
- Only completed records count in reports. Pending and invalid entries remain visible in operational records but are excluded from completed collection totals.
- The collected date/time is entered on the record. Database timestamps support date-range filtering.

The reported amount is recorded waste weight in kilograms, not an independently measured pollution index. A pollution score would require additional validated environmental measurements.
