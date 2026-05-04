# Architecture Notes

AutoPrime is intentionally a plain Java, JDBC, and MySQL application. It does not use Spring, Hibernate, JPA, or a full ORM.

## Request Flow

1. Browser pages in `src/main/webapp` call backend endpoints with `fetch()` or form posts.
2. `Main.java` receives HTTP requests through the JDK `HttpServer`.
3. `Main.java` performs session checks, role checks, parsing, validation, and JSON responses.
4. DAO classes in `src/main/java/com/cardealership/database` run SQL through `MySQLDatabase`.
5. `MySQLDatabase` opens JDBC connections and executes parameterized `PreparedStatement` queries.
6. MySQL stores the live application data.

## Main Layers

- `Main.java`: route handlers, session management, authorization, validation, JSON formatting.
- `database/*Database.java`: DAO classes for SQL reads and writes.
- `util/MySQLDatabase.java`: shared JDBC helper for connections, prepared statements, generated keys, and result rows.
- `model/Car.java` and `model/User.java`: simple Java objects used by the app.
- `database/schema.sql`: database structure.
- `database/sample-data.sql`: seed data for local testing and demos.

## Manual Object-Relational Mapping

The project uses manual object-relational mapping. SQL rows are converted into Java objects by explicit mapper methods, such as:

- `CarDatabase.mapCar(...)`
- `UserDatabase.mapUser(...)`

This keeps the project aligned with JDBC course goals and avoids a full ORM framework.

## Authorization

The app uses simple role-based authorization from `Users.role`:

- `ADMIN`: manage vehicles, view logs, view customers/employees, manage enquiries, record sales.
- `CUSTOMER`: browse inventory and submit enquiries.
- `EMPLOYEE`: read-only account role in the current UI.

The schema includes permission tables for course modeling, but the running app intentionally uses the simpler `Users.role` checks because that matches the current feature set.

## Transactions

Sale recording is the main transactional workflow. `SalesDatabase.recordSale()` uses one JDBC connection and one transaction to:

- verify the customer exists
- verify the employee is active
- mark the vehicle `Sold`
- insert the sale
- convert active reservations for that vehicle

If any step fails, the transaction rolls back.

## Auditing

The project uses two audit tables:

- `VehicleChangeLog`: vehicle-specific create/update/status audit entries.
- `ActionLog`: general actions such as login, sale recorded, vehicle deleted, and enquiry status changed.

This avoids forcing vehicle delete or enquiry events into a vehicle-specific table where the foreign keys do not fit cleanly.

## Security Notes

- SQL uses `PreparedStatement` parameters for user-controlled values.
- User-facing errors stay generic where database errors are involved.
- Detailed exceptions are written through `DLException` logging.
- App passwords currently use SHA-256 to preserve compatibility with the sample data. A PBKDF2 migration is documented in `docs/ISTE-330-implementation-notes.md`.
