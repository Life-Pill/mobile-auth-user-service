-- V2__Add_OAuth_Fields.sql
-- Add OAuth provider fields to users table for social login support

ALTER TABLE users
ADD COLUMN auth_provider VARCHAR(50) DEFAULT 'local',
ADD COLUMN provider_id VARCHAR(255),
ADD COLUMN profile_picture_url VARCHAR(500);

-- Create index for provider lookup
CREATE INDEX idx_users_auth_provider ON users(auth_provider);
CREATE INDEX idx_users_provider_id ON users(provider_id);

-- Update existing users to have 'local' as auth provider
UPDATE users SET auth_provider = 'local' WHERE auth_provider IS NULL;
