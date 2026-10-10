Waste Management System
=======================

Beginner-friendly Java Swing + MySQL OOP mini project.
This is a full desktop project, not a web application.

Packages:
- model: OOP classes
- dao: JDBC database classes
- gui: Swing GUI
- main: Application starting point

Default database configuration:
- Database: waste_management_db
- Username: root
- Password: empty

The application automatically creates the MySQL database and waste table
when it starts, as long as MySQL is installed and running.

Important files:
- sql/setup.sql: Creates database/table and inserts sample records
- src/dao/DatabaseManager.java: Change MySQL username/password here
- build.sh: Compiles the project and creates a runnable JAR
- run.sh: Runs the project
- build.bat: Windows compile script
- run.bat: Windows run script

Mac/Linux:
1. Make sure MySQL is running.
2. ./build.sh
3. ./run.sh

Windows:
1. Install MySQL and make sure it is running.
2. Keep mysql-connector-j.jar inside lib.
3. Run build.bat.
4. Run run.bat.

Optional manual database setup:
- sql/setup.sql can still be imported in MySQL Workbench if your teacher asks
  to see the SQL separately.
