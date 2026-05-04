-- Migration: add integrity constraints and general ActionLog table.
-- Run once against an existing local car_dealership database.
--
-- Apply:
--   /usr/local/mysql/bin/mysql -u root -p car_dealership < database/migrations/001_add_integrity_and_action_log.sql
--
-- Rollback note:
--   For this class project, the normal rollback is a local reset with database/schema.sql
--   and database/sample-data.sql. Do not run this migration twice.

USE car_dealership;

ALTER TABLE Vehicles
    ADD CONSTRAINT chk_vehicle_year CHECK (year BETWEEN 1900 AND 2100),
    ADD CONSTRAINT chk_vehicle_price_positive CHECK (price > 0),
    ADD CONSTRAINT chk_vehicle_mileage_nonnegative CHECK (mileage >= 0);

ALTER TABLE Sales
    ADD CONSTRAINT chk_sale_price_positive CHECK (sale_price > 0);

ALTER TABLE Reservations
    ADD CONSTRAINT chk_reservation_deposit_nonnegative CHECK (deposit_amount IS NULL OR deposit_amount >= 0);

ALTER TABLE TestDrives
    ADD CONSTRAINT chk_test_drive_duration_positive CHECK (duration_minutes > 0);

ALTER TABLE MaintenanceHistory
    ADD CONSTRAINT chk_maintenance_cost_nonnegative CHECK (cost IS NULL OR cost >= 0);

CREATE TABLE IF NOT EXISTS ActionLog (
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

CREATE INDEX idx_action_log_date ON ActionLog(action_date);
