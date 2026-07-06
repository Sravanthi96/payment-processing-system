CREATE TABLE payments (
    id              UUID            PRIMARY KEY,
    order_id        VARCHAR(64)     NOT NULL,
    customer_id     VARCHAR(64)     NOT NULL,
    amount          NUMERIC(19, 2)  NOT NULL,
    currency        VARCHAR(3)      NOT NULL,
    status          VARCHAR(20)     NOT NULL,
    transaction_ref VARCHAR(64),
    failure_reason  VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);

-- One payment per order: enforces idempotency for repeated OrderCreated events.
CREATE UNIQUE INDEX ux_payments_order_id ON payments (order_id);
CREATE INDEX ix_payments_customer_id ON payments (customer_id);
CREATE INDEX ix_payments_status ON payments (status);
