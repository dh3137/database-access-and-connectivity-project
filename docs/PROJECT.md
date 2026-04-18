# AutoPrime — Project Overview

## What is this project?

AutoPrime is a car dealership web application built for the ISTE 330 (Database Access and Connectivity) course at RIT. It demonstrates how a Java backend connects to a MySQL database and serves data to a browser in real time.

The app has three user roles:
- **ADMIN** — can view, add, edit, and delete cars. Sees the activity log.
- **EMPLOYEE** — can view the car list and car details. Read-only.
- **CUSTOMER** — same as employee. Read-only.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| HTTP Server | `com.sun.net.httpserver.HttpServer` (built into the JDK — no Tomcat, no install needed) |
| Database | MySQL (running locally on port 3306) |
| DB Connection | Plain JDBC via `DriverManager.getConnection()` |
| Build Tool | Maven (`mvn compile exec:java` to run) |
| Frontend | Plain HTML + CSS + vanilla JavaScript (`fetch` API) |
| Auth | Session tokens (UUID) stored in server memory, sent to browser as an `HttpOnly` cookie |
| Password hashing | SHA-256 via `java.security.MessageDigest` |

No frameworks. No Spring. No Tomcat. No npm. Everything runs with one Maven command.

---

## How to Run

```bash
mvn compile exec:java
```

Then open `http://localhost:8080` in your browser.

---

## Accounts

All passwords are `password123`.

| Username | Role | Name |
|---|---|---|
| ivan | ADMIN | Ivan Karlo |
| danis | ADMIN | Danis Harmandić |
| jurica | ADMIN | Jurica Jamić |
| tomo | ADMIN | Tomislav Tešija |
| branko | ADMIN | Branko Mihaljević |
| employee1 | EMPLOYEE | Jane Employee |
| customer1 | CUSTOMER | John Customer |

---

## How Storage Works (Important)

This is the part that confuses everyone at first.

### There are TWO separate things: the SQL files and the live database.

```
database/schema.sql      ← defines the table structure (run once to create tables)
database/sample-data.sql ← starting data (run to seed/reset the database)
MySQL (live)             ← where the app actually reads and writes at runtime
```

They are **not linked**. The SQL files do not update automatically when you use the app. The app talks directly to MySQL.

### What happens when you add a Mercedes in the dashboard?

1. You click "Add Car" and fill in the form.
2. The browser sends a `POST /api/cars` request to the Java server.
3. `Main.java` receives it, calls `CarService.addCar()`, which calls `CarDao.addCar()`.
4. `CarDao` opens a JDBC connection to MySQL and runs:
   ```sql
   INSERT INTO cars (make, model, year, price) VALUES (?, ?, ?, ?)
   ```
5. MySQL stores the row. The Java server sends back `{"ok":true}`.
6. The dashboard reloads the table from `GET /api/cars`, which runs `SELECT * FROM cars`.
7. The Mercedes appears.

**The Mercedes is only in MySQL.** It is NOT written to `sample-data.sql`. That file is unchanged.

### So why does the Mercedes still show after I restart the server?

Because the server restarts, but MySQL keeps running. The data lives in MySQL, not in the Java process. The server just reconnects and reads the same rows.

### What if I want to reset everything back to the original 4 cars?

Run this in your terminal:

```bash
/usr/local/mysql/bin/mysql -u root -p'<your password>' car_dealership -e "DELETE FROM cars;"
/usr/local/mysql/bin/mysql -u root -p'<your password>' car_dealership < database/sample-data.sql
```

That wipes the cars table and reloads the seed data from the file.

### What if I add a car to `sample-data.sql`?

Edit the file, add a line like:
```sql
('Mercedes', 'C200', 2023, 42000.00),
```

Then run:
```bash
/usr/local/mysql/bin/mysql -u root -p'<your password>' car_dealership < database/sample-data.sql
```

Because the file uses `INSERT IGNORE`, it skips rows that already exist and only adds new ones. Restart the server (or just refresh the dashboard) and the new car will appear.

---

## Where Each File Lives and What It Does

```
database/
  schema.sql              — CREATE TABLE statements. Run once when setting up.
  sample-data.sql         — INSERT IGNORE seed data. Re-run anytime to add new rows.

src/main/java/com/cardealership/
  Main.java               — The entire HTTP server. All API routes live here.
  util/DatabaseConnection.java  — Opens JDBC connections to MySQL. Credentials live here.
  model/Car.java          — Plain Java object representing a car row.
  model/User.java         — Plain Java object representing a user row.
  dao/CarDao.java         — All SQL for cars: SELECT, INSERT, UPDATE, DELETE.
  dao/UserDao.java        — SQL for users: find by username.
  dao/ActionLogDao.java   — SQL for the action_log table: insert and read entries.
  service/CarService.java — Business logic for cars (validation before hitting the DAO).
  service/UserService.java — Authenticates users: hashes the password and compares to DB.

src/main/webapp/
  login.html              — Login form. POSTs to /api/login.
  dashboard.html          — Admin view. Shows car table + Add/Edit/Delete + activity log.
  cars.html               — Employee/customer view. Read-only car list.
  vehicle-details.html    — Detail page for a single car. Works for all roles.
  css/style.css           — All shared styles.
```

---

## API Endpoints

| Method | Path | Who | What |
|---|---|---|---|
| POST | `/api/login` | Anyone | Authenticate, set session cookie |
| GET | `/api/logout` | Logged in | Destroy session, redirect to login |
| GET | `/api/me` | Logged in | Returns current user's info (id, username, role, fullName) |
| GET | `/api/cars` | Logged in | Returns all cars as JSON array |
| GET | `/api/cars/{id}` | Logged in | Returns one car as JSON |
| POST | `/api/cars` | ADMIN only | Add a new car |
| PUT | `/api/cars/{id}` | ADMIN only | Edit an existing car |
| DELETE | `/api/cars/{id}` | ADMIN only | Delete a car |
| GET | `/api/logs` | ADMIN only | Returns last 50 action log entries |

---

## MySQL Tables

### `cars`
| Column | Type | Notes |
|---|---|---|
| id | INT AUTO_INCREMENT | Primary key |
| make | VARCHAR(50) | e.g. Toyota |
| model | VARCHAR(50) | e.g. Corolla |
| year | INT | e.g. 2023 |
| price | DECIMAL(10,2) | e.g. 25000.00 |

### `users`
| Column | Type | Notes |
|---|---|---|
| id | INT AUTO_INCREMENT | Primary key |
| username | VARCHAR(50) UNIQUE | Login username |
| password | VARCHAR(255) | SHA-256 hex of the plain password |
| role | ENUM | ADMIN, EMPLOYEE, or CUSTOMER |
| full_name | VARCHAR(100) | Display name shown in sidebar |

### `action_log`
| Column | Type | Notes |
|---|---|---|
| id | INT AUTO_INCREMENT | Primary key |
| username | VARCHAR(50) | Who did the action |
| action | VARCHAR(100) | LOGIN, ADD_CAR, EDIT_CAR, DELETE_CAR |
| detail | VARCHAR(255) | Human-readable description |
| created_at | TIMESTAMP | Auto-set to current time |

---

## Planned Features

Features planned for the next phase of development. Ordered by priority.

### 1. Car Status (Available / Sold / Reserved)
Add a `status` column to the `cars` table: `ENUM('AVAILABLE', 'SOLD', 'RESERVED')`.  
- Admins can change status from the dashboard (dropdown in the Edit modal).  
- Car list highlights status with a colored badge (green = available, red = sold, yellow = reserved).  
- Customers and employees see status on the detail page.  
- Prevents selling a car that's already been sold.

### 2. Search and Filter Cars
A live search bar and filter panel above the car table that lets any user filter by:
- Make (text search)
- Year range (from / to)
- Price range (min / max)
- Status (if feature #1 is implemented)

Filtering runs in the browser against the already-loaded list — no extra DB query needed.

### 3. Inventory Statistics Panel
An at-a-glance summary for admins at the top of the dashboard:
- Total cars in inventory
- Total inventory value (sum of all prices)
- Breakdown by status (X available, Y sold, Z reserved)
- Breakdown by make (5 Toyota, 3 BMW, …)

All calculated with a single SQL `GROUP BY` query and rendered as simple stat cards.

### 4. User Management (Admin Only)
A dedicated page (`/users.html`) that lets admins:
- View all users (username, full name, role)
- Add a new user (sets a temporary password)
- Change a user's role
- Delete a user

New API endpoints: `GET /api/users`, `POST /api/users`, `PUT /api/users/{id}`, `DELETE /api/users/{id}` — all ADMIN only.

### 5. Change Password
A form available to every logged-in user on a profile page (`/profile.html`):
- Enter current password, new password, confirm new password
- Server validates the current password, then stores the SHA-256 of the new one
- Activity log records a `CHANGE_PASSWORD` event

### 6. Sortable Table Columns
Click any column header in the car table to sort ascending/descending by that column.  
Sort state is tracked in a JS variable and applied to the in-memory array — no extra DB query.

### 7. Pagination
When the inventory grows large, show 20 cars per page with Previous / Next controls.  
Page number stored in a JS variable; slicing the array in the browser (no backend changes needed).

### 8. Export to CSV
A button in the admin dashboard that downloads the current car list as a `.csv` file.  
Generated entirely in the browser from the already-loaded JSON — no new endpoint needed.  
Useful for importing into Excel or Google Sheets for presentations.

### 9. Customer Inquiry Form
A contact form visible to customers on the car detail page:
- Fields: name, email, message
- Stores the inquiry in a new `inquiries` table (car_id, name, email, message, created_at)
- Admins see pending inquiries in a tab on the dashboard
- New API: `POST /api/inquiries` (any role), `GET /api/inquiries` (ADMIN only)

### 10. Improved Design and Responsive Layout
A visual overhaul using a consistent design system:
- Proper color palette, typography, and spacing tokens in CSS custom properties
- Responsive layout — works on tablet and mobile (flex/grid, no fixed widths)
- Designed sidebar with collapse support on small screens
- Smooth transitions on modal open/close, row hover states
- Status badges, empty-state illustrations, loading skeletons
