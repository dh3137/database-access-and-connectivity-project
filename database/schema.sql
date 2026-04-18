CREATE DATABASE IF NOT EXISTS car_dealership;
USE car_dealership;

CREATE TABLE cars (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    make        VARCHAR(50)  NOT NULL,
    model       VARCHAR(50)  NOT NULL,
    year        INT          NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    status      ENUM('AVAILABLE','SOLD','RESERVED') NOT NULL DEFAULT 'AVAILABLE',
    color       VARCHAR(30),
    mileage     INT,
    image_url   VARCHAR(500),
    description TEXT
);

CREATE TABLE users (
    id        INT PRIMARY KEY AUTO_INCREMENT,
    username  VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    role      ENUM('ADMIN','EMPLOYEE','CUSTOMER') NOT NULL,
    full_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS action_log (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(50)  NOT NULL,
    action     VARCHAR(100) NOT NULL,
    detail     VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
