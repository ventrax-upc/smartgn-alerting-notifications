CREATE SCHEMA IF NOT EXISTS alerting;
SET search_path TO alerting;

CREATE TABLE alert_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    metric VARCHAR(30) NOT NULL,
    operator VARCHAR(40) NOT NULL,
    threshold NUMERIC(18,4),
    severity VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL,
    version INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_alert_rule_name UNIQUE (name)
);

CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(128) NOT NULL,
    device_id VARCHAR(128) NOT NULL,
    property_id UUID NOT NULL,
    supply_point_id UUID NOT NULL,
    account_id UUID NOT NULL,
    rule_id BIGINT NOT NULL,
    rule_version INTEGER NOT NULL,
    rule_name VARCHAR(120) NOT NULL,
    metric VARCHAR(30) NOT NULL,
    operator VARCHAR(40) NOT NULL,
    threshold NUMERIC(18,4),
    measured_value NUMERIC(18,4),
    condition VARCHAR(500) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    instructions TEXT NOT NULL,
    measured_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    acknowledged_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_alert_device_event_rule UNIQUE (device_id, event_id, rule_id)
);

CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    account_id UUID NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(320) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_notification_preference_account_type UNIQUE (account_id, notification_type)
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT,
    source_event_id VARCHAR(128),
    account_id UUID NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    operational_event_type VARCHAR(40),
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(320) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL,
    next_attempt_at TIMESTAMPTZ,
    provider_message_id VARCHAR(200),
    last_error VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_notification_alert_channel_recipient UNIQUE (alert_id, channel, recipient),
    CONSTRAINT uk_notification_source_channel_recipient UNIQUE (source_event_id, channel, recipient)
);

CREATE TABLE notification_attempts (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    attempt_number INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider_message_id VARCHAR(200),
    error_code VARCHAR(80),
    error_message VARCHAR(500),
    attempted_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_notification_attempt_number UNIQUE (notification_id, attempt_number)
);
