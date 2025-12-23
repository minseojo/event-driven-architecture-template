-- 1. 범용 파일 메타데이터 테이블
CREATE TABLE file_records (
    id BIGSERIAL PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(512) NOT NULL, -- 스토리지 내 경로/ID
    content_type VARCHAR(100),         -- MIME type (application/pdf, model/gltf-binary 등)
    size_bytes BIGINT NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Transactional Outbox 테이블
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL, -- 예: "FILE"
    aggregate_id VARCHAR(50) NOT NULL,   -- file_records.id
    event_type VARCHAR(100) NOT NULL,    -- 예: "FILE_UPLOADED"
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    processed BOOLEAN DEFAULT FALSE
);

-- Polling 성능 최적화 인덱스
CREATE INDEX idx_outbox_unprocessed ON outbox_events (created_at) WHERE processed = FALSE;

