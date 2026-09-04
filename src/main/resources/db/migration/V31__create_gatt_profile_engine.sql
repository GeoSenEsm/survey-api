-- Drives how the mobile app talks to a device: `profile` types are generic BLE devices driven
-- entirely by a published sensor_gatt_profile spec (no app-side code per device); `native` types
-- have a hand-written Dart adapter keyed by adapter_key; `manual`/`none` have no BLE integration.
ALTER TABLE sensor_type
    ADD integration_mode NVARCHAR(32) NOT NULL
            CONSTRAINT DF_sensor_type_integration_mode DEFAULT N'native',
        adapter_key NVARCHAR(64) NULL;
GO

UPDATE sensor_type
SET integration_mode = CASE code
    WHEN N'manual' THEN N'manual'
    WHEN N'none' THEN N'none'
    ELSE N'native'
END;
GO

ALTER TABLE sensor_type ADD CONSTRAINT CK_sensor_type_integration_mode
    CHECK (integration_mode IN (N'profile', N'native', N'manual', N'none'));
GO

-- The generic BLE "GATT profile engine": one row per (sensor_type_id, revision). Built-in
-- templates and custom sensor types both publish here once their sensor_type row is created (on
-- demand) — nothing is pre-seeded, so a fresh database's profile catalog starts empty.
CREATE TABLE sensor_gatt_profile (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    sensor_type_id UNIQUEIDENTIFIER NOT NULL,
    revision INT NOT NULL,
    status NVARCHAR(16) NOT NULL,
    schema_version INT NOT NULL,
    spec_json NVARCHAR(MAX) NOT NULL,
    spec_hash CHAR(64) NOT NULL,
    min_engine_version NVARCHAR(32) NOT NULL,
    read_only BIT NOT NULL DEFAULT 0,
    created_at DATETIMEOFFSET(0) NOT NULL DEFAULT sysutcdatetime(),
    updated_at DATETIMEOFFSET(0) NOT NULL DEFAULT sysutcdatetime(),
    published_at DATETIMEOFFSET(0) NULL,
    row_version TIMESTAMP NOT NULL,
    CONSTRAINT FK_sensor_gatt_profile_sensor_type
        FOREIGN KEY (sensor_type_id) REFERENCES sensor_type(id) ON DELETE CASCADE,
    CONSTRAINT UQ_sensor_gatt_profile_revision UNIQUE (sensor_type_id, revision),
    CONSTRAINT CK_sensor_gatt_profile_revision CHECK (revision > 0),
    CONSTRAINT CK_sensor_gatt_profile_schema_version CHECK (schema_version = 1),
    CONSTRAINT CK_sensor_gatt_profile_status CHECK (status IN (N'draft', N'published', N'archived')),
    CONSTRAINT CK_sensor_gatt_profile_json CHECK (ISJSON(spec_json) = 1),
    CONSTRAINT CK_sensor_gatt_profile_published_at CHECK (
        (status = N'published' AND published_at IS NOT NULL)
        OR (status <> N'published' AND published_at IS NULL))
);
GO

CREATE UNIQUE INDEX UQ_sensor_gatt_profile_published
    ON sensor_gatt_profile(sensor_type_id) WHERE status = N'published';
GO
CREATE INDEX IX_sensor_gatt_profile_sensor_type_revision
    ON sensor_gatt_profile(sensor_type_id, revision DESC);
GO
