CREATE TABLE telemetry_receipt (
    device_id varchar(128) NOT NULL,
    event_id varchar(128) NOT NULL,
    PRIMARY KEY (device_id, event_id)
);

CREATE TABLE telemetry_watermark (
    device_id varchar(128) PRIMARY KEY,
    measured_at timestamp with time zone NOT NULL
);
