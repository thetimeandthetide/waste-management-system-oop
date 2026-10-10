# Waste Management System

A beginner-friendly Java OOP desktop application built with Swing, MySQL, and JDBC.

## What It Does

- Add waste records
- View all waste records
- Update selected records
- Delete selected records
- Search records
- Mark waste as collected
- Store data permanently in MySQL

## Technologies Used

- Java
- Java Swing
- MySQL
- JDBC
- MySQL Connector/J
- Firebase Hosting for the viva website

## Project Structure

```text
src/model      OOP model classes
src/dao        JDBC and CRUD database classes
src/gui        Swing interface
src/main       Application entry point
sql            MySQL setup script
lib            MySQL JDBC connector
dist           Runnable JAR
VivaWebsite    Website explaining the full project for viva/presentation
```

## Run On macOS/Linux

```bash
brew services start mysql
./build.sh
./run.sh
```

## Run On Windows

1. Install Java JDK and MySQL.
2. Make sure MySQL is running.
3. Open Command Prompt inside this folder.
4. Run:

```bat
build.bat
run.bat
```

## MySQL Check Commands

```bash
mysql -u root
```

```sql
SHOW DATABASES;
USE waste_management_db;
SHOW TABLES;
DESCRIBE waste;
SELECT * FROM waste;
```

## Live Viva Website

https://waste-viva-joseph-1791639455.web.app

Detailed walkthrough:

https://waste-viva-joseph-1791639455.web.app/code-walkthrough

## OOP Concepts Demonstrated

- Classes and objects
- Encapsulation
- Inheritance
- Abstraction
- Polymorphism
- Exception handling
- Packages
- Swing event handling

