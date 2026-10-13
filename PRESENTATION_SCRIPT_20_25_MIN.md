# Waste Management System - 20-25 Minute Presentation Script

Use this as a speaking script, not as text to read word-for-word from the slide. The best delivery is: look at the slide, speak the main idea confidently, then explain the technical part in your own voice.

Team context:
- Joseph Fidel K M: Clean CET Treasurer, slides 1-5
- Aman Fayad: CleanCET CS Coordinator, slides 6-11
- Nivedya Sunil: CleanCET CS Representative, slides 12-16
- Ayshwaria Gopakumar: CleanCET CS Coordinator, slides 17-20

Suggested total time: 22-24 minutes

---

## Joseph Fidel K M - Slides 1 to 5

Role introduction: Clean CET Treasurer

Target time: 6-7 minutes

### Slide 1 - Title Slide

Good morning respected sir and everyone present here.

We are presenting our Object Oriented Programming mini project titled **Waste Management System**.

I am Joseph Fidel K M, Treasurer of Clean CET. As part of Clean CET, we regularly think about waste collection, campus cleanliness and how small systems can make operations more organized. That connection is what inspired this project. We wanted to build something simple, but meaningful: a desktop application that can record, classify, search and update waste collection details.

Our project is a **Java Swing desktop application** powered by **MySQL and JDBC**. It is not a web application. It runs as a desktop window, stores data in a database, and demonstrates core OOP concepts clearly through Java classes.

The phrase on the slide, **"From records to responsible action"**, captures the idea. Waste management begins with reliable records. If we do not know what waste exists, where it is, how much there is, and whether it has been collected, we cannot manage it properly. This project solves that basic record-management problem.

Our team members are Aman Fayad, Joseph Fidel K M, Ayshwaria Gopakumar, Nivedya Sunil and Nida Rahman C. Each of us contributed to understanding, building, testing and presenting the system.

Smooth transition:
Before going into the code, I will first explain the presentation roadmap and the problem we are trying to solve.

Possible sir question:
**Why did you choose this topic?**

Answer:
We are connected with Clean CET, so waste collection and campus cleanliness are practical themes for us. Also, this topic is suitable for OOP because waste categories can be represented using parent and child classes.

---

### Slide 2 - Presentation Outline

This slide shows the structure of our presentation.

We divided the presentation into seven sections. First, we introduce the project. Then we explain the problem and objectives. After that, we explain the design methodology, implementation setup, implementation results, individual contributions and conclusion.

The project follows a very direct flow. We start with the real-world need, then we explain how that need becomes software design, and finally we show how the software is implemented using Java, Swing, JDBC and MySQL.

The most important point is that this is not only a CRUD project. It is also an OOP project. So throughout the presentation, we will connect each feature back to OOP concepts like abstraction, inheritance, encapsulation and polymorphism.

Our expected presentation duration is around 20 to 25 minutes, followed by viva questions.

Possible sir question:
**What is the core objective of the project?**

Answer:
The core objective is to maintain waste records digitally with add, view, update, delete, search and collection-status tracking, while demonstrating OOP principles in Java.

---

### Slide 3 - Introduction

The main idea of our project is to create a **digital ledger for waste collection**.

In many small organizations or campus clubs, waste collection details may be handled manually. For example, someone may write down the type of waste, quantity, location and whether it was collected. But paper records can be misplaced, repeated or difficult to search.

Our application provides a simple desktop form where the user can enter:
- Waste type
- Description
- Quantity
- Location
- Status

The status can be **Pending** or **Collected**.

Once the record is added, it is stored in MySQL. This means the data does not disappear when the application is closed. It can be retrieved again later.

The slide also shows the flow of information:

First, the **user** enters data through the Swing form. Then the application creates a **Waste subtype object**, such as OrganicWaste, RecyclableWaste or HazardousWaste. Finally, that object is stored in the **MySQL table**.

So the flow is:

User input -> Java object -> MySQL storage

We have intentionally kept the project scope focused. This application does not do IoT sensing, route optimization or automatic pickup scheduling. It focuses on record management, which is the foundation for any bigger waste-management system.

Possible sir question:
**Why did you keep the scope limited?**

Answer:
Because this is an OOP mini project. We focused on building a complete and explainable desktop CRUD system rather than adding complex features that would distract from OOP and JDBC concepts.

---

### Slide 4 - Problem And Objectives

This slide explains why the system is needed.

The current problem is fragmented information. If records are kept manually, three issues can happen:

First, paper files can be misplaced or duplicated. Second, collection status becomes difficult to audit. Third, manual lookup and edits take time.

For example, if someone asks, "Which hazardous waste records are still pending?", a paper system makes that hard to answer quickly. But in our application, the user can search or filter based on type, location or status.

Our objectives are:

1. Centralize add, view, edit and delete operations.
2. Classify waste records into clear categories.
3. Enable fast search.
4. Persist data in MySQL.
5. Refresh the visible table after every operation.

The important technical point is that every visible action in the GUI connects to a database operation. When the user adds data, an INSERT query runs. When they update, an UPDATE query runs. When they delete, a DELETE query runs. When they view or search, SELECT queries run.

Possible sir question:
**What problem does MySQL solve here?**

Answer:
MySQL gives persistent storage. If we store records only in variables or arrays, data disappears when the program closes. MySQL keeps the records permanently until deleted or modified.

---

### Slide 5 - Proposed Solution

This slide summarizes the six actions implemented in the application.

The first action is **Add**. The user enters a new waste record and the application stores it in MySQL.

The second action is **View**. The application reads records from the database and displays them in a JTable.

The third action is **Update**. The user selects a row, edits the details in the form, and saves the changes.

The fourth action is **Delete**. The user selects a record and removes it from the database using its ID.

The fifth action is **Search**. The user can search partial text. For example, typing "Organic" or "Lab" can return matching records.

The sixth action is **Collect**. This changes the selected record status to **Collected**.

Technically, the GUI buttons do not directly write SQL inside the button code. Instead, they call methods in a DAO class called **WasteManager**. That separation is important because it keeps the GUI code and database code organized.

So when we click Add, the GUI calls `addWaste()`, and that calls `WasteManager.addWaste()`. When we click View, it calls `WasteManager.getAllWaste()`.

This is the handover point to the design section, where Aman will explain how the system is structured internally.

Handover:
Now Aman will explain the design methodology, the layers of the system, the package structure, OOP design and database schema.

Possible sir question:
**What is CRUD?**

Answer:
CRUD means Create, Read, Update and Delete. In this project, Add is Create, View/Search is Read, Update is Update, and Delete is Delete.

---

## Aman Fayad - Slides 6 to 11

Role introduction: CleanCET CS Coordinator

Target time: 7-8 minutes

### Slide 6 - Design Methodology: Three Layers

Good morning. I am Aman Fayad, CS Coordinator of CleanCET.

I will explain the design methodology of the project.

The application is organized into three main layers:

1. Presentation layer
2. Domain and data access layer
3. Persistence layer

The **presentation layer** is the Swing GUI. This contains the main window, form inputs, buttons and table. The class responsible for this is `WasteManagementGUI`.

The **domain and data access layer** contains the OOP model and DAO logic. The model includes `Waste`, `OrganicWaste`, `RecyclableWaste` and `HazardousWaste`. The DAO class is `WasteManager`, which performs database operations.

The **persistence layer** is MySQL. The database name is `waste_management_db`, and the table name is `waste`.

The most important flow is shown at the bottom of the slide:

Request -> domain object -> PreparedStatement -> MySQL -> ResultSet -> JTable

This means the user action begins in the GUI, becomes a Java object, goes through JDBC SQL, reaches MySQL, and then comes back as rows displayed in the table.

Possible sir question:
**Why use layers?**

Answer:
Layers separate responsibilities. GUI code handles user interaction, model code represents data, DAO code handles SQL, and MySQL stores records.

---

### Slide 7 - One Record's Journey

This slide explains what happens to one record from input to storage.

Step 1 is **Input**. The user enters details in the Swing form.

Step 2 is **Object**. Based on the selected waste type, the program creates a specific object. If the type is Organic, it creates `OrganicWaste`. If it is Recyclable, it creates `RecyclableWaste`. Otherwise, it creates `HazardousWaste`.

Step 3 is **DAO**. The object is passed to `WasteManager`.

Step 4 is **SQL**. `WasteManager` uses a `PreparedStatement`. PreparedStatement is important because it sends values safely into SQL placeholders.

Step 5 is **Store**. MySQL stores the record inside the `waste` table.

For reading data, the path is reversed:

SELECT query -> ResultSet -> List of Waste objects -> DefaultTableModel -> JTable

This is how database rows become visible table rows in the application.

Another important part is the selection listener. When the user selects a row in JTable, the values from that row are copied back into the form fields. That allows update, delete and mark collected operations to work on the selected record.

Possible sir question:
**What is ResultSet?**

Answer:
ResultSet is a JDBC object that stores the rows returned by a SELECT query. We loop through it using `while(resultSet.next())`.

---

### Slide 8 - Package Structure

This slide shows the package structure.

The project is organized into packages instead of putting all code in one file.

The `model` package contains:
- `Waste.java`
- `OrganicWaste.java`
- `RecyclableWaste.java`
- `HazardousWaste.java`

This package demonstrates the OOP hierarchy.

The `dao` package contains:
- `DatabaseManager.java`
- `WasteManager.java`

`DatabaseManager` handles the connection to MySQL and database initialization. `WasteManager` handles CRUD operations.

The `gui` package contains:
- `WasteManagementGUI.java`

This file builds the Swing interface, including form fields, table, buttons and event listeners.

The `main` package contains:
- `Main.java`

This is the starting point of the application.

We also have `lib`, which stores the MySQL Connector/J JAR, `dist`, which stores the runnable JAR, and `sql`, which stores the setup SQL file.

Possible sir question:
**Why do we use packages?**

Answer:
Packages organize related classes. They make the project easier to understand, maintain and explain.

---

### Slide 9 - OOP Design

This slide is one of the most important for the OOP viva.

The parent class is `Waste`. It is an abstract class.

It contains common fields:
- id
- description
- quantity
- location
- status

These fields are private. That demonstrates **encapsulation**. We access them through getters and setters.

`Waste` also has an abstract method:

`getWasteCategory()`

This demonstrates **abstraction**, because the parent class says that every waste must have a category, but it does not decide the category itself.

Then we have three child classes:
- `OrganicWaste`
- `RecyclableWaste`
- `HazardousWaste`

These classes extend `Waste`, so this demonstrates **inheritance**.

Each child class overrides `getWasteCategory()`. OrganicWaste returns "Organic", RecyclableWaste returns "Recyclable", and HazardousWaste returns "Hazardous".

This demonstrates **polymorphism**, because the same method name behaves differently depending on the object.

Possible sir question:
**Where exactly is polymorphism?**

Answer:
Polymorphism is in `getWasteCategory()`. The GUI and DAO can call the same method on a `Waste` reference, but the output depends on whether the object is OrganicWaste, RecyclableWaste or HazardousWaste.

---

### Slide 10 - Database Model

This slide explains the MySQL table.

The database name is:

`waste_management_db`

The table name is:

`waste`

The table has six fields:

1. `id`
2. `waste_type`
3. `description`
4. `quantity`
5. `location`
6. `status`

`id` is an integer primary key with auto-increment. This means MySQL automatically gives every record a unique ID.

`waste_type` stores Organic, Recyclable or Hazardous.

`description` stores the description typed by the user.

`quantity` is a double, so decimal values like 12.5 are allowed.

`location` stores where the waste is found.

`status` stores Pending or Collected.

The sample query shown is:

`SELECT * FROM waste WHERE status = 'Collected';`

This would show all collected waste records.

One limitation mentioned on the slide is that there is no `collection_date` field currently. If we add that later, we can track exactly when the waste was collected.

Possible sir question:
**Why is id auto-increment?**

Answer:
Because the user should not manually enter IDs. MySQL generates unique IDs automatically, avoiding duplicate primary keys.

---

### Slide 11 - Implementation Setup

This slide lists what is needed to run the application.

The software requirements are:

- Java JDK
- Java Swing and AWT
- MySQL Server
- JDBC API
- MySQL Connector/J
- Build and run scripts

Java JDK is needed to compile and run the application. Swing and AWT are part of Java and are used for the desktop interface.

MySQL Server stores the data.

JDBC is the Java API that allows the Java program to connect to the database.

MySQL Connector/J is the driver that allows JDBC to talk specifically to MySQL.

The build scripts are:

- `build.sh` for macOS/Linux
- `build.bat` for Windows

The run scripts are:

- `run.sh`
- `run.bat`

We do not claim a specific minimum CPU or RAM requirement because this is a lightweight desktop application. A normal laptop that can run Java and MySQL is enough.

Handover:
Now Nivedya will explain the implementation results, the Swing interface, and how CRUD operations work in practice.

Possible sir question:
**What is the connector JAR?**

Answer:
It is the MySQL Connector/J file. Java uses it as a JDBC driver to communicate with MySQL.

---

## Nivedya Sunil - Slides 12 to 16

Role introduction: CleanCET CS Representative

Target time: 6-7 minutes

### Slide 12 - Desktop Interface

Good morning. I am Nivedya Sunil, CS Representative of CleanCET.

I will explain the implementation results and how the user interacts with the system.

The application opens as a single Java Swing window. The main class for the interface is `WasteManagementGUI`, which extends `JFrame`.

The interface has three main parts:

1. Form section
2. Table section
3. Action button section

The form section uses `JTextField` and `JComboBox`.

`JTextField` is used for inputs like description, quantity, location and search.

`JComboBox` is used for dropdowns like waste type and status.

The table section uses `JTable` inside a `JScrollPane`. JTable displays the records in rows and columns. JScrollPane allows scrolling if there are many records.

The action buttons are created using `JButton`. Each button has an event listener.

For example:

`addButton.addActionListener(event -> addWaste());`

This means when the Add button is clicked, the `addWaste()` method runs.

Possible sir question:
**What is JFrame?**

Answer:
JFrame is the main window class in Java Swing. Our GUI class extends JFrame, so it becomes the application window.

---

### Slide 13 - Create And Read

This slide explains the Add and View functions.

For adding a record, the user enters details into the form. Then the program checks the selected waste type and creates the matching object.

For example, if the type is Organic, it creates:

`new OrganicWaste(...)`

Then the GUI calls:

`wasteManager.addWaste(waste)`

Inside `WasteManager`, an INSERT query runs:

`INSERT INTO waste (...) VALUES (?, ?, ?, ?, ?)`

The question marks are placeholders. The actual values are filled using methods like:

`statement.setString()`

`statement.setDouble()`

This is how PreparedStatement works.

For reading records, `getAllWaste()` runs:

`SELECT * FROM waste ORDER BY id`

Then the result is stored in a `ResultSet`. Each row is converted into a Waste object and added to a `List<Waste>`. Finally, the GUI uses `DefaultTableModel` to display the list in the JTable.

Possible sir question:
**Why use DefaultTableModel?**

Answer:
DefaultTableModel manages the data displayed inside JTable. We add rows to the model, and JTable shows them.

---

### Slide 14 - Search

This slide explains the search feature.

The user enters a keyword in the search field and clicks Search.

The GUI calls:

`searchWaste()`

Then `WasteManager.searchWaste(keyword)` runs a SELECT query using LIKE.

The query searches across:

- description
- location
- waste_type
- status

The SQL logic is:

`WHERE description LIKE ? OR location LIKE ? OR waste_type LIKE ? OR status LIKE ?`

The keyword is changed into:

`%keyword%`

The percent signs mean that the keyword can appear anywhere in the text.

For example, searching "Lab" can match "Lab Room 2". Searching "Organic" can match the waste type Organic.

Again, PreparedStatement is used here, so the search value is passed safely.

Possible sir question:
**What does LIKE do in SQL?**

Answer:
LIKE is used for pattern matching. With `%keyword%`, SQL searches for records where the keyword appears anywhere inside the field.

---

### Slide 15 - Update, Delete And Mark Collected

This slide explains three important operations.

First is **Update**.

The user selects a row in the JTable. The selection listener copies the row values into the form fields. Then the user edits the values and clicks Update.

The SQL query is:

`UPDATE waste SET ... WHERE id = ?`

The `WHERE id = ?` part is very important because it ensures only the selected record is updated.

Second is **Delete**.

The user selects a row and clicks Delete. The program reads the selected ID and runs:

`DELETE FROM waste WHERE id = ?`

Third is **Mark Collected**.

This is a shortcut action. It updates only the status:

`UPDATE waste SET status = 'Collected' WHERE id = ?`

In all three operations, the ID is the key. That is why the ID column is important.

After every operation, the application calls `loadAllWaste()` to refresh the visible table.

Possible sir question:
**Why do we use WHERE id = ?**

Answer:
Because ID uniquely identifies one row. Without WHERE, the query may update or delete many records by mistake.

---

### Slide 16 - Run And Verify

This slide shows how to run the project and verify the database.

On macOS or Linux:

`./build.sh`

compiles the Java files and creates the JAR.

`./run.sh`

runs the application.

On Windows:

`build.bat`

and

`run.bat`

do the same thing.

The project also includes a packaged JAR inside the `dist` folder.

For database verification, we can use MySQL commands:

`USE waste_management_db;`

`SHOW TABLES;`

`DESCRIBE waste;`

`SELECT * FROM waste;`

These commands prove that the same records shown in the GUI are actually stored in the database.

Handover:
Now Ayshwaria will explain testing evidence, limitations, contributions, conclusion and future enhancements.

Possible sir question:
**How do you prove data is persistent?**

Answer:
Add a record in the GUI, close the app, reopen it, and run `SELECT * FROM waste;`. The record remains in MySQL.

---

## Ayshwaria Gopakumar - Slides 17 to 20

Role introduction: CleanCET CS Coordinator

Target time: 5-6 minutes

### Slide 17 - Evidence, Testing And Limitations

Good morning. I am Ayshwaria Gopakumar, CS Coordinator of CleanCET.

This slide summarizes the implemented features and the evidence from the source code.

Create and view are implemented through:

`addWaste()` and `getAllWaste()`

Search is implemented through:

`searchWaste()` and SQL LIKE.

Update and delete are implemented through:

`updateWaste()` and `deleteWaste()`

Collection status is implemented through:

`markAsCollected(id)`

Input and SQL errors are handled using:

`NumberFormatException` and `SQLException`

For example, if the user types letters in the quantity field, Java cannot convert that into a number. That is handled using NumberFormatException.

If MySQL is not running or the password is wrong, SQLException is caught and an error dialog is shown.

Limitations:

1. There is no delete confirmation dialog yet.
2. There is no login system or role-based access.
3. There is no export to PDF or Excel.
4. There is no collection date or timestamp field.
5. Database work runs directly from GUI event methods, so for a bigger system, background tasks would be better.

These limitations are not failures. They are realistic next steps for future improvement.

Possible sir question:
**What exception handling is used?**

Answer:
NumberFormatException handles invalid number input, and SQLException handles database errors.

---

### Slide 18 - Individual Contributions

This slide lists individual contributions.

As a team, we divided the project according to the major parts of the system:

Joseph worked on project framing, presentation flow, GitHub/Firebase organization, and helped connect the Clean CET context to the project idea.

Aman focused on design methodology, architecture explanation, package structure and database/OOP understanding.

Nivedya focused on implementation results, GUI feature explanation, CRUD flow and run verification.

Ayshwaria focused on testing evidence, limitations, contribution explanation, and conclusion.

Nida supported documentation, review and presentation preparation.

The project was developed as a team learning exercise, and every member understood the full flow: GUI, object creation, DAO, SQL and MySQL persistence.

Possible sir question:
**Did only one person code everything?**

Answer:
The implementation was integrated as one project, but the team divided responsibilities across design, code understanding, testing, documentation, presentation and viva preparation. Every member studied the full code flow.

---

### Slide 19 or 20 - Conclusion

This final slide concludes the project.

Our Waste Management System is a complete OOP learning system because it combines:

- Object-based waste categories
- Java Swing desktop interface
- CRUD and search operations
- JDBC and MySQL persistence
- Clear package separation

The application successfully demonstrates a full end-to-end workflow:

User enters data -> Java creates an object -> DAO sends SQL -> MySQL stores data -> GUI displays updated table

This is why the project is useful for an OOP mini project. It is not overly complex, but it clearly shows how object-oriented design connects to a real application.

Future enhancements include:

1. **IoT bin sensor integration**

In the future, smart bins could automatically send fill-level data to the system.

2. **Role-based access control**

Admin users could edit/delete records, while normal users could only view records.

3. **PDF or Excel export**

Clean CET or campus authorities could export monthly waste reports.

4. **Background database tasks**

For larger datasets, database operations can run in background threads so the GUI remains responsive.

5. **Collection date field**

Adding a timestamp would make reporting and auditing stronger.

Closing statement:

To conclude, this project helped us connect our Clean CET experience with Java OOP concepts. It gave us a practical way to understand classes, objects, inheritance, abstraction, polymorphism, Swing event handling, JDBC connectivity and MySQL persistence.

Thank you. We are ready for questions.

Possible sir question:
**What is the best future enhancement?**

Answer:
The most practical next enhancement is adding collection date and report export, because it would make the system useful for monthly campus cleanliness records.

---

## Rapid Fire Viva Preparation

### SQL And MySQL

**Q: What is your database name?**

A: `waste_management_db`

**Q: What is your table name?**

A: `waste`

**Q: What are the columns?**

A: id, waste_type, description, quantity, location and status.

**Q: Which column is primary key?**

A: id.

**Q: Why use AUTO_INCREMENT?**

A: So MySQL automatically generates unique IDs.

**Q: Which SQL commands are used?**

A: INSERT, SELECT, UPDATE and DELETE.

### JDBC

**Q: What is JDBC?**

A: JDBC stands for Java Database Connectivity. It allows Java programs to connect to databases and run SQL queries.

**Q: What is DriverManager?**

A: It creates the database connection using the JDBC URL, username and password.

**Q: What is PreparedStatement?**

A: It is a JDBC object that runs SQL queries with placeholders.

**Q: Why PreparedStatement instead of Statement for user input?**

A: PreparedStatement is safer and cleaner because values are passed separately.

### Swing

**Q: What is Swing?**

A: Swing is Java's GUI toolkit for desktop applications.

**Q: What is JFrame?**

A: JFrame is the main window.

**Q: What is JTable?**

A: JTable displays records in rows and columns.

**Q: What is addActionListener?**

A: It handles button click events.

### OOP

**Q: Where is abstraction?**

A: `Waste` is an abstract class with abstract method `getWasteCategory()`.

**Q: Where is inheritance?**

A: OrganicWaste, RecyclableWaste and HazardousWaste extend Waste.

**Q: Where is encapsulation?**

A: Waste has private fields and public getters/setters.

**Q: Where is polymorphism?**

A: `getWasteCategory()` is overridden in each child class.

### Application Flow

**Q: What happens when Add is clicked?**

A: GUI reads form input, creates a Waste subclass object, calls `WasteManager.addWaste()`, runs INSERT query, then refreshes JTable.

**Q: What happens when a table row is selected?**

A: The selection listener copies row values into the form fields.

**Q: What happens when MySQL is not running?**

A: SQLException is caught and an error dialog is shown.

