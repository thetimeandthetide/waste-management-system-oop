DROP DATABASE IF EXISTS waste_management_db;
CREATE DATABASE waste_management_db;

USE waste_management_db;

CREATE TABLE IF NOT EXISTS waste (
    id INT AUTO_INCREMENT PRIMARY KEY,
    waste_type VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity DOUBLE NOT NULL,
    location VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL
);

INSERT INTO waste (waste_type, description, quantity, location, status)
VALUES
('Organic', 'Food waste from cafeteria', 12.5, 'College Canteen', 'Pending'),
('Recyclable', 'Plastic bottles', 8.0, 'Block A', 'Pending'),
('Hazardous', 'Used batteries', 3.0, 'Lab Room 2', 'Collected');
