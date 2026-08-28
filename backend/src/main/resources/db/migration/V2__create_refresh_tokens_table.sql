DROP TABLE IF EXISTS refresh_tokens;

CREATE TABLE refresh_tokens (
    id BINARY(16) NOT NULL PRIMARY KEY,
    jti VARCHAR(36) NOT NULL,
    user_id INT NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uq_refresh_tokens_jti UNIQUE (jti),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_refresh_tokens_user_id (user_id),
    INDEX idx_refresh_tokens_expires_at (expires_at)
);
