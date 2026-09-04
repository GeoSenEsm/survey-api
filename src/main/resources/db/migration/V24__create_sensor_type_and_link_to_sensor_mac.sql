-- Lookup of supported sensor device kinds. Codes are stable API values;
-- names are admin-facing labels. Only 'manual' is seeded here: it is a permanent, always-present
-- type (respondent-entered readings, no physical device). Every other sensor type — including
-- ones that used to be pre-seeded here (Xiaomi, Kestrel) — is created on demand instead, either
-- by installing a built-in template (SensorProfileTemplateCatalog) or defining a custom type.
CREATE TABLE sensor_type (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    code NVARCHAR(32) NOT NULL,
    name NVARCHAR(64) NOT NULL,
    row_version TIMESTAMP NOT NULL,
    CONSTRAINT UQ_sensor_type_code UNIQUE (code),
    CONSTRAINT UQ_sensor_type_name UNIQUE (name)
);
GO

INSERT INTO sensor_type (id, code, name) VALUES
    ('A1000000-0000-4000-8000-000000000003', N'manual', N'Manual');
GO

ALTER TABLE sensor_mac
    ADD sensor_type_id UNIQUEIDENTIFIER NOT NULL
        CONSTRAINT FK_sensor_mac_sensor_type
        REFERENCES sensor_type(id);
GO

CREATE INDEX IX_sensor_mac_sensor_type_id
    ON sensor_mac (sensor_type_id);
GO
