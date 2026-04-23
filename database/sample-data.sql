USE car_dealership;

-- Manufacturers
INSERT INTO Manufacturers (name, country, founded_year, website_url) VALUES
    ('Toyota',       'Japan',        1937, 'https://toyota.com'),
    ('Volkswagen',   'Germany',      1937, 'https://vw.com'),
    ('Audi',         'Germany',      1909, 'https://audi.com'),
    ('BMW',          'Germany',      1916, 'https://bmw.com'),
    ('Honda',        'Japan',        1948, 'https://honda.com'),
    ('Mercedes-Benz','Germany',      1926, 'https://mercedes-benz.com'),
    ('Porsche',      'Germany',      1931, 'https://porsche.com'),
    ('Ferrari',      'Italy',        1939, 'https://ferrari.com'),
    ('Lamborghini',  'Italy',        1963, 'https://lamborghini.com'),
    ('McLaren',      'United Kingdom',1963,'https://mclaren.com');

-- Models  (segment must match frontend filter values: Economy | Sport | Luxury | Exotic | SUV)
INSERT INTO Models (manufacturer_id, model_name, body_type, segment) VALUES
    -- Economy
    (1,  'Yaris',          'Hatchback', 'Economy'),
    (1,  'Corolla',        'Sedan',     'Economy'),
    (2,  'Golf',           'Hatchback', 'Economy'),
    (5,  'Civic',          'Sedan',     'Economy'),
    -- Sport
    (4,  'M4',             'Coupe',     'Sport'),
    (7,  '718 Boxster',    'Roadster',  'Sport'),
    (5,  'Civic Type R',   'Hatchback', 'Sport'),
    (1,  'GR86',           'Coupe',     'Sport'),
    -- Luxury
    (6,  'S-Class',        'Sedan',     'Luxury'),
    (3,  'A8',             'Sedan',     'Luxury'),
    (4,  '7 Series',       'Sedan',     'Luxury'),
    (7,  'Panamera',       'Sedan',     'Luxury'),
    (3,  'A4',             'Sedan',     'Luxury'),
    (4,  '320d',           'Sedan',     'Luxury'),
    -- Exotic
    (8,  '488 GTB',        'Coupe',     'Exotic'),
    (9,  'Huracan',        'Coupe',     'Exotic'),
    (10, '720S',           'Coupe',     'Exotic'),
    (8,  'F8 Tributo',     'Coupe',     'Exotic'),
    (9,  'Urus',           'SUV',       'Exotic'),
    -- SUV
    (4,  'X5',             'SUV',       'SUV'),
    (6,  'GLE',            'SUV',       'SUV'),
    (7,  'Cayenne',        'SUV',       'SUV'),
    (3,  'Q8',             'SUV',       'SUV'),
    (1,  'RAV4',           'SUV',       'SUV'),
    (2,  'Tiguan',         'SUV',       'SUV'),
    (5,  'CR-V',           'SUV',       'SUV');

-- Vehicles  (no VehicleImages rows — Wikipedia API fetches images dynamically)
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

-- Customers
INSERT INTO Customers (first_name, last_name, email, phone) VALUES
    ('John',  'Customer', 'john.customer@example.com', '+385911112223'),
    ('Ana',   'Horvat',   'ana.horvat@example.com',    '+385981234567'),
    ('Marko', 'Kovacic',  'marko.kovacic@example.com', '+385991234568');

-- Employees  (hire_date required by schema)
INSERT INTO Employees (first_name, last_name, email, hire_date) VALUES
    ('Ivan',      'Karlo',       'ivan.karlo@example.com',      '2024-01-01'),
    ('Danis',     'Harmandic',   'danis.harmandic@example.com', '2024-01-01'),
    ('Jurica',    'Jamic',       'jurica.jamic@example.com',    '2024-01-01'),
    ('Tomislav',  'Tesija',      'tomislav.tesija@example.com', '2024-01-01'),
    ('Branko',    'Mihaljevic',  'branko.mihaljevic@example.com','2024-01-01');

-- Users  (password = SHA-256 of "password123")
-- SHA-256("password123") = ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
INSERT INTO Users (username, password, role, emp_id) VALUES
    ('ivan',   'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN',    1),
    ('danis',  'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN',    2),
    ('jurica', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN',    3),
    ('tomo',   'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN',    4),
    ('branko', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'EMPLOYEE', 5);

INSERT INTO Users (username, password, role, customer_id) VALUES
    ('john',  'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'CUSTOMER', 1),
    ('ana',   'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'CUSTOMER', 2),
    ('marko', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'CUSTOMER', 3);

-- Roles & Permissions
INSERT INTO Roles (role_name) VALUES ('Admin'), ('Salesperson'), ('Employee');

INSERT INTO Permissions (permission_name) VALUES
    ('Add Vehicle'), ('Update Vehicle'), ('Remove Vehicle'),
    ('Record Sale'), ('View Sales Records'), ('Manage Customers'), ('Manage Reservations');

INSERT INTO EmployeeRoles (emp_id, role_id, assigned_date) VALUES
    (1, 1, '2026-04-01'), (2, 1, '2026-04-01'), (3, 1, '2026-04-01'),
    (4, 1, '2026-04-01'), (5, 3, '2026-04-01');

INSERT INTO RolePermissions (role_id, permission_id) VALUES
    (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),
    (2,1),(2,2),(2,4),(2,5),(2,6),(2,7),
    (3,5);

-- Sales
INSERT INTO Sales (vehicle_id, customer_id, emp_id, sale_price, payment_method) VALUES
    (20, 1, 1, 34000.00, 'Finance');

-- Reservations
INSERT INTO Reservations (vehicle_id, customer_id, emp_id, expiry_date) VALUES
    (5, 2, 1, '2026-05-15');

-- TestDrives
INSERT INTO TestDrives (vehicle_id, customer_id, emp_id, scheduled_date) VALUES
    (3, 3, 1, '2026-04-25 10:00:00'),
    (21, 2, 2, '2026-04-26 14:00:00');

-- MaintenanceHistory
INSERT INTO MaintenanceHistory (vehicle_id, service_date, service_type, description, cost) VALUES
    (3, '2026-03-15', 'Oil Change',        'Regular oil and filter change', 150.00),
    (5, '2026-03-20', 'Brake Service',     'Brake pads replaced',           320.00),
    (20,'2026-02-10', 'Annual Inspection', 'Annual inspection completed',    90.00);

-- VehicleChangeLog
INSERT INTO VehicleChangeLog (vehicle_id, emp_id, change_type, field_changed, new_value) VALUES
    (1, 1, 'INSERT', NULL,     NULL),
    (5, 2, 'UPDATE', 'status', 'Reserved'),
    (20,1, 'UPDATE', 'status', 'Sold'),
    (21,4, 'INSERT', NULL,     NULL);
