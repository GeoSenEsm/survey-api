-- Lookup of supported sensor device kinds. Codes are stable API values;
-- names are admin-facing labels. Existing sensor_mac rows default to Xiaomi.
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
    ('A1000000-0000-4000-8000-000000000001', N'xiaomi', N'Xiaomi'),
    ('A1000000-0000-4000-8000-000000000002', N'kestrel', N'Kestrel'),
    ('A1000000-0000-4000-8000-000000000003', N'manual', N'Manual');
GO

ALTER TABLE sensor_mac
    ADD sensor_type_id UNIQUEIDENTIFIER NULL;
GO

UPDATE sensor_mac
SET sensor_type_id = 'A1000000-0000-4000-8000-000000000001'
WHERE sensor_type_id IS NULL;
GO

ALTER TABLE sensor_mac
    ALTER COLUMN sensor_type_id UNIQUEIDENTIFIER NOT NULL;
GO

ALTER TABLE sensor_mac
    ADD CONSTRAINT FK_sensor_mac_sensor_type
        FOREIGN KEY (sensor_type_id) REFERENCES sensor_type(id);
GO

CREATE INDEX IX_sensor_mac_sensor_type_id
    ON sensor_mac (sensor_type_id);
GO
