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

-- Vehicles
INSERT INTO Vehicles (model_id, year, color, mileage, price, vin, status, description) VALUES
    -- Economy
    (1,  2023, 'Scarlet Red',     8000,  16900.00, 'JTDKB3EU7N3123401', 'Available', 'Toyota Yaris, fuel-efficient city car'),
    (1,  2022, 'Ice Silver',     21000,  14500.00, 'JTDKB3EU6M3123402', 'Available', 'Low mileage, single owner'),
    (2,  2023, 'Pearl White',    12000,  18500.00, 'JTDBR32E720081235', 'Available', 'Well maintained, one owner'),
    (2,  2021, 'Graphite Grey',  39000,  15800.00, 'JTDBR32E520081236', 'Available', 'Full service history'),
    (3,  2022, 'Deep Black',     28000,  17200.00, 'WVWZZZ1KZ6W612345', 'Available', 'Full service history'),
    (3,  2020, 'Tornado Red',    52000,  13900.00, 'WVWZZZ1KZ4W612346', 'Available', 'Excellent condition'),
    (4,  2023, 'Lunar Silver',    5000,  19900.00, 'JHMFC2F54PX000101', 'Available', 'Honda Civic, sporty and efficient'),
    (4,  2021, 'Rallye Red',     34000,  16500.00, 'JHMFC2F52MX000102', 'Available', 'One careful owner'),
    -- Sport
    (5,  2023, 'Sao Paulo Yellow', 3000, 89900.00, 'WBS42AH07PCF12301', 'Available', 'BMW M4, Competition Package, full M spec'),
    (5,  2022, 'Alpine White',   18000,  82000.00, 'WBS42AH06NCF12302', 'Available', 'Carbon fibre interior, M exhaust'),
    (6,  2023, 'GT Silver',       2000,  72500.00, 'WP0CB2A93PS700101', 'Available', 'Porsche 718 Boxster, PDK gearbox'),
    (6,  2021, 'Gentian Blue',   14000,  64000.00, 'WP0CB2A91MS700102', 'Available', 'Sports Chrono Package'),
    (7,  2023, 'Championship White',1200,47500.00, 'JHMFL5H82PX000201', 'Available', 'Honda Civic Type R, track-ready'),
    (8,  2022, 'Supersonic Red',  8000,  38000.00, 'JF1ZNBA15N8702301', 'Available', 'Toyota GR86, naturally aspirated'),
    -- Luxury
    (9,  2024, 'Obsidian Black',  1500, 125000.00, 'WDD2220561A000101', 'Available', 'Mercedes S-Class, AMG Line, Burmester audio'),
    (9,  2022, 'Polar White',    22000, 102000.00, 'WDD2220561A000102', 'Available', 'Panoramic roof, rear seat entertainment'),
    (10, 2023, 'Daytona Grey',    4000,  98000.00, 'WAUZZZ4H8AN000201', 'Available', 'Audi A8, quattro, Matrix LED'),
    (11, 2023, 'Cashmere Silver', 6000,  92000.00, 'WBA7J41070CL00101', 'Available', 'BMW 7 Series, Sky Lounge panoramic roof'),
    (12, 2022, 'Chalk',          15000,  88000.00, 'WP0AA2A76NS000401', 'Available', 'Porsche Panamera, Sport Turismo'),
    (13, 2020, 'Glacier White',  45000,  35000.00, 'WAUZZZ8K9DA123456', 'Sold',      'Sport package included'),
    (14, 2023, 'Sapphire Blue',   5000,  31500.00, 'WBA8E11050K123456', 'Available', 'Nearly new, still under warranty'),
    -- Exotic
    (15, 2021, 'Rosso Corsa',     3500, 279000.00, 'ZFF80NJA0M0264101', 'Available', 'Ferrari 488 GTB, Scuderia shields, carbon pack'),
    (15, 2020, 'Giallo Modena',   9000, 255000.00, 'ZFF80NJA0L0264102', 'Available', 'Ferrari 488 GTB, low mileage supercar'),
    (16, 2023, 'Arancio Borealis',1800, 245000.00, 'ZHWUC1ZF8PLA00101', 'Available', 'Lamborghini Huracan Evo, forged carbon'),
    (17, 2022, 'Volcano Orange',  4500, 310000.00, 'SBM14DCA4NW006001', 'Available', 'McLaren 720S, Track Pack, Senna seats'),
    (18, 2023, 'Rosso Ferrari',   2200, 295000.00, 'ZFF94HLA0P0001001', 'Available', 'Ferrari F8 Tributo, 710 hp, carbon fibre'),
    (19, 2022, 'Grigio Titans',   8000, 320000.00, 'ZHWUR2ZF1NLA09101', 'Available', 'Lamborghini Urus Performante, ceramic brakes'),
    -- SUV
    (20, 2023, 'Carbon Black',    7000,  72000.00, 'WBAJP0C52PB000101', 'Available', 'BMW X5 xDrive40i, M Sport Package'),
    (20, 2021, 'Mineral White',  31000,  58000.00, 'WBAJP0C50MB000102', 'Available', 'Panoramic roof, third row seating'),
    (21, 2023, 'Selenite Grey',   4500,  79000.00, 'WDC1670731A000201', 'Available', 'Mercedes GLE 450, AMG Line, air suspension'),
    (22, 2022, 'Jet Black',      18000,  85000.00, 'WP1AA2AY8NDA00101', 'Available', 'Porsche Cayenne Turbo, Sport Chrono'),
    (23, 2023, 'Navarra Blue',    3000,  88000.00, 'WAUZZZ4MXPD000301', 'Available', 'Audi Q8 e-tron, 55 TFSI quattro'),
    (24, 2022, 'Magnetic Grey',  27000,  32000.00, 'JTMRFREV5MD000401', 'Available', 'Toyota RAV4 Hybrid, all-wheel drive'),
    (25, 2023, 'Reflex Silver',   9000,  38500.00, 'WVGZZZ5NZPW000501', 'Available', 'VW Tiguan R-Line, panoramic sunroof'),
    (26, 2021, 'Sonic Grey',     42000,  27500.00, 'JHMRW2H53MX000601', 'Available', 'Honda CR-V Hybrid, excellent MPG');

-- VehicleImages (unique URL per vehicle, fetched from Unsplash 2026-04-24)
INSERT INTO VehicleImages (vehicle_id, image_url, is_primary) VALUES
    -- Economy: Toyota Yaris (model 1) — 2 unique photos
    (1,  'https://images.unsplash.com/photo-1742742646347-4f708c0afca4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (2,  'https://images.unsplash.com/photo-1742742646348-32384aeaefec?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Economy: Toyota Corolla (model 2) — 2 unique photos
    (3,  'https://images.unsplash.com/photo-1691994866119-14c702835475?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (4,  'https://images.unsplash.com/photo-1691994877641-36e673ad4236?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Economy: VW Golf (model 3) — 2 unique photos
    (5,  'https://images.unsplash.com/photo-1645307621773-1e8d3d0e2482?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (6,  'https://images.unsplash.com/photo-1678120597905-b2f3a982b625?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Economy: Honda Civic (model 4) — 2 unique photos
    (7,  'https://images.unsplash.com/photo-1711226876715-53a1882660e9?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (8,  'https://images.unsplash.com/photo-1711226876657-3801d4a4e0ab?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Sport: BMW M4 (model 5) — 2 unique photos
    (9,  'https://images.unsplash.com/photo-1728060838342-cb9744a27d1b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (10, 'https://images.unsplash.com/photo-1634214564170-893240d3b789?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Sport: Porsche 718 Boxster (model 6) — 2 unique photos
    (11, 'https://images.unsplash.com/photo-1721572345790-507985e4317a?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (12, 'https://images.unsplash.com/photo-1560691647-7085e5cebcfe?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Sport: Honda Civic Type R (model 7)
    (13, 'https://images.unsplash.com/photo-1641921966529-7a887417a8b2?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Sport: Toyota GR86 (model 8)
    (14, 'https://images.unsplash.com/photo-1729009491967-63901999a656?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Luxury: Mercedes S-Class (model 9) — 2 unique photos
    (15, 'https://images.unsplash.com/photo-1706977384830-df8b515e6b70?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    (16, 'https://images.unsplash.com/photo-1698816688678-a3f838fd4fe0?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
    -- Luxury: Audi A8 (model 10)
    (17, 'https://images.unsplash.com/photo-1693945423710-626e031eef83?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
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
    (27, 'https://images.unsplash.com/photo-1569398890582-1943b9a5c94b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixlib=rb-4.1.0&q=80&w=1080', TRUE),
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

-- Customers
INSERT INTO Customers (first_name, last_name, email, phone) VALUES
    ('John',  'Customer', 'john.customer@example.com', '+385911112223'),
    ('Ana',   'Horvat',   'ana.horvat@example.com',    '+385981234567'),
    ('Marko', 'Kovacic',  'marko.kovacic@example.com', '+385991234568');

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