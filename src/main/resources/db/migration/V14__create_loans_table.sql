CREATE TABLE IF NOT EXISTS loans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    manager_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    contract_number VARCHAR(64) NOT NULL,
    loan_amount NUMERIC(19,2) NOT NULL,
    repay_amount NUMERIC(19,2) NOT NULL,
    interest_rate_daily NUMERIC(10,4) NOT NULL,
    collateral_description VARCHAR(1000) NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    payment_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    notes VARCHAR(2000),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_loans_user_id ON loans(user_id);
CREATE INDEX IF NOT EXISTS idx_loans_manager_id ON loans(manager_id);
