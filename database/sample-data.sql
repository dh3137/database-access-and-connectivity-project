USE car_dealership;

INSERT INTO Manufacturers (name)
VALUES
    ('Toyota'),
    ('Volkswagen'),
    ('Audi'),
    ('BMW');

INSERT INTO Models (manufacturer_id, model_name)
VALUES
    (1, 'Corolla'),
    (2, 'Golf'),
    (3, 'A4'),
    (4, '320d');

INSERT INTO Vehicles (vin, model_id, price, status)
VALUES
    ('JTDBR32E720081235', 1, 18500.00, 'Available'),
    ('WVWZZZ1KZ6W612345', 2, 17200.00, 'Reserved'),
    ('WAUZZZ8K9DA123456', 3, 35000.00, 'Sold'),
    ('WBA8E11050K123456', 4, 31500.00, 'Available');

INSERT INTO Customers (firstName, lastName, email, phone)
VALUES
    ('John', 'Customer', 'john.customer@example.com', '+385911112223'),
    ('Ana', 'Horvat', 'ana.horvat@example.com', '+385981234567'),
    ('Marko', 'Kovacic', 'marko.kovacic@example.com', '+385991234568');

INSERT INTO Employees (firstName, lastName, email)
VALUES
    ('Ivan', 'Karlo', 'ivan.karlo@example.com'),
    ('Danis', 'Harmandic', 'danis.harmandic@example.com'),
    ('Jurica', 'Jamic', 'jurica.jamic@example.com'),
    ('Tomislav', 'Tesija', 'tomislav.tesija@example.com'),
    ('Branko', 'Mihaljevic', 'branko.mihaljevic@example.com');

INSERT INTO Roles (role_name)
VALUES
    ('Admin'),
    ('Salesperson'),
    ('Employee');

INSERT INTO Permissions (permission_name)
VALUES
    ('Add Vehicle'),
    ('Update Vehicle'),
    ('Remove Vehicle'),
    ('Record Sale'),
    ('View Sales Records'),
    ('Manage Customers'),
    ('Manage Reservations');

INSERT INTO EmployeeRoles (emp_id, role_id, assigned_date)
VALUES
    (1, 1, '2026-04-01'),
    (2, 1, '2026-04-01'),
    (3, 1, '2026-04-01'),
    (4, 1, '2026-04-01'),
    (5, 3, '2026-04-01');

INSERT INTO RolePermissions (role_id, permission_id)
VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (1, 5),
    (1, 6),
    (1, 7),
    (2, 1),
    (2, 2),
    (2, 4),
    (2, 5),
    (2, 6),
    (2, 7),
    (3, 5);

INSERT INTO Sales (sale_date, vehicle_id, customer_id, emp_id)
VALUES
    ('2026-04-10', 3, 1, 1);

INSERT INTO MaintenanceHistory (vehicle_id, service_date, description)
VALUES
    (1, '2026-03-15', 'Regular oil and filter change'),
    (2, '2026-03-20', 'Brake pads replaced'),
    (3, '2026-02-10', 'Annual inspection completed');

INSERT INTO VehicleChangeLog (vehicle_id, emp_id, change_type, change_date)
VALUES
    (1, 1, 'INSERT', '2026-04-01 09:00:00'),
    (2, 2, 'UPDATE', '2026-04-05 11:30:00'),
    (3, 1, 'UPDATE', '2026-04-10 14:15:00'),
    (4, 4, 'INSERT', '2026-04-12 10:45:00');

INSERT INTO Reservations (vehicle_id, customer_id, reservation_date)
VALUES
    (2, 2, '2026-04-18');

INSERT INTO TestDrives (vehicle_id, customer_id, scheduled_date)
VALUES
    (1, 3, '2026-04-25'),
    (4, 2, '2026-04-26');

INSERT INTO VehicleImages (vehicle_id, image_url)
VALUES
    (1, 'images/toyota_corolla_1.jpg'),
    (2, 'images/vw_golf_1.jpg'),
    (3, 'images/audi_a4_1.jpg'),
    (4, 'images/bmw_320d_1.jpg');