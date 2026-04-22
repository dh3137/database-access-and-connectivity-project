# Workflow: Reset Database

## Objective
Wipe the `cars` table and reload the seed data from `database/sample-data.sql`. Used when the live data has gotten messy and you want a clean slate.

## When to use
- Before a demo and the DB has test junk in it
- After someone added bad data manually
- Any time someone asks to "reset the cars"

## Inputs needed
- MySQL root password (ask the user — never store it)
- Confirm the user actually wants to delete all current cars (this is destructive)

## Steps

1. Confirm with user before proceeding — this deletes ALL current cars.
2. Run `tools/reset_db.sh` passing the MySQL password:
   ```bash
   bash tools/reset_db.sh '<mysql_root_password>'
   ```
3. Verify success by checking row count:
   ```bash
   /usr/local/mysql/bin/mysql -u root -p'<password>' car_dealership -e "SELECT COUNT(*) FROM cars;"
   ```
4. Expected: matches the number of rows in `database/sample-data.sql`.

## Edge Cases

- **MySQL not at `/usr/local/mysql/bin/mysql`** — try `which mysql` to find it, then update `tools/reset_db.sh` with the correct path and document the machine it was found on.
- **Access denied** — wrong password. Ask the user again.
- **Table doesn't exist** — schema hasn't been run. Run `database/schema.sql` first:
  ```bash
  /usr/local/mysql/bin/mysql -u root -p'<password>' car_dealership < database/schema.sql
  ```
  Then retry this workflow.

## Output
`cars` table contains only the seed rows from `sample-data.sql`. Server does not need to restart — next `GET /api/cars` will reflect the reset.
