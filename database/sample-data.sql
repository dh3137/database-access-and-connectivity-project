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

INSERT INTO Vehicles (vin, model_id, year, price, status, color, mileage, description)
VALUES
    ('JTDBR32E720081235', 1, 2023, 18500.00, 'Available', 'Super White', 15000, 'Excellent fuel economy and reliability.'),
    ('WVWZZZ1KZ6W612345', 2, 2022, 17200.00, 'Reserved', 'Deep Black', 22000, 'A classic hatchback in pristine condition.'),
    ('WAUZZZ8K9DA123456', 3, 2021, 35000.00, 'Sold', 'Ibis White', 45000, 'Luxury sedan with full service history.'),
    ('WBA8E11050K123456', 4, 2024, 31500.00, 'Available', 'Alpine White', 500, 'Brand new model with high performance package.');

INSERT INTO Customers (firstName, lastName, email, phone)
VALUES
    ('John', 'Customer', 'john.customer@example.com', '+385911112223'),
    ('Ana', 'Horvat', 'ana.horvat@example.com', '+385981234567'),
    ('Marko', 'Kovacic', 'marko.kovacic@example.com', '+385991234568');

INSERT INTO Employees (firstName, lastName, email, username, password_hash)
VALUES
    ('Ivan',      'Karlo',      'ivan.karlo@example.com',      'ivan',      'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
    ('Danis',     'Harmandic',  'danis.harmandic@example.com', 'danis',     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
    ('Jurica',    'Jamic',      'jurica.jamic@example.com',    'jurica',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
    ('Tomislav',  'Tesija',     'tomislav.tesija@example.com', 'tomo',      'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
    ('Branko',    'Mihaljevic', 'branko.mihaljevic@example.com','branko',   'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f');

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
    (1, 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a4/2019_Toyota_Corolla_Icon_Tech_VVT-i_Hybrid_1.8.jpg/1200px-2019_Toyota_Corolla_Icon_Tech_VVT-i_Hybrid_1.8.jpg'),
    (2, 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/VW_Golf_VII_1.4_TSI_BlueMotion_Technology_Highline_%28AU%29_%E2%80%93_Frontansicht%2C_31._August_2013%2C_Ratingen.jpg/1200px-VW_Golf_VII_1.4_TSI_BlueMotion_Technology_Highline_%28AU%29_%E2%80%93_Frontansicht%2C_31._August_2013%2C_Ratingen.jpg'),
    (3, 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/2018_Audi_A4_Sport_35_TFSI_1.4_Front.jpg/1200px-2018_Audi_A4_Sport_35_TFSI_1.4_Front.jpg'),
    (4, 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/00/BMW_320d_F30_LCI_Sport_Line_Shadow_Automatic.jpg/1200px-BMW_320d_F30_LCI_Sport_Line_Shadow_Automatic.jpg');