CREATE TABLE valuation_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    manager_id BIGINT NULL REFERENCES users(id),

    category_id BIGINT NULL REFERENCES categories(id),
    model_name VARCHAR(255) NOT NULL,
    condition VARCHAR(10) NOT NULL,
    "year" INT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'NEW',

    analog_estimated_price DECIMAL(10,2) NULL,
    analog_matched_count BIGINT NOT NULL DEFAULT 0,

    manager_price DECIMAL(10,2) NULL,
    manager_note TEXT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_valuation_requests_user ON valuation_requests(user_id);
CREATE INDEX idx_valuation_requests_status ON valuation_requests(status);

