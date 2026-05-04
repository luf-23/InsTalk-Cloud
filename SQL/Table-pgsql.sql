-- PostgreSQL schema for ai-service
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS user_ai_config (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    robot_id BIGINT NOT NULL,

    system_prompt TEXT,
    model VARCHAR(50) DEFAULT 'deepseek-v3',
    temperature DECIMAL(3,2) DEFAULT 0.70,
    max_tokens INT DEFAULT 2000,
    top_p DECIMAL(3,2) DEFAULT 1.00,
    presence_penalty DECIMAL(3,2) DEFAULT 0.00,
    seed INT DEFAULT 1234,

    daily_message_limit INT DEFAULT 100,
    daily_message_count INT DEFAULT 0,
    last_reset_date DATE,

    total_messages INT DEFAULT 0,
    total_tokens_used BIGINT DEFAULT 0,
    last_used_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id),
    UNIQUE (robot_id)
);

CREATE TABLE IF NOT EXISTS ai_chat_summary (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    robot_id BIGINT NOT NULL,
    summary TEXT NOT NULL,
    last_message_id BIGINT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, robot_id)
);

CREATE TABLE IF NOT EXISTS ai_memory (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    robot_id BIGINT NOT NULL,
    type VARCHAR(16) DEFAULT 'FACT',
    content TEXT NOT NULL,
    embedding_json JSONB NULL,
    embedding VECTOR(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_memory_user_robot ON ai_memory(user_id, robot_id);
CREATE INDEX IF NOT EXISTS idx_ai_memory_embedding ON ai_memory USING ivfflat (embedding vector_cosine_ops);
