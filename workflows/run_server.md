# Workflow: Run the Server

## Objective
Compile the Java source and start the HTTP server on port 8080.

## When to use
- Starting the app for development or demo
- After making Java changes that need to be tested

## Inputs needed
- MySQL must be running (the app connects on startup)
- Caller's OS username must have an entry in `db.properties`

## Steps

1. Check that MySQL is running:
   ```bash
   /usr/local/mysql/bin/mysqladmin -u root -p'<password>' ping
   ```
   Expected: `mysqld is alive`

2. Confirm the caller's OS username is in `db.properties`:
   ```bash
   grep "$(whoami)" db.properties
   ```
   If missing — add an entry: `<username>.db.url`, `<username>.db.user`, `<username>.db.password`.

3. Start the server:
   ```bash
   bash tools/run_server.sh
   ```
   Or directly: `mvn compile exec:java`

4. Confirm it's up — look for this line in the output:
   ```
   Server started on port 8080
   ```

5. Open `http://localhost:8080` in a browser.

## Edge Cases

- **Port 8080 already in use** — find and kill the process:
  ```bash
  lsof -ti:8080 | xargs kill -9
  ```
  Then retry.
- **DB connection refused** — MySQL isn't running. Start it via System Preferences → MySQL, or `sudo /usr/local/mysql/support-files/mysql.server start`.
- **`db.properties` key not found`** — `DatabaseConnection.java` throws at startup. Add the missing key for the current OS user.
- **Maven not found** — `mvn` not on PATH. Check `which mvn`; install via Homebrew: `brew install maven`.

## Output
Server running at `http://localhost:8080`. Logs written to `logs/`.
