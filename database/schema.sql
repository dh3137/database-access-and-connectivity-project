DROP DATABASE IF EXISTS car_dealership;
CREATE DATABASE car_dealership;
USE car_dealership;

-- ─── 01 Vehicle Catalog ───────────────────────────────────────────────────────

CREATE TABLE Manufacturers (
    manufacturer_id INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL UNIQUE,
    country         VARCHAR(50)  NOT NULL,
    founded_year    INT,
    website_url     VARCHAR(255)
);

CREATE TABLE Models (
    model_id        INT PRIMARY KEY AUTO_INCREMENT,
    manufacturer_id INT         NOT NULL,
    model_name      VARCHAR(100) NOT NULL,
    body_type       VARCHAR(50),
    segment         VARCHAR(50),
    CONSTRAINT fk_models_manufacturer
        FOREIGN KEY (manufacturer_id) REFERENCES Manufacturers(manufacturer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE Vehicles (
    vehicle_id  INT PRIMARY KEY AUTO_INCREMENT,
    model_id    INT              NOT NULL,
    year        INT              NOT NULL,
    color       VARCHAR(50),
    mileage     INT              NOT NULL DEFAULT 0,
    price       DECIMAL(10,2)   NOT NULL,
    vin         VARCHAR(17)      NOT NULL UNIQUE,
    status      ENUM('Available','Reserved','Sold') NOT NULL DEFAULT 'Available',
    description TEXT,
    date_added  TIMESTAMP        NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_vehicles_model
        FOREIGN KEY (model_id) REFERENCES Models(model_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_vin_length CHECK (CHAR_LENGTH(vin) = 17),
    CONSTRAINT chk_vehicle_year CHECK (year BETWEEN 1900 AND 2100),
    CONSTRAINT chk_vehicle_price_positive CHECK (price > 0),
    CONSTRAINT chk_vehicle_mileage_nonnegative CHECK (mileage >= 0)
);

CREATE TABLE VehicleImages (
    image_id   INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT          NOT NULL,
    image_url  VARCHAR(500) NOT NULL,
    is_primary BOOLEAN      NOT NULL DEFAULT FALSE,
    upload_date TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_vehicle_images_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES Vehicles(vehicle_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ─── 02 People ────────────────────────────────────────────────────────────────

CREATE TABLE Customers (
    customer_id     INT PRIMARY KEY AUTO_INCREMENT,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone           VARCHAR(20),
    address         VARCHAR(255),
    date_registered TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE Employees (
    emp_id     INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    phone      VARCHAR(20),
    hire_date  DATE         NOT NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ─── 03 Authentication ────────────────────────────────────────────────────────

CREATE TABLE Users (
    user_id     INT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(64)  NOT NULL,
    role        ENUM('ADMIN','EMPLOYEE','CUSTOMER') NOT NULL,
    emp_id      INT,
    customer_id INT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_users_employee
        FOREIGN KEY (emp_id) REFERENCES Employees(emp_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_users_customer
        FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
        ON DELETE SET NULL ON UPDATE CASCADE
    -- role mutual-exclusivity enforced by application layer (saveUser branches on empId vs customerId)
);

-- ─── 04 Transactions ──────────────────────────────────────────────────────────

CREATE TABLE Sales (
    sale_id        INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id     INT           NOT NULL,
    customer_id    INT           NOT NULL,
    emp_id         INT           NOT NULL,
    sale_date      TIMESTAMP     NOT NULL DEFAULT NOW(),
    sale_price     DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    notes          TEXT,
    CONSTRAINT fk_sales_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES Vehicles(vehicle_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_sales_customer
        FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_sales_employee
        FOREIGN KEY (emp_id) REFERENCES Employees(emp_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_sale_price_positive CHECK (sale_price > 0)
);

-- Business layer converts active reservations when a vehicle is sold.
CREATE TABLE Reservations (
    reservation_id   INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id       INT           NOT NULL,
    customer_id      INT           NOT NULL,
    emp_id           INT           NOT NULL,
    reservation_date TIMESTAMP     NOT NULL DEFAULT NOW(),
    expiry_date      DATE          NOT NULL,
    status           ENUM('Active','Cancelled','Converted') NOT NULL DEFAULT 'Active',
    deposit_amount   DECIMAL(10,2),
    CONSTRAINT fk_reservations_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES Vehicles(vehicle_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_reservations_customer
        FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_reservations_employee
        FOREIGN KEY (emp_id) REFERENCES Employees(emp_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_reservation_deposit_nonnegative CHECK (deposit_amount IS NULL OR deposit_amount >= 0)
);

CREATE TABLE TestDrives (
    test_drive_id    INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id       INT      NOT NULL,
    customer_id      INT      NOT NULL,
    emp_id           INT      NOT NULL,
    scheduled_date   DATETIME NOT NULL,
    duration_minutes INT      NOT NULL DEFAULT 30,
    status           ENUM('Scheduled','Completed','Cancelled') NOT NULL DEFAULT 'Scheduled',
    notes            TEXT,
    CONSTRAINT fk_test_drives_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES Vehicles(vehicle_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_test_drives_customer
        FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_test_drives_employee
        FOREIGN KEY (emp_id) REFERENCES Employees(emp_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_test_drive_duration_positive CHECK (duration_minutes > 0)
);

-- ─── 05 Audit & Maintenance ───────────────────────────────────────────────────

CREATE TABLE MaintenanceHistory (
    maintenance_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id     INT           NOT NULL,
    service_date   DATE          NOT NULL,
    service_type   VARCHAR(100)  NOT NULL,
    description    TEXT,
    cost           DECIMAL(10,2),
    performed_by   VARCHAR(100),
    CONSTRAINT fk_maintenance_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES Vehicles(vehicle_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_maintenance_cost_nonnegative CHECK (cost IS NULL OR cost >= 0)
);

CREATE TABLE VehicleChangeLog (
    log_id        INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id    INT          NOT NULL,
    emp_id        INT          NOT NULL,
    change_date   TIMESTAMP    NOT NULL DEFAULT NOW(),
    change_type   ENUM('INSERT','UPDATE','DELETE') NOT NULL,
    field_changed VARCHAR(100),
    old_value     TEXT,
    new_value     TEXT,
    CONSTRAINT fk_vehicle_change_log_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES Vehicles(vehicle_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_vehicle_change_log_employee
        FOREIGN KEY (emp_id) REFERENCES Employees(emp_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE ActionLog (
    action_id   INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT,
    emp_id      INT,
    action_type VARCHAR(50)  NOT NULL,
    object_type VARCHAR(50),
    object_id   VARCHAR(50),
    details     TEXT,
    action_date TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_action_log_user
        FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_action_log_employee
        FOREIGN KEY (emp_id) REFERENCES Employees(emp_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- ─── Indexes ──────────────────────────────────────────────────────────────────

CREATE INDEX idx_vehicles_status ON Vehicles(status);
CREATE INDEX idx_vehicles_model  ON Vehicles(model_id);
CREATE INDEX idx_sales_vehicle   ON Sales(vehicle_id);
CREATE INDEX idx_users_username  ON Users(username);
CREATE INDEX idx_action_log_date ON ActionLog(action_date);

-- ─── Reviews ──────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS Reviews (
  review_id    INT AUTO_INCREMENT PRIMARY KEY,
  model_id     INT NOT NULL,
  author_name  VARCHAR(100) NOT NULL,
  rating       TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  review_text  TEXT NOT NULL,
  source       ENUM('TEAM') DEFAULT 'TEAM',
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (model_id) REFERENCES Models(model_id) ON DELETE CASCADE
);

-- ─── Enquiries ────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS Enquiries (
  enquiry_id   INT PRIMARY KEY AUTO_INCREMENT,
  vehicle_id   INT,
  customer_id  INT,
  name         VARCHAR(100) NOT NULL,
  email        VARCHAR(100) NOT NULL,
  phone        VARCHAR(20),
  message      TEXT,
  submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_read      BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_enquiries_vehicle
    FOREIGN KEY (vehicle_id) REFERENCES Vehicles(vehicle_id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_enquiries_customer
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
    ON DELETE SET NULL ON UPDATE CASCADE
);

-- Migration (run once if table already exists):
-- ALTER TABLE Enquiries ADD COLUMN customer_id INT NULL,
--   ADD CONSTRAINT fk_enquiries_customer FOREIGN KEY (customer_id) REFERENCES Customers(customer_id) ON DELETE SET NULL ON UPDATE CASCADE;
