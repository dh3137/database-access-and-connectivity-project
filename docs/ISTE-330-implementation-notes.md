# ISTE-330 Implementation Notes

This project intentionally stays plain Java, JDBC, and MySQL. Course topics are implemented only where they fit the current app without changing the architecture.

## Implemented in Code

- **Transactions:** `SalesDatabase.recordSale()` uses one JDBC `Connection` and one transaction for recording a sale, marking the vehicle as `Sold`, and converting active reservations for that vehicle. If any step fails, the transaction rolls back.
- **SQL injection protection:** DAO writes and reads use `PreparedStatement` parameters. The audit log limit query also uses a parameter instead of concatenating the limit value into SQL.
- **Authorization/RBAC:** The app uses simple role-based authorization from `Users.role` with centralized helpers in `Main.java` for admin and customer checks.
- **Auditing:** Vehicle create/update actions use `VehicleChangeLog`; sale recording also logs the vehicle status change to `Sold`. General actions such as login, sale recorded, vehicle deleted, and enquiry status changed use `ActionLog`.
- **Data validation:** Server-side checks validate vehicle year, positive price, nonnegative mileage, valid status values, VIN length, required sale IDs, positive sale price, and email format. Database `CHECK` constraints enforce key numeric rules as final protection.
- **Manual object-relational mapping:** The app manually maps relational rows into Java objects, such as `CarDatabase.mapCar()` and `UserDatabase.mapUser()`, instead of using an ORM.

## Intentionally Documented Instead of Implemented

- **Connection pooling/DataSource:** The app still uses a small `MySQLDatabase` JDBC helper. A production version could replace `DriverManager.getConnection()` with a `DataSource`, such as HikariCP, configured from `db.properties`. That would require a new Maven dependency and connection lifecycle testing, so it was not added here.
- **PBKDF2 password hashing:** Current sample users store SHA-256 hashes in `Users.password VARCHAR(64)`. PBKDF2 with per-user salts should use a longer hash format or extra salt column, plus a migration for existing sample users. To avoid breaking login/register, this is left as a documented migration.
- **Permission-table RBAC:** The database has `Roles`, `Permissions`, `EmployeeRoles`, and `RolePermissions`, but the current UI only needs coarse roles from `Users.role`. A full permission-checking service would be larger than the current requirements.
- **ETL:** CSV import would fit as an isolated admin utility for importing vehicles or customers, but the current app does not have an import screen or workflow.
- **RowSet:** The app uses normal connected JDBC `ResultSet` reads through `MySQLDatabase.getData()`. A disconnected `RowSet` is not needed for the current request/response flow.
