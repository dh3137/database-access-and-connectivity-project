# Manual Test Checklist

Use this checklist before demo or submission. Start from a freshly seeded database when possible.

## Setup

- Run `mvn compile`.
- Run `mvn test`.
- Confirm `db.properties` exists in the project root and has an entry for your OS username.
- Start the app with `mvn compile exec:java`.
- Open `http://localhost:8080`.

## Authentication

- Log in as admin: `ivan / password123`.
- Confirm admin redirects to `dashboard.html`.
- Log out.
- Log in as customer: `john / password123`.
- Confirm customer can browse cars but cannot access admin dashboard actions.

## Inventory

- Open the car listing page and confirm cars load.
- Open a vehicle details page and confirm make, model, price, status, VIN, and image load.
- As admin, create a vehicle with valid year, price, mileage, status, and VIN.
- Try creating a vehicle with invalid data, such as negative price or short VIN, and confirm the server returns a safe validation error.
- Update an existing vehicle and confirm the changed values appear after refresh.
- Delete a test vehicle and confirm it no longer appears in the inventory.

## Enquiries

- As customer, submit an enquiry from a vehicle details page.
- As admin, open the dashboard and confirm the enquiry appears.
- Mark the enquiry read/unread and confirm the status changes.

## Sales

- As admin, record a sale for an available vehicle.
- Confirm the sale request succeeds.
- Confirm the vehicle status becomes `SOLD`.
- Confirm the sale appears in Recent Sales.
- Try recording another sale for the same vehicle and confirm it is rejected.
- If the vehicle had an active reservation, confirm the reservation status changes to `Converted` in the database.

## Audit

- Confirm vehicle create/update actions appear in `VehicleChangeLog`.
- Confirm sale recording writes a vehicle status change to `VehicleChangeLog`.
- Confirm login, sale, vehicle delete, and enquiry status actions write to `ActionLog`.

## SQL Checks

Run these only if you want to inspect the database directly:

```sql
SELECT * FROM Sales ORDER BY sale_date DESC LIMIT 5;
SELECT * FROM Vehicles WHERE status = 'Sold' ORDER BY vehicle_id DESC LIMIT 5;
SELECT * FROM VehicleChangeLog ORDER BY change_date DESC LIMIT 5;
SELECT * FROM ActionLog ORDER BY action_date DESC LIMIT 10;
```
