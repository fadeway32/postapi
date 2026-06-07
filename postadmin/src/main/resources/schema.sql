CREATE TABLE IF NOT EXISTS pa_tenant (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS pa_admin_user (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (tenant_id, username)
);

CREATE TABLE IF NOT EXISTS pa_api_group (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS pa_api_definition (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    group_id BIGINT,
    api_code VARCHAR(128) NOT NULL,
    version VARCHAR(32) NOT NULL DEFAULT 'v1',
    api_name VARCHAR(128) NOT NULL,
    method VARCHAR(16) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    headers_json CLOB,
    query_json CLOB,
    body_type VARCHAR(32) NOT NULL DEFAULT 'NONE',
    body_template CLOB,
    script_text CLOB,
    encrypted_secret_json CLOB,
    timeout_millis BIGINT,
    enabled TINYINT NOT NULL DEFAULT 1,
    call_count BIGINT NOT NULL DEFAULT 0,
    success_count BIGINT NOT NULL DEFAULT 0,
    failure_count BIGINT NOT NULL DEFAULT 0,
    last_call_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (tenant_id, api_code, version)
);

CREATE INDEX IF NOT EXISTS idx_pa_api_definition_tenant_code ON pa_api_definition (tenant_id, api_code);

CREATE TABLE IF NOT EXISTS pa_api_call_log (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    api_id BIGINT,
    api_code VARCHAR(128) NOT NULL,
    api_name VARCHAR(128) NOT NULL,
    batch_id VARCHAR(64),
    request_payload CLOB,
    response_status INT,
    response_body CLOB,
    success TINYINT NOT NULL,
    error_message CLOB,
    elapsed_millis BIGINT NOT NULL DEFAULT 0,
    trace_id VARCHAR(128),
    detail_json CLOB,
    called_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pa_api_log_tenant_called ON pa_api_call_log (tenant_id, called_at);
CREATE INDEX IF NOT EXISTS idx_pa_api_log_tenant_api ON pa_api_call_log (tenant_id, api_code);
