# Waste Management System

This repository contains two separate parts:

```text
DesktopApplication/   Java Swing + MySQL desktop application
Website/              Viva/presentation website explaining the project
```

## 1. Desktop Application

Location:

```text
DesktopApplication/
```

This is the actual Java OOP mini project.

It includes:

- Java Swing desktop GUI
- MySQL backend
- JDBC connection
- MySQL Connector/J driver
- CRUD operations
- Runnable JAR
- Windows and macOS/Linux run scripts
- SQL setup file

### Run On macOS/Linux

```bash
cd DesktopApplication
brew services start mysql
./build.sh
./run.sh
```

### Run On Windows

Open Command Prompt inside `DesktopApplication`, then run:

```bat
build.bat
run.bat
```

### MySQL Check Commands

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

## 2. Viva Website

Location:

```text
Website/
```

This is a static website that explains the whole project for viva and presentation preparation.

It includes:

- Full project overview
- Software and package explanation
- OOP concept explanation
- Swing explanation
- Every file explanation
- Detailed code walkthrough
- MySQL commands
- Presentation script
- Viva questions and answers

### Live Website

https://waste-viva-joseph-1791639455.web.app

### Detailed Code Walkthrough

https://waste-viva-joseph-1791639455.web.app/code-walkthrough

### Run Website Locally

```bash
cd Website
python3 -m http.server 8085
```

Then open:

```text
http://localhost:8085
```

## Technologies Used

- Java
- Java Swing
- MySQL
- JDBC
- MySQL Connector/J
- Firebase Hosting
- HTML, CSS, JavaScript

## OOP Concepts Demonstrated

- Classes and objects
- Encapsulation
- Inheritance
- Abstraction
- Polymorphism
- Exception handling
- Packages
- Swing event handling

