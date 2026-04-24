# Workflow: Add User

## Objective
Add a new user to the `users` table with a properly hashed password.

## When to use
- Onboarding a new team member as ADMIN
- Creating a test EMPLOYEE or CUSTOMER account
- Any time someone asks to add a login

## Inputs needed
- Username (must be unique, no spaces)
- Full name (display name shown in sidebar)
- Role: ADMIN, EMPLOYEE, or CUSTOMER
- Plain-text password (you will hash it — never store the plain version)
- MySQL root password

## Steps

1. Hash the plain password:
   ```bash
   python3 tools/hash_password.py '<plain_password>'
   ```
   Copy the printed hex string.

2. Run the INSERT:
   ```bash
   /usr/local/mysql/bin/mysql -u root -p'<mysql_password>' car_dealership -e \
     "INSERT INTO users (username, password, role, full_name) VALUES ('<username>', '<hash>', '<ROLE>', '<Full Name>');"
   ```

3. Verify:
   ```bash
   /usr/local/mysql/bin/mysql -u root -p'<mysql_password>' car_dealership -e \
     "SELECT id, username, role, full_name FROM users WHERE username='<username>';"
   ```

## Edge Cases

- **Duplicate username** — MySQL will throw a UNIQUE constraint error. Choose a different username.
- **Wrong role value** — Role must be exactly `ADMIN`, `EMPLOYEE`, or `CUSTOMER` (uppercase). MySQL ENUM will reject anything else.
- **User can't log in** — Verify the hash matches: run `hash_password.py` again with the same input and compare to what's in the DB.

## Output
User can log in at `/login.html` with the chosen username and password. ADMIN users land on `/dashboard.html`; EMPLOYEE/CUSTOMER land on `/cars.html`.
