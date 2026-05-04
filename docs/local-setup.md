# Local Setup

Use this guide for a clean local MySQL setup.

## 1. Create and Seed the Database

```bash
/usr/local/mysql/bin/mysql -u root -p < database/schema.sql
/usr/local/mysql/bin/mysql -u root -p car_dealership < database/sample-data.sql
```

The first command creates the `car_dealership` database and all tables. The second command loads sample cars, users, customers, employees, sales, reservations, reviews, and audit rows.

## 2. Configure `db.properties`

Copy the example file:

```bash
cp db.properties.example db.properties
```

Find your OS username:

```bash
whoami
```

Add or edit the matching prefix in `db.properties`:

```properties
YOUR_OS_USERNAME.host=localhost
YOUR_OS_USERNAME.port=3306
YOUR_OS_USERNAME.database=car_dealership
YOUR_OS_USERNAME.username=root
YOUR_OS_USERNAME.password=YOUR_LOCAL_DB_PASSWORD
YOUR_OS_USERNAME.params=
```

`db.properties` is gitignored and should not be committed.

## 3. Start the App

```bash
mvn compile exec:java
```

Open:

```text
http://localhost:8080
```

## 4. Demo Accounts

All demo account passwords are `password123`.

| Username | Role |
|---|---|
| ivan | ADMIN |
| danis | ADMIN |
| jurica | ADMIN |
| tomo | ADMIN |
| branko | EMPLOYEE |
| john | CUSTOMER |
| ana | CUSTOMER |
| marko | CUSTOMER |

## 5. Reset Sample Data

For a full reset:

```bash
/usr/local/mysql/bin/mysql -u root -p < database/schema.sql
/usr/local/mysql/bin/mysql -u root -p car_dealership < database/sample-data.sql
```

## 6. Updating an Existing Database

If you already have a local database with data you want to keep, run the migration instead of resetting:

```bash
/usr/local/mysql/bin/mysql -u root -p car_dealership < database/migrations/001_add_integrity_and_action_log.sql
```

Run that migration only once. If you do not need to preserve local data, the full reset commands above are simpler.
