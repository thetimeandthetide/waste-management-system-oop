# Login, Roles, Reports: Updated Viva Notes

This addendum describes the current role-based implementation. It takes precedence over older study notes wherever those still mention a generic CRUD-only version, a `Collected` status, or direct startup into the main window.

## What the application does

It is a desktop-only Java Swing application. `Main` initializes MySQL and opens `LoginFrame`. After authentication, a role-aware `WasteManagementGUI` provides a record workspace and a report workspace. MySQL stores member accounts and waste records. The reports are calculated from stored records each time they are requested, so a separate table per person or a manually maintained overall table is unnecessary and could become inconsistent.

## Source file map

- `src/main/Main.java`: Java entry point; initializes schema, then opens the login window on Swing's Event Dispatch Thread.
- `src/gui/LoginFrame.java`: username/password form, login button event, calls authentication, passes the authenticated user to the main GUI.
- `src/gui/WasteManagementGUI.java`: builds the role-aware Swing dashboard, record form/table, admin collection-approval queue, reports, applied date filters, chart, CSV export, announcements inbox, validation, confirmation dialogs, and button event listeners.
- `src/gui/CleanButtonUI.java`: paints shared button backgrounds and interaction states consistently across operating-system look-and-feels.
- `src/model/Waste.java`: abstract waste entity with private common data and accessors; includes collector, record author, collected time, and invalid reason.
- `src/model/OrganicWaste.java`, `RecyclableWaste.java`, `HazardousWaste.java`: concrete subclasses implementing `getWasteCategory()`.
- `src/model/AppUser.java`: authenticated identity, role, and assigned territory passed through the app.
- `src/model/ReportSummary.java`: aggregate totals for a report period.
- `src/model/ReportRow.java`: one member or area breakdown row.
- `src/dao/DatabaseManager.java`: environment-based connection settings, database/table creation and additive migration, account seeding, and JDBC connections.
- `src/dao/PasswordHasher.java`: salted PBKDF2 password hashing and constant-time comparison; plaintext passwords are not stored.
- `src/dao/AuthenticationManager.java`: looks up a username and verifies its password hash.
- `src/dao/WasteManager.java`: prepared-statement CRUD and record-state changes; applies the app's role/ownership restrictions.
- `src/dao/ReportManager.java`: parameterized aggregate queries for member, area, and personal reports.
- `src/model/Announcement.java` and `src/dao/AnnouncementManager.java`: announcement data, admin-only publishing, member inbox queries, unread counts, and per-member read receipts.
- `src/dao/WasteManager.java`: role-checked CRUD, member completion requests, transactional admin approval/rejection, and cancellation of stale requests when a record changes.
- `sql/setup.sql`: non-destructive SQL schema setup for a database administrator who wants to inspect/create the schema manually.
- `lib/mysql-connector-j.jar`: JDBC driver, required when compiling/running.
- `build.sh` / `build.bat`: compile source and produce `dist/WasteManagementSystem.jar`.
- `run.sh` / `run.bat`: start the packaged desktop application.

## Data model and SQL

`app_users` stores the login identity, role, assigned area, password hash, salt, and active state. `waste` stores type, description, quantity in kg, actual location, status, record author (`created_by`), credited collector (`collected_by`), collection timestamp, and optional invalid reason. `announcements` stores admin broadcasts, `announcement_reads` records each member's read status, and `completion_requests` records member requests and administrator review outcomes. Foreign keys connect these records; startup adds tables/columns without dropping waste records.

`PreparedStatement` binds runtime values to `?` placeholders. This separates SQL syntax from user input and prevents ordinary input from changing the query structure. `ResultSet` reads the rows returned by `SELECT`. `try`-with-resources closes JDBC connections, statements, and results when a method finishes.

Useful inspection queries:

```sql
USE waste_management_db;
SHOW TABLES;
DESCRIBE app_users;
DESCRIBE waste;
SELECT username, display_name, role, assigned_area, active FROM app_users;
SELECT id, waste_type, quantity, location, status, collected_at, collected_by FROM waste ORDER BY collected_at DESC;
SELECT waste_type, SUM(quantity) AS kg FROM waste WHERE status='COMPLETED' GROUP BY waste_type;
SELECT location, waste_type, SUM(quantity) AS kg FROM waste WHERE status='COMPLETED' GROUP BY location, waste_type ORDER BY location;
```

There is one normalized record table, not one table for each person. Reports use SQL `SUM`, `COUNT`, `GROUP BY`, date filters, and joins to compute current totals. Duplicating reports into 14 extra tables would introduce synchronization/update bugs. Only completed records count in report totals. Invalid and pending records do not count.

## Roles and authorization

| Action | Member | Admin |
|---|---:|---:|
| View operational records | Own/authored or credited records | All records |
| Add record | Yes, as self | Yes; may assign collector |
| Update record | Own/credited pending records | Any pending record |
| Mark invalid | Own/credited pending record | Any pending record |
| Request completion / mark as collected | Request own pending record | Approve/reject requests; may mark a pending record collected directly |
| Delete | No | Yes, with confirmation in GUI |
| Reports | Own contribution only | Overall, all 13 members, and all areas |
| Announcements | Read broadcast notices; unread status clears when opened | Publish notices to all members |

Members are seeded with their assigned areas: Aman Fayad/Civil canteen, Joseph Fidel/Mech front, Nivedya/Dhwani, Ayshwaria/Gazebo, Nida/CS front, Arya/Golden walkway, Ahban/Electrical gate, Sidharth/Indoor court, Heera/Archie corner, Keerthana/College front, Farzeen/DJ hall, Merin/CGPU hall, and Shyam/CETAA hall. Usernames are lowercase first names.

The actual `location` is where collection happened. If it differs from the member's assigned area, that amount contributes to their personal bonus total, while the area report credits the actual collection location. Area totals are calculated as kilograms and record counts grouped by location; they are not a population estimate or scientific pollution measure. The attributed collector (`collected_by`) and record author (`created_by`) are distinct so an admin can enter a record for a member without claiming to have collected it.

Members cannot directly complete a record. They submit an approval request. The admin's collection-approval queue refreshes its count every 15 seconds; approval and record completion happen in one database transaction. Rejection leaves the record pending. Editing or invalidating a record cancels a stale pending request. Reports and CSV exports use completed rows within the last successfully applied date range.

Announcements are administrator-to-member broadcasts, not private replies. Each member has an inbox and a per-notice read receipt. The unread count refreshes when the inbox is opened; this is not a real-time push notification service.

## Timestamp and report interpretation

Each record requires its collection date/time in `yyyy-MM-dd HH:mm` format (the code also accepts ISO local datetime input). Date filters apply to completed collection timestamps. Amounts are kilograms. The dashboard gives category totals (Organic, Recyclable, Hazardous), overall totals, member totals, assigned-vs-bonus totals, and location totals. This is a waste-weight report, not a measured pollution or toxicity score; a scientifically meaningful pollution indicator would need separate measured inputs and a defined method.

## Security and practical limits

Passwords are salted PBKDF2 hashes. For a fresh database, the admin password is read once from `WASTE_ADMIN_INITIAL_PASSWORD` and hashed during seeding; the password-derived hash is not embedded in source. The requested member password convention is predictable and is for this class project only; change it before real operational use. The desktop app uses role checks in its Java data-access layer, but a person with direct SQL access using the shared database account can bypass GUI permissions. A production deployment would need individual database/API identities or a trusted server, secure credential rotation, backups, audit logging, and stronger centralized authorization.

By default each computer connects to its own `localhost` MySQL. That means different installations do not automatically share records. Team-wide shared operation requires a common remote MySQL host and secure network configuration. Do not expose MySQL publicly or commit the DB password. Set `WASTE_DB_SERVER`, `WASTE_DB_NAME`, `WASTE_DB_USER`, and `WASTE_DB_PASSWORD` as environment variables to configure a common server. The app requires schema/account creation permissions during initial setup.

The dashboard's charts are drawn in Swing using Java2D; no charting framework or web technology is used in the application. The separate `Website/` directory is static HTML/CSS/JavaScript for learning only. Firebase hosts that learning site, not the desktop app or its database.

## Suggested video capture (about 4 minutes)

1. Start on the login screen; identify the native Swing app and explain login routes each role to permitted data.
2. Sign in as a member. Show only their records, add a pending record with category, kg, actual location, and collection timestamp, then request completion approval. Explain that the row remains pending until an admin reviews it; a location outside their territory is counted as bonus in their own report after approval.
3. Mark a test record invalid and show the status. Explain invalid records are excluded from completed report totals.
4. Sign out and sign in as admin. Show the approval badge and pending queue, approve the member request, then show the record becomes collected.
5. Show the admin-only direct "Mark as collected" action on another throwaway pending row; show delete only with a throwaway record after a clear confirmation.
6. Show overall/member/area report, apply a date range, and export CSV for that applied range.
7. Optionally show `SELECT ... FROM waste` and `SELECT ... FROM completion_requests` in MySQL Workbench/terminal to demonstrate persistence and review timestamps.
8. Open Announcements and explain admin broadcasts and member read receipts. End with Swing UI -> manager/DAO -> JDBC Connector/J -> MySQL; mention that team machines need one shared configured MySQL host for shared data.

## Likely viva questions

**Why not create a report table for each user?** Reports are derived from canonical waste rows with aggregation. Duplicated totals can drift when a record changes, is invalidated, or is deleted.

**What does `collected_by` mean versus `created_by`?** One identifies whose contribution gets credit; the other records who entered the row. This supports admin-entered records for members.

**When does waste count in reports?** Only after an admin directly marks a pending record collected or approves a member's completion request. Invalid and pending rows are excluded.

**What happens after a member requests completion?** The record stays PENDING. The admin approval queue lists the record and requester; approval changes request and record status in a single transaction. Rejection leaves it pending so it can be corrected and resubmitted.

**How is bonus collection determined?** Compare actual collection location with the authenticated member's assigned area. The member gets personal bonus credit for a different area; the area aggregate follows the actual location.

**How does JDBC connect?** Connector/J implements the JDBC driver; `DriverManager` opens a `Connection` using the configured JDBC URL. Managers execute parameterized SQL and map result rows into model/report objects.

**Is the app multi-user across laptops automatically?** No. The default is local MySQL. All computers must be configured for the same reachable server; separate local DBs remain separate.

**Does a weight total measure pollution?** No. It reports recorded waste mass and categories. Pollution estimation needs additional validated environmental data.

**Can a user bypass role permissions?** The GUI and DAO restrict actions, but the desktop app is not a hardened server. Direct database access can bypass these client-side rules; production needs centralized authorization and protected credentials.

**Why use `PreparedStatement`?** It binds values safely, handles types, and avoids assembling SQL by concatenating user input.

**How does Swing react to clicks?** `addActionListener` registers an event listener. When a button event occurs, its handler calls a GUI method, then a manager method performs the database operation and refreshes the view.
