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

<<<<<<< HEAD
INSERT INTO Customers (firstName, lastName, email, phone)
VALUES
    ('John', 'Customer', 'john.customer@example.com', '+385911112223'),
    ('Ana', 'Horvat', 'ana.horvat@example.com', '+385981234567'),
    ('Marko', 'Kovacic', 'marko.kovacic@example.com', '+385991234568');
=======
-- VehicleImages (unique URL per vehicle, fetched from Unsplash 2026-04-24)
INSERT INTO VehicleImages (vehicle_id, image_url, is_primary) VALUES
    -- Economy: Toyota Yaris (model 1) — 2 unique photos
    (1,  'https://images.unsplash.com/photo-1742742646347-4f708c0afca4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (2,  'https://images.unsplash.com/photo-1742742646348-32384aeaefec?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Economy: Toyota Corolla (model 2) — 2 unique photos
    (3,  'https://images.unsplash.com/photo-1711978477980-a0f1a05039a0?w=800&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTd8fFRveW90YSUyMENvcm9sbGElMjAyMDIzfGVufDB8fDB8fHww', TRUE),
    (4,  'https://images.unsplash.com/photo-1763268265028-1631360c07dd?q=80&w=2574&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D', TRUE),
    -- Economy: VW Golf (model 3) — 2 unique photos
    (5,  'https://images.unsplash.com/photo-1724620961935-ee922e2dfad5?q=80&w=2572&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D', TRUE),
    (6,  'https://images.unsplash.com/photo-1683444126212-50c0aa2a421b?q=80&w=1738&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D', TRUE),
    -- Economy: Honda Civic (model 4) — 2 unique photos
    (7,  'https://images.unsplash.com/photo-1711226876715-53a1882660e9?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (8,  'https://images.unsplash.com/photo-1711226876657-3801d4a4e0ab?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Sport: BMW M4 (model 5) — 2 unique photos
    (9,  'https://images.unsplash.com/photo-1728060838342-cb9744a27d1b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (10, 'https://images.unsplash.com/photo-1634214564170-893240d3b789?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Sport: Porsche 718 Boxster (model 6) — 2 unique photos
    (11, 'https://images.unsplash.com/photo-1632245889029-e406faaa34cd?q=80&w=2564&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D', TRUE),
    (12, 'https://images.unsplash.com/photo-1632245872256-49c78a46c400?q=80&w=2564&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D', TRUE),
    -- Sport: Honda Civic Type R (model 7)
    (13, 'https://images.unsplash.com/photo-1641921966529-7a887417a8b2?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Sport: Toyota GR86 (model 8)
    (14, 'https://images.unsplash.com/photo-1729009491967-63901999a656?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Luxury: Mercedes S-Class (model 9) — 2 unique photos
    (15, 'https://images.unsplash.com/photo-1706977384830-df8b515e6b70?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (16, 'https://images.unsplash.com/photo-1698816688678-a3f838fd4fe0?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Luxury: Audi A8 (model 10)
    (17, 'https://t3.ftcdn.net/jpg/04/45/55/36/240_F_445553697_1xwHRCxI6lL6ZzA3uicOmOgAY6mgAg4E.jpg', TRUE),
    -- Luxury: BMW 7 Series (model 11)
    (18, 'https://images.unsplash.com/photo-1627936354732-ffbe552799d8?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Luxury: Porsche Panamera (model 12)
    (19, 'https://images.unsplash.com/photo-1656751556025-002685d0f3b1?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Luxury: Audi A4 (model 13)
    (20, 'https://images.unsplash.com/photo-1670686189004-3f824f40027e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Luxury: BMW 320d (model 14)
    (21, 'https://images.unsplash.com/photo-1750670950984-d4095aec9e81?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Exotic: Ferrari 488 GTB (model 15) — 2 unique photos
    (22, 'https://images.unsplash.com/photo-1634824162531-a9f139a1d43a?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (23, 'https://images.unsplash.com/photo-1634823929885-b12342dfc408?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Exotic: Lamborghini Huracan (model 16)
    (24, 'https://images.unsplash.com/photo-1696581082291-5230d4de01c5?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Exotic: McLaren 720S (model 17)
    (25, 'https://images.unsplash.com/photo-1689596323441-94fe62ac5f0c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Exotic: Ferrari F8 Tributo (model 18)
    (26, 'https://images.unsplash.com/photo-1655593972695-919eb269b0df?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Exotic: Lamborghini Urus (model 19)
    (27, 'https://images.unsplash.com/photo-1627140290942-7c8f9f56e870?w=800&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8TGFtYm9yZ2hpbmklMjBVcnVzJTIwMjAyMnxlbnwwfHwwfHx8MA%3D%3D', TRUE),
    -- SUV: BMW X5 (model 20) — 2 unique photos
    (28, 'https://images.unsplash.com/photo-1615908397724-6dc711db34a7?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (29, 'https://images.unsplash.com/photo-1717082842911-9e55aeaf80cb?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- SUV: Mercedes GLE (model 21)
    (30, 'https://images.unsplash.com/photo-1677764822912-4f99e9326243?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- SUV: Porsche Cayenne (model 22)
    (31, 'https://images.unsplash.com/photo-1738780151621-98ca2d4fad93?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- SUV: Audi Q8 (model 23)
    (32, 'https://images.unsplash.com/photo-1584558303984-0ac08c41ddc5?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- SUV: Toyota RAV4 (model 24)
    (33, 'https://images.unsplash.com/photo-1597799980291-19f88b1cf1c1?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- SUV: VW Tiguan (model 25)
    (34, 'https://images.unsplash.com/photo-1653310555014-bb4ac201b4a5?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- SUV: Honda CR-V (model 26)
    (35, 'https://images.unsplash.com/photo-1616559650863-a043ce0188fc?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE);



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