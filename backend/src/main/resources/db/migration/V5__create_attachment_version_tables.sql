CREATE TABLE IF NOT EXISTS document_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(512) NULL,
    status VARCHAR(32) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_document_versions_document_version UNIQUE (document_id, version_no),
    INDEX idx_document_versions_document_id (document_id),
    CONSTRAINT fk_document_versions_document FOREIGN KEY (document_id) REFERENCES documents (id),
    CONSTRAINT fk_document_versions_user FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(512) NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_attachments_document_deleted (document_id, deleted),
    CONSTRAINT fk_attachments_document FOREIGN KEY (document_id) REFERENCES documents (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
