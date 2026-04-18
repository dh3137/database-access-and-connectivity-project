USE car_dealership;

INSERT IGNORE INTO cars (make, model, year, price)
VALUES
    ('Toyota', 'Corolla', 2021, 18500.00),
    ('Volkswagen', 'Golf', 2020, 17200.00),
    ('Audi', 'A4', 2022, 35000.00),
    ('BMW', '320d', 2022, 31500.00);

-- Passwords are SHA-256 of "password123"
-- SHA-256("password123") = ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
INSERT IGNORE INTO users (username, password, role, full_name)
VALUES
    ('ivan',      'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN',    'Ivan Karlo'),
    ('danis',     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN',    'Danis Harmandić'),
    ('jurica',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN',    'Jurica Jamić'),
    ('tomo',      'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN',    'Tomislav Tešija'),
    ('branko',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN',    'Branko Mihaljević'),
    ('employee1', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'EMPLOYEE', 'Jane Employee'),
    ('customer1', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'CUSTOMER', 'John Customer');
