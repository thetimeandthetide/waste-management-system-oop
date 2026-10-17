-- Non-destructive schema setup. Existing databases and records are preserved.
CREATE DATABASE IF NOT EXISTS waste_management_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE waste_management_db;

CREATE TABLE IF NOT EXISTS app_users (
    username VARCHAR(40) PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL,
    assigned_area VARCHAR(100),
    role VARCHAR(10) NOT NULL,
    password_salt CHAR(32) NOT NULL,
    password_hash CHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS waste (
    id INT AUTO_INCREMENT PRIMARY KEY,
    waste_type VARCHAR(30) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    location VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_by VARCHAR(40),
    collected_by VARCHAR(40),
    collected_at DATETIME NOT NULL,
    invalid_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_waste_report (status, collected_at),
    INDEX idx_waste_collector (collected_by, status),
    INDEX idx_waste_area (location, status)
);

CREATE TABLE IF NOT EXISTS announcements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    message TEXT NOT NULL,
    created_by VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcement_author FOREIGN KEY (created_by)
        REFERENCES app_users(username)
);

CREATE TABLE IF NOT EXISTS announcement_reads (
    announcement_id INT NOT NULL,
    username VARCHAR(40) NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (announcement_id, username),
    CONSTRAINT fk_announcement_read_announcement FOREIGN KEY (announcement_id)
        REFERENCES announcements(id) ON DELETE CASCADE,
    CONSTRAINT fk_announcement_read_user FOREIGN KEY (username)
        REFERENCES app_users(username) ON DELETE CASCADE
);

-- Application startup adds missing columns to an older waste table and seeds
-- the administrator/member accounts without replacing existing records.
