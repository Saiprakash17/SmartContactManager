-- Fix: Enclose the reserved table name "user" in double quotes.
DROP TABLE IF EXISTS "user" CASCADE;
DROP TABLE IF EXISTS contact CASCADE;
DROP TABLE IF EXISTS authorities CASCADE;

CREATE TABLE "user" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TABLE contact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(255),
    is_favorite BOOLEAN DEFAULT FALSE,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE TABLE authorities (
    username VARCHAR(255) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    FOREIGN KEY (username) REFERENCES "user"(username) ON DELETE CASCADE
);