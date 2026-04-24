CREATE DATABASE IF NOT EXISTS car_dealership;
USE car_dealership;

CREATE TABLE Manufacturers (
    manufacturer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Models (
    model_id INT PRIMARY KEY AUTO_INCREMENT,
    manufacturer_id INT NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_models_manufacturer
        FOREIGN KEY (manufacturer_id)
        REFERENCES Manufacturers(manufacturer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE Vehicles (
    vehicle_id INT PRIMARY KEY AUTO_INCREMENT,
    vin VARCHAR(17) NOT NULL UNIQUE,
    model_id INT NOT NULL,
    year INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status ENUM('Available','Reserved','Sold') NOT NULL DEFAULT 'Available',
    color VARCHAR(50),
    mileage INT,
    description TEXT,
    CONSTRAINT fk_vehicles_model
        FOREIGN KEY (model_id)
        REFERENCES Models(model_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE Customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    firstName varchar(20), 
    lastName varchar(20),
    email VARCHAR(100) NOT NULL UNIQUE,
    phone varchar(20)

);

CREATE TABLE Employees (
    emp_id INT PRIMARY KEY AUTO_INCREMENT,
    firstName varchar(20),
    lastName varchar(20),
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) UNIQUE,
    password_hash VARCHAR(64)
);

CREATE TABLE Roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Permissions (
    permission_id INT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE EmployeeRoles (
    emp_id INT NOT NULL,
    role_id INT NOT NULL,
    assigned_date DATE NOT NULL,
    PRIMARY KEY (emp_id, role_id),
    CONSTRAINT fk_employee_roles_employee
        FOREIGN KEY (emp_id)
        REFERENCES Employees(emp_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_employee_roles_role
        FOREIGN KEY (role_id)
        REFERENCES Roles(role_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE RolePermissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES Roles(role_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES Permissions(permission_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Sales (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_date DATE NOT NULL,
    vehicle_id INT NOT NULL,
    customer_id INT NOT NULL,
    emp_id INT NOT NULL,
    CONSTRAINT fk_sales_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES Vehicles(vehicle_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_sales_customer
        FOREIGN KEY (customer_id)
        REFERENCES Customers(customer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_sales_employee
        FOREIGN KEY (emp_id)
        REFERENCES Employees(emp_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE MaintenanceHistory (
    maintenance_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT NOT NULL,
    service_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    CONSTRAINT fk_maintenance_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES Vehicles(vehicle_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE VehicleChangeLog (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT NOT NULL,
    emp_id INT NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    change_date DATETIME NOT NULL,
    CONSTRAINT fk_vehicle_change_log_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES Vehicles(vehicle_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_vehicle_change_log_employee
        FOREIGN KEY (emp_id)
        REFERENCES Employees(emp_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE Reservations (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT NOT NULL,
    customer_id INT NOT NULL,
    reservation_date DATE NOT NULL,
    CONSTRAINT fk_reservations_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES Vehicles(vehicle_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_reservations_customer
        FOREIGN KEY (customer_id)
        REFERENCES Customers(customer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE TestDrives (
    test_drive_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT NOT NULL,
    customer_id INT NOT NULL,
    scheduled_date DATE NOT NULL,
    CONSTRAINT fk_test_drives_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES Vehicles(vehicle_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_test_drives_customer
        FOREIGN KEY (customer_id)
        REFERENCES Customers(customer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE VehicleImages (
    image_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    CONSTRAINT fk_vehicle_images_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES Vehicles(vehicle_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE action_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50),
    action VARCHAR(100),
    detail VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);