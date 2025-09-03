-- Clean up existing data
DELETE FROM contact;
DELETE FROM user;

-- Insert test users
INSERT INTO user (id, name, email, password, role, enabled)
VALUES 
('test-user-1', 'Test User 1', 'test1@example.com', '$2a$10$xyz...', 'ROLE_USER', true),
('test-user-2', 'Test User 2', 'test2@example.com', '$2a$10$xyz...', 'ROLE_USER', true);

-- Insert test contacts
INSERT INTO contact (id, name, email, phone, relationship, favorite, user_id)
VALUES 
(1, 'Contact 1', 'contact1@example.com', '1234567890', 'FRIEND', true, 'test-user-1'),
(2, 'Contact 2', 'contact2@example.com', '0987654321', 'FAMILY', false, 'test-user-1'),
(3, 'Contact 3', 'contact3@example.com', '1112223333', 'WORK', true, 'test-user-2');
