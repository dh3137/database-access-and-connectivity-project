# AutoPrime — Project Overview

## What is this project?

AutoPrime is a car dealership web application built for the ISTE 330 (Database Access and Connectivity) course at RIT. It demonstrates how a Java backend connects to a MySQL database and serves data to a browser in real time.

The app has three user roles:
- **ADMIN** — can view, add, edit, and delete vehicles. Sees the vehicle change log.
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

### 1. Set up the database (first time only)

```bash
/usr/local/mysql/bin/mysql -u root -p < database/schema.sql
/usr/local/mysql/bin/mysql -u root -p car_dealership < database/sample-data.sql
```

### 2. Configure your credentials

Copy `db.properties.example` to `db.properties` and fill in your OS username and MySQL password:

```properties
YOUR_OS_USERNAME.host=localhost
YOUR_OS_USERNAME.port=3306
YOUR_OS_USERNAME.database=car_dealership
YOUR_OS_USERNAME.username=root
YOUR_OS_USERNAME.password=your_mysql_password
```

The key prefix must match `System.getProperty("user.name")` — i.e. whatever your Mac/laptop username is (e.g. `ivankarlo`).

### 3. Start the server

```bash
mvn compile exec:java
```

Then open `http://localhost:8080` in your browser.

### 4. Reset the database to sample data

```bash
/usr/local/mysql/bin/mysql -u root -p car_dealership < database/sample-data.sql
```

---

## Accounts

All passwords are `password123`.

| Username | Role | Linked to |
|---|---|---|
| ivan | ADMIN | Employee: Ivan Karlo |
| danis | ADMIN | Employee: Danis Harmandić |
| jurica | ADMIN | Employee: Jurica Jamić |
| tomo | ADMIN | Employee: Tomislav Tešija |
| branko | EMPLOYEE | Employee: Branko Mihaljević |
| john | CUSTOMER | Customer: John Customer |
| ana | CUSTOMER | Customer: Ana Horvat |
| marko | CUSTOMER | Customer: Marko Kovačić |

---

## How Storage Works

### There are TWO separate things: the SQL files and the live database.

```
database/schema.sql      ← defines the table structure (run once to create tables)
database/sample-data.sql ← starting data (run to seed/reset the database)
MySQL (live)             ← where the app actually reads and writes at runtime
```

They are **not linked**. The SQL files do not update automatically when you use the app. The app talks directly to MySQL.

### What happens when you add a car in the dashboard?

1. Browser sends `POST /api/cars` with a JSON body including `modelId`, `year`, `price`, `vin`, etc.
2. `Main.java` parses it and calls `CarDatabase.saveCar()`.
3. `CarDatabase` runs `INSERT INTO Vehicles (model_id, year, price, ...) VALUES (?, ?, ?, ...)`.
4. MySQL stores the row. Server sends back `{"ok":true}`.
5. Dashboard reloads from `GET /api/cars`, which does a full JOIN query across Vehicles + Models + Manufacturers + VehicleImages.

**The car is only in MySQL.** It is NOT written to `sample-data.sql`.

---

## Where Each File Lives and What It Does

```
database/
  schema.sql              — 16 CREATE TABLE statements. Run once when setting up.
  sample-data.sql         — Seed data for all tables. Re-run to reset.

src/main/java/com/cardealership/
  Main.java               — HTTP server. All API routes, session handling, JSON serialization.
  DLException.java        — Custom exception wrapping JDBC errors with context.
  util/MySQLDatabase.java — JDBC connection pool helper (getData / setData).
  model/Car.java          — Flat Java object for a vehicle + make/model strings from JOINs.
  model/User.java         — Java object for a Users row (id, username, role, empId, customerId).
  database/CarDatabase.java     — SQL for vehicles: SELECT with JOINs, INSERT, UPDATE, DELETE.
  database/UserDatabase.java    — SQL for users: authenticate (username+SHA256), getUserByUsername, saveUser.
  database/ActionLogDatabase.java — SQL for VehicleChangeLog: save and read change entries.

src/main/webapp/
  index.html              — Public landing page. Hero video, classic navbar/footer, rounded showcase cards, and customer inventory view.
  login.html              — Login form. POSTs to /api/login.
  dashboard.html          — Admin view. Shows full car table (including sold/reserved), Add/Edit/Delete, and change log.
  vehicle-details.html    — Public car detail page. Lazy-fetches Wikipedia image if no imageUrl and adjusts CTAs for reserved/sold vehicles.
  css/style.css           — All shared styles.

db.properties             — Per-developer MySQL credentials (gitignored).
db.properties.example     — Template showing how to set up db.properties.
```

---

## API Endpoints

| Method | Path | Auth | What |
|---|---|---|---|
| POST | `/api/login` | Anyone | Authenticate, set session cookie |
| GET | `/api/logout` | Logged in | Destroy session, redirect to login |
| GET | `/api/me` | Logged in | Returns `{id, username, role, empId, customerId}` |
| GET | `/api/cars` | Anyone | All vehicles as JSON array (JOINed with make/model/image, status normalized to `AVAILABLE` / `RESERVED` / `SOLD`) |
| GET | `/api/cars/{id}` | Anyone | Single vehicle as JSON with normalized uppercase status |
| POST | `/api/cars` | ADMIN | Add a new vehicle (requires `modelId`) |
| PUT | `/api/cars/{id}` | ADMIN | Edit an existing vehicle |
| DELETE | `/api/cars/{id}` | ADMIN | Delete a vehicle |
| GET | `/api/logs` | ADMIN | Last 50 VehicleChangeLog entries |
| GET | `/api/carimage` | Anyone | Wikipedia image URL for `?make=&model=&year=` |

---

## Database Schema (16 Tables)

### Vehicle Catalog

| Table | Purpose |
|---|---|
| `Manufacturers` | Brand info: name, country, founded_year, website_url |
| `Models` | Model info per manufacturer: model_name, body_type, segment |
| `Vehicles` | Actual inventory: model_id FK, year, color, mileage, price, vin, status, description |
| `VehicleImages` | Images per vehicle: image_url, is_primary flag |

A "car" in the app = `Vehicles` JOIN `Models` JOIN `Manufacturers` LEFT JOIN `VehicleImages (is_primary=TRUE)`.

### People

| Table | Purpose |
|---|---|
| `Customers` | first_name, last_name, email, phone, address |
| `Employees` | first_name, last_name, email, phone, hire_date, is_active |

### Authentication

| Table | Purpose |
|---|---|
| `Users` | username, password (SHA-256), role ENUM(ADMIN/EMPLOYEE/CUSTOMER), nullable FK to emp_id OR customer_id |

### Access Control

| Table | Purpose |
|---|---|
| `Roles` | Role definitions (Admin, Salesperson, Employee) |
| `Permissions` | Permission definitions (Add Vehicle, Record Sale, etc.) |
| `EmployeeRoles` | Many-to-many: which employee has which role |
| `RolePermissions` | Many-to-many: which role has which permission |

### Transactions

| Table | Purpose |
|---|---|
| `Sales` | vehicle_id, customer_id, emp_id, sale_price, payment_method |
| `Reservations` | vehicle_id, customer_id, emp_id, expiry_date, status, deposit_amount |
| `TestDrives` | vehicle_id, customer_id, emp_id, scheduled_date, status |

### Audit & Maintenance

| Table | Purpose |
|---|---|
| `MaintenanceHistory` | Per-vehicle service records: service_type, cost, performed_by |
| `VehicleChangeLog` | Audit trail: who changed what on which vehicle and when |

---

## Key Design Notes

- `Car.java` is a **flat read model** — `make` and `model` strings are populated by JOINs and are read-only. To write a vehicle, you must supply `modelId` (FK to Models).
- `User.java` links to either `Employees` (for ADMIN/EMPLOYEE roles) or `Customers` (for CUSTOMER role) via nullable FKs. Never both.
- Vehicle CRUD operations are logged to `VehicleChangeLog` with the employee's `emp_id`. Login events are only logged to the console (not to the DB).
- The database stores status as the exact ENUM strings `Available`, `Reserved`, `Sold`, but the API normalizes them to uppercase (`AVAILABLE`, `RESERVED`, `SOLD`) for the frontend.
- Customer-facing inventory surfaces (`index.html`, `cars.html`) show `Available` and `Reserved` vehicles; `Sold` vehicles remain visible in the admin dashboard only.
- Reserved vehicles stay visible to customers and use seller-contact messaging rather than being treated as fully unavailable.

---

## Planned Features & Improvements

### Implemented
- Scroll-reveal animations (IntersectionObserver + CSS, re-animates on every scroll) — `js/reveal.js`
- Light / dark mode toggle — `js/theme.js`, persists to `localStorage`, respects system preference
- Unified filter bar on `index.html` and `cars.html` (search, sort, status, type, price range, year range)

### Lead Capture / Buying Flow
- **Contact form modal** — name, email, phone, message pre-filled with car name. `POST /api/enquiry` stores to DB. Admin sees enquiries in dashboard.
- **"Reserve this car" button** — flips vehicle status AVAILABLE → RESERVED directly from the detail page. Admin can confirm or release from dashboard.
- **Appointment scheduler** — date/time picker writes to the existing `TestDrives` table. Admin sees upcoming viewings.
- **WhatsApp quick-contact** — `href="https://wa.me/..."` with pre-written message including the car name. Zero backend needed.

### Vehicle Details Page
- **Image gallery** — if a vehicle has multiple `VehicleImages` rows, render a horizontal strip below the hero; click to swap main image.
- **Similar vehicles strip** — 3 cards at the bottom filtered by same segment or ±20% price. Keeps users browsing.
- **Specs comparison** — pick a second car from a dropdown, see a side-by-side spec table.
- **Share button** — copies URL or opens native share sheet (`navigator.share`).
- **Loading skeletons** — shimmer placeholders while the car data fetches (already on cars.html, missing here).
- **360° / video slot** — placeholder section for a YouTube unlisted embed or turntable video.

### Inventory Page (cars.html)
- **Active filter chips** — pill badges below the bar showing active filters (e.g. `× Sport`, `× < $50k`). Click to remove individually.
- **Price range slider** — dual-handle slider replacing the two number inputs. More tactile, higher-end feel.
- **Lazy-load / pagination** — show 12 per page with "Load more" for when inventory grows.

### Homepage (index.html)
- **Stats bar** — animated counters: "47 vehicles · 12 makes · Est. 2024". Builds credibility fast.
- **Testimonials carousel** — pull team reviews from the Reviews table as a scrolling strip.
- **"Arriving soon" teaser** — blurred-out cards for manually flagged upcoming vehicles with an email-capture "Notify me" CTA.

### Cross-Cutting
- **Accessibility pass** — keyboard navigation, visible focus rings, ARIA labels on all interactive elements.
- **Loading skeletons on vehicle-details.html** — match the shimmer pattern already used on cars.html.
