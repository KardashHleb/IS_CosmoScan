CREATE TABLE works (
    id UUID PRIMARY KEY,
    student_full_name VARCHAR(255) NOT NULL,
    submitted_at TIMESTAMPTZ NOT NULL,
    original_file_name VARCHAR(512) NOT NULL,
    file_id UUID NOT NULL,
    content_type VARCHAR(255),
    file_size_bytes BIGINT NOT NULL
);
