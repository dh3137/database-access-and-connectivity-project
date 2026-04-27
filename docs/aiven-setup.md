# Aiven Setup Notes

This project can connect to a remote MySQL-compatible database through `db.properties`.

## What changed

- `db.properties` entries now support an optional `.params` field.
- Those params are appended to the JDBC connection string.
- This is useful for managed databases such as Aiven, where SSL or provider-specific options are often required.

## Local example

```properties
ivankarlo.host=localhost
ivankarlo.port=3306
ivankarlo.database=car_dealership
ivankarlo.username=root
ivankarlo.password=YOUR_LOCAL_DB_PASSWORD
ivankarlo.params=
```

## Aiven example

```properties
ivankarlo.host=YOUR_AIVEN_HOST
ivankarlo.port=YOUR_AIVEN_PORT
ivankarlo.database=defaultdb
ivankarlo.username=avnadmin
ivankarlo.password=YOUR_AIVEN_PASSWORD
ivankarlo.params=sslMode=REQUIRED
```

## Suggested next steps

1. Create the MySQL service in Aiven.
2. Copy the host, port, database, username, and password into `db.properties`.
3. Check Aiven's JDBC/SSL instructions and update `.params` if they require more than `sslMode=REQUIRED`.
4. Run `database/schema.sql` against the new database.
5. Optionally run `database/sample-data.sql` if you want demo data there too.

## Important

- Keep `db.properties` local and out of git.
- Do not commit live Aiven passwords.
- If the connection fails, the most likely cause will be missing SSL-related JDBC params.
