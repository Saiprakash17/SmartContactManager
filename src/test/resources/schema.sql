DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS contact CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;

CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL UNIQUE,
    user_password VARCHAR(255) NOT NULL,
    image_url VARCHAR(1000),
    about VARCHAR(1000),
    phone_number VARCHAR(100),
    enabled BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    provider VARCHAR(50) DEFAULT 'SELF',
    provider_user_id VARCHAR(255),
    user_verify_token VARCHAR(1000),
    cloudinary_image_public_id VARCHAR(255)
);

CREATE TABLE user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255),
    role VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE contact (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(255),
    relationship VARCHAR(50),
    favorite BOOLEAN DEFAULT FALSE,
    user_id VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);