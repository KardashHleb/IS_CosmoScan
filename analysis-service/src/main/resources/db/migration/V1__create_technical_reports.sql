CREATE TABLE technical_reports (
    id UUID PRIMARY KEY,
    work_id UUID NOT NULL,
    file_id UUID NOT NULL,
    checked_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL,
    report_file_path VARCHAR(1024)
);

CREATE TABLE technical_report_remarks (
    report_id UUID NOT NULL REFERENCES technical_reports (id) ON DELETE CASCADE,
    remark VARCHAR(1024) NOT NULL
);

CREATE INDEX idx_technical_reports_work_id ON technical_reports (work_id);

CREATE TABLE work_word_clouds (
    id UUID PRIMARY KEY,
    work_id UUID NOT NULL UNIQUE,
    file_id UUID NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL
);

CREATE TABLE work_word_cloud_terms (
    word_cloud_id UUID NOT NULL REFERENCES work_word_clouds (id) ON DELETE CASCADE,
    term_order INTEGER NOT NULL,
    term VARCHAR(256) NOT NULL,
    weight INTEGER NOT NULL
);

CREATE INDEX idx_work_word_clouds_work_id ON work_word_clouds (work_id);
