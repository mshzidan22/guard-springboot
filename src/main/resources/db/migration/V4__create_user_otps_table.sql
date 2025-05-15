CREATE TABLE user_otps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    otp_type ENUM('EMAIL', 'PHONE') NOT NULL,
    status ENUM('PENDING', 'VERIFIED', 'EXPIRED', 'FAILED') NOT NULL,
    verification_attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NULL
);