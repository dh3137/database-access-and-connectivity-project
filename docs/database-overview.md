# Database Overview

This project uses a relational MySQL schema centered around inventory, people,
sales activity, and dealership operations.

## Core Structure

The data model is easiest to understand as six connected areas:

1. Vehicle catalog
2. People and accounts
3. Staff access support tables
4. Customer activity and sales
5. Reviews and service history
6. Audit logging

## 1. Vehicle Catalog

### `Manufacturers`
- One row per car brand.
- Example data: Toyota, BMW, Audi.
- Referenced by: `Models.manufacturer_id`

### `Models`
- Defines model-level information like model name, body type, and segment.
- Referenced by: `Vehicles.model_id`, `Reviews.model_id`
- Relationship: many models belong to one manufacturer.

### `Vehicles`
- Represents actual inventory units listed in the dealership.
- Stores year, price, mileage, VIN, status, and description.
- Relationship: many vehicles belong to one model.
- Used by:
  - public inventory pages
  - admin dashboard
  - sales flow
  - enquiries
  - maintenance history
  - activity log

### `VehicleImages`
- Stores one or more image URLs per vehicle.
- Relationship: many images belong to one vehicle.
- Used by: vehicle cards and detail pages.

## 2. People and Accounts

### `Customers`
- Contact and profile information for buyers.
- Used by:
  - signup
  - enquiries
  - sales
  - test drives
  - reservations

### `Employees`
- Contact and employment information for staff.
- Used by:
  - employee directory
  - sales attribution
  - vehicle change logging
  - maintenance and enquiry workflows

### `Users`
- Login account table.
- Each user is one of:
  - `ADMIN`
  - `EMPLOYEE`
  - `CUSTOMER`
- A user links either to an employee or a customer record.
- Used by:
  - login
  - session identity
  - live access control
  - deciding whether a staff user has admin-level inventory access

## 3. Staff Access Support Tables

The schema still includes extra role/permission tables, but the current app no
longer uses them for runtime authorization. Live access is now driven directly
by `Users.role`:

1. `ADMIN` can access all staff screens and perform vehicle inventory CRUD.
2. `EMPLOYEE` can access the same staff workflow screens, but cannot add, edit,
   or delete vehicles.
3. `CUSTOMER` is limited to customer flows.

### `Roles`
- Optional future-expansion table for named staff roles.
- Not used by the current runtime flow.

### `Permissions`
- Optional future-expansion table for fine-grained capabilities.
- Not used by the current runtime flow.

### `EmployeeRoles`
- Join table between employees and roles.
- One employee can have multiple roles.
- Currently unused by the live app.

### `RolePermissions`
- Join table between roles and permissions.
- One role can grant multiple permissions.
- Currently unused by the live app.

## 4. Customer Activity and Sales

### `Enquiries`
- Stores customer messages about vehicles.
- May be linked to a logged-in customer and a specific vehicle.
- Used by:
  - vehicle details enquiry modal
  - dashboard enquiries table

### `Sales`
- Stores completed sales.
- Links:
  - vehicle
  - customer
  - employee
- Used by:
  - mark-as-sold flow
  - recent sales dashboard

### `Reservations`
- Stores reserved vehicles pending conversion or cancellation.
- Present for buying-flow support and future expansion.

### `TestDrives`
- Stores scheduled appointments for viewings or test drives.
- Present for future expansion and real dealership workflows.

## 5. Reviews and Service History

### `Reviews`
- Model-level reviews and ratings.
- Links to `Models`, not directly to `Vehicles`.
- Used by:
  - reviews page
  - vehicle details page

### `MaintenanceHistory`
- Service history records for individual vehicles.
- Links to `Vehicles`.
- Used by:
  - service history section on vehicle details
  - staff maintenance entry flow

## 6. Audit Logging

### `VehicleChangeLog`
- Tracks inserts, updates, and deletes performed on vehicles.
- Links each change to:
  - a vehicle
  - an employee
- Used by:
  - dashboard activity log

## Main Relationship Chains

The most important table chains are:

- `Manufacturers -> Models -> Vehicles -> VehicleImages`
- `Customers -> Users`
- `Employees -> Users`
- `Vehicles + Customers + Employees -> Sales`
- `Vehicles + Customers -> Enquiries`
- `Vehicles -> MaintenanceHistory`
- `Models -> Reviews`
- `Vehicles + Employees -> VehicleChangeLog`

## Which Parts of the App Use Which Tables

### Public inventory pages
- `Manufacturers`
- `Models`
- `Vehicles`
- `VehicleImages`

### Vehicle detail page
- `Vehicles`
- `VehicleImages`
- `Reviews`
- `MaintenanceHistory`
- `Enquiries`

### Login and signup
- `Users`
- `Customers`
- `Employees`

### Admin/staff dashboard
- `Vehicles`
- `Customers`
- `Employees`
- `Sales`
- `Enquiries`
- `VehicleChangeLog`

## Design Notes

- `Users.role` defines the account type.
- `ADMIN` is full-access staff.
- `EMPLOYEE` can work with sales, enquiries, maintenance, logs, and the employee directory.
- Vehicle inventory CRUD stays admin-only.
- `Roles`, `Permissions`, `EmployeeRoles`, and `RolePermissions` remain in the schema as future expansion tables, but they are not part of the current app flow.
- The app relies heavily on foreign-key relationships to keep inventory,
  customer, employee, and sales data consistent.
