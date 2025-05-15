CREATE TABLE IF NOT EXISTS auth_providers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL, -- "LOCAL", "GOOGLE", "FACEBOOK", etc.
    provider_user_id VARCHAR(255), -- user id from the provider (for OAuth)
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
