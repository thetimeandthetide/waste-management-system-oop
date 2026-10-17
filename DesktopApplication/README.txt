WASTE MANAGEMENT SYSTEM - DESKTOP APPLICATION
================================================

Native Java Swing desktop app. MySQL is the data store and JDBC is the connection layer.

REQUIREMENTS
- Java 8 or newer (JDK needed to rebuild)
- MySQL Server running
- Bundled lib/mysql-connector-j.jar (already included)

RUN ON WINDOWS
1. Install/start MySQL Server.
2. Open Command Prompt in this DesktopApplication folder.
3. Run build.bat (only needed to compile source or rebuild the JAR).
4. Run run.bat.

RUN ON macOS/Linux
1. Start MySQL Server.
2. From this folder run ./build.sh and then ./run.sh.

On first launch the app creates waste_management_db, app_users, waste,
announcements and announcement_reads, applies safe additive schema migrations,
and seeds the 13 member accounts plus one admin.
The default MySQL settings are localhost:3306, database waste_management_db,
username root, and an empty password. Set environment variables before launch
to override them: WASTE_DB_SERVER, WASTE_DB_NAME, WASTE_DB_USER, WASTE_DB_PASSWORD.
Example Windows Command Prompt: set WASTE_DB_PASSWORD=your_mysql_password

The default server is local to each computer. Separate teammate laptops will not
share data unless they are configured to use the same reachable MySQL server.
Do not place production database credentials in source control.

The admin username is deepthirani. On the first launch against a new database,
set WASTE_ADMIN_INITIAL_PASSWORD privately before starting the app; it is used
once to create the salted PBKDF2 admin hash and is not needed after seeding.
Member accounts use their lowercase first name and the requested classroom
password convention. Passwords are stored as salted PBKDF2 hashes. Do not commit
credentials or use these initial credentials for a production deployment.

DATABASE INSPECTION
mysql -u root -p
USE waste_management_db;
SHOW TABLES;
DESCRIBE waste;
SELECT * FROM waste ORDER BY collected_at DESC;
SELECT username, display_name, assigned_area, role FROM app_users;

Reports are calculated from completed rows in waste, not maintained in one table
per user. Admins can broadcast announcements; members see unread notices and
read receipts in the application inbox. Notices are not real-time push or direct
two-way chat. The implementation walkthrough and limitations are in
ROLE_REPORTS_VIVA_ADDENDUM.md.
