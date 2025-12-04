-- V3__Seed_User_Auth_Data.sql
-- Seed data for User Authentication Service (Mobile App Users)
-- Password is BCrypt hash of 'password123' for all users

-- Insert mobile app users
INSERT INTO users (id, email, password_hash, first_name, last_name, phone_number, date_of_birth, email_verified, created_at) VALUES
-- Verified users with complete profiles
('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'saman.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Saman', 'Perera', '0771234501', '1989-05-15', true, '2024-01-15 10:30:00'),
('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'nimali.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Nimali', 'Silva', '0772234502', '1990-08-22', true, '2024-01-20 14:45:00'),
('c3d4e5f6-a7b8-9012-cdef-123456789012', 'kamal.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Kamal', 'Fernando', '0773234503', '1985-12-10', true, '2024-02-01 09:15:00'),
('d4e5f6a7-b8c9-0123-defa-234567890123', 'shamila.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Shamila', 'Rajapaksa', '0774234504', '1992-03-25', true, '2024-02-15 11:30:00'),
('e5f6a7b8-c9d0-1234-efab-345678901234', 'chandana.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Chandana', 'Pushpakumara', '0715234505', '1988-07-18', true, '2024-03-01 08:00:00'),

-- More verified users
('f6a7b8c9-d0e1-2345-fabc-456789012345', 'ruwani.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Ruwani', 'Bandara', '0716234506', '1991-11-05', true, '2024-03-10 15:20:00'),
('a7b8c9d0-e1f2-3456-abcd-567890123456', 'arjuna.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Arjuna', 'Wimalasuriya', '0717234507', '1987-04-30', true, '2024-03-20 10:45:00'),
('b8c9d0e1-f2a3-4567-bcde-678901234567', 'priyantha.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Priyantha', 'Wickramasinghe', '0768234508', '1986-09-08', true, '2024-04-01 13:00:00'),
('c9d0e1f2-a3b4-5678-cdef-789012345678', 'shanika.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Shanika', 'Dilrukshi', '0769234509', '1993-02-14', true, '2024-04-10 16:30:00'),
('d0e1f2a3-b4c5-6789-defa-890123456789', 'mahesh.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Mahesh', 'Sanjeewa', '0760234510', '1990-06-20', true, '2024-04-20 09:00:00'),

-- Users with pending email verification
('e1f2a3b4-c5d6-7890-efab-901234567890', 'dilshan.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Dilshan', 'Jayawardena', '0751234511', '1991-08-25', false, '2024-05-01 11:15:00'),
('f2a3b4c5-d6e7-8901-fabc-012345678901', 'nayomi.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Nayomi', 'Fernando', '0752234512', '1994-04-12', false, '2024-05-05 14:00:00'),

-- More verified users from different regions
('a3b4c5d6-e7f8-9012-abcd-123456789012', 'raj.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Raj', 'Kumar', '0784234514', '1984-01-30', true, '2024-05-10 10:30:00'),
('b4c5d6e7-f8a9-0123-bcde-234567890123', 'priya.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Priya', 'Shankar', '0785234515', '1995-10-15', true, '2024-05-15 08:45:00'),
('c5d6e7f8-a9b0-1234-cdef-345678901234', 'malith.mobile@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyu.z..jkPa.8LVi3G.pJqR25KuBp2B2Ki', 'Malith', 'Dissanayake', '0796234516', '1993-12-05', true, '2024-05-20 12:00:00');

-- Insert user addresses
INSERT INTO user_addresses (id, user_id, street, city, state, zip_code, country, is_primary) VALUES
-- Saman Perera addresses
('11111111-1111-1111-1111-111111111111', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', '45 Park Lane', 'Colombo 03', 'Western', '00300', 'Sri Lanka', true),
('11111111-1111-1111-1111-111111111112', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', '123 Office Tower', 'Colombo 01', 'Western', '00100', 'Sri Lanka', false),

-- Nimali Silva addresses
('22222222-2222-2222-2222-222222222221', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', '78 Galle Road', 'Dehiwala', 'Western', '10350', 'Sri Lanka', true),

-- Kamal Fernando addresses
('33333333-3333-3333-3333-333333333331', 'c3d4e5f6-a7b8-9012-cdef-123456789012', '23 High Level Road', 'Nugegoda', 'Western', '10250', 'Sri Lanka', true),

-- Chandana Pushpakumara addresses
('44444444-4444-4444-4444-444444444441', 'e5f6a7b8-c9d0-1234-efab-345678901234', '67 Dalada Veediya', 'Kandy', 'Central', '20000', 'Sri Lanka', true),
('44444444-4444-4444-4444-444444444442', 'e5f6a7b8-c9d0-1234-efab-345678901234', '12 Peradeniya Road', 'Kandy', 'Central', '20000', 'Sri Lanka', false),

-- Priyantha Wickramasinghe addresses
('55555555-5555-5555-5555-555555555551', 'b8c9d0e1-f2a3-4567-bcde-678901234567', '12 Church Street', 'Galle Fort', 'Southern', '80000', 'Sri Lanka', true),

-- Raj Kumar addresses
('66666666-6666-6666-6666-666666666661', 'a3b4c5d6-e7f8-9012-abcd-123456789012', '34 Hospital Road', 'Jaffna', 'Northern', '40000', 'Sri Lanka', true),

-- Dilshan Jayawardena addresses
('77777777-7777-7777-7777-777777777771', 'e1f2a3b4-c5d6-7890-efab-901234567890', '90 Lewis Place', 'Negombo', 'Western', '11500', 'Sri Lanka', true),

-- Malith Dissanayake addresses
('88888888-8888-8888-8888-888888888881', 'c5d6e7f8-a9b0-1234-cdef-345678901234', '12 Colombo Road', 'Kurunegala', 'North Western', '60000', 'Sri Lanka', true);

-- Note: Password for all users is 'password123'
-- Total: 15 mobile app users, 10 user addresses
