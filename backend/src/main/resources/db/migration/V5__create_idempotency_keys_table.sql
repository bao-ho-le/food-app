CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id         INT NOT NULL,
    scope           VARCHAR(100) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    response_status INT NULL,
    response_body   MEDIUMTEXT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_idempotency_created_at (created_at),
    INDEX idx_idempotency_user_id (user_id)
);