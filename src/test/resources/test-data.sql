-- Clean up existing data
DELETE FROM contact;
DELETE FROM user_roles;
DELETE FROM users;

-- Insert test users
INSERT INTO users (id, user_name, user_email, user_password, provider, enabled, email_verified)
VALUES 
('test-user-1', 'Test User', 'test@example.com', '$2a$10$O3sOaRXK7qN1W4p.E3A7e.0Sq.YME4SCYXm4V69QYxZOF2PaQxS3y', 'SELF', true, true),
('test-user-2', 'Test User 2', 'test2@example.com', '$2a$10$xyz...', 'SELF', true, true);

-- Insert user roles
INSERT INTO user_roles (user_id, role)
VALUES
('test-user-1', 'ROLE_USER'),
('test-user-2', 'ROLE_USER');

-- Insert test contacts
INSERT INTO contact (id, name, email, phone, relationship, favorite, user_id)
VALUES 
(1, 'Contact 1', 'contact1@example.com', '1234567890', 'FRIEND', true, 'test-user-1'),
(2, 'Contact 2', 'contact2@example.com', '0987654321', 'FAMILY', false, 'test-user-1'),
(3, 'Contact 3', 'contact3@example.com', '1112223333', 'WORK', true, 'test-user-2');
