CREATE TABLE IF NOT EXISTS app_metadata (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    metadata_key VARCHAR(128) NOT NULL,
    metadata_value VARCHAR(512) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_app_metadata_key UNIQUE (metadata_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO app_metadata (metadata_key, metadata_value)
VALUES ('schema_stage', 'stage-0-foundation')
ON DUPLICATE KEY UPDATE metadata_value = VALUES(metadata_value);
