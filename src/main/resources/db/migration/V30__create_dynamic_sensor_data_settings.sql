CREATE TABLE survey_sensor_settings (
    id INT NOT NULL PRIMARY KEY,
    mode NVARCHAR(32) NOT NULL,
    CONSTRAINT CK_survey_sensor_settings_singleton CHECK (id = 1),
    CONSTRAINT CK_survey_sensor_settings_mode CHECK (mode IN (N'no_sensor_data', N'configured_sensors'))
);
GO

INSERT INTO survey_sensor_settings (id, mode)
VALUES (1, N'no_sensor_data');
GO

CREATE TABLE sensor_type_setting (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    sensor_type_id UNIQUEIDENTIFIER NOT NULL,
    enabled BIT NOT NULL DEFAULT 0,
    connection_timeout_seconds INT NOT NULL DEFAULT 30,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT FK_sensor_type_setting_sensor_type
        FOREIGN KEY (sensor_type_id) REFERENCES sensor_type(id),
    CONSTRAINT UQ_sensor_type_setting_sensor_type UNIQUE (sensor_type_id),
    CONSTRAINT CK_sensor_type_setting_timeout CHECK (connection_timeout_seconds > 0)
);
GO

INSERT INTO sensor_type_setting (sensor_type_id, enabled, connection_timeout_seconds, display_order)
SELECT id,
       CASE WHEN code = N'manual' THEN 1 ELSE 0 END,
       30,
       CASE code WHEN N'manual' THEN 1 ELSE 99 END
FROM sensor_type
WHERE code IN (N'manual', N'none');
GO

-- "Used sensor data": the admin-curated, globally-unique columns actually collected/exported.
-- No per-parameter `required`/`active` flag — every parameter is unconditionally collectible and
-- there is no soft-hide, only DELETE. Nothing is seeded here: a row only starts existing once a
-- sensor integration that actually produces it is installed (SensorProfileTemplateServiceImpl) or
-- an admin defines a custom one, so the list is never cluttered with parameters no active
-- integration backs. `(name, unit)` is the parameter's identity, not `name` alone — two
-- definitions may share a name only if their units differ.
CREATE TABLE sensor_parameter_definition (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    code NVARCHAR(64) NOT NULL,
    name NVARCHAR(128) NOT NULL,
    data_type NVARCHAR(32) NOT NULL,
    unit NVARCHAR(32) NULL,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT UQ_sensor_parameter_definition_code UNIQUE (code),
    CONSTRAINT UQ_sensor_parameter_definition_name_unit UNIQUE (name, unit)
);
GO

-- A sensor type's own raw parameter catalog: what that sensor type can possibly produce,
-- independent of whether it has been promoted ("used") yet. `(name, unit)` is not unique here —
-- the same reading can appear under several sensor types with no conflict. No `priority_order`:
-- every source that reports a value is kept as its own independent reading (see sensor_data's
-- per-source rows) — there is no "winning" source to rank.
CREATE TABLE sensor_type_parameter (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    sensor_type_id UNIQUEIDENTIFIER NOT NULL,
    code NVARCHAR(64) NOT NULL,
    name NVARCHAR(128) NOT NULL,
    data_type NVARCHAR(32) NOT NULL,
    unit NVARCHAR(32) NULL,
    used_parameter_id UNIQUEIDENTIFIER NULL,
    CONSTRAINT FK_sensor_type_parameter_sensor_type
        FOREIGN KEY (sensor_type_id) REFERENCES sensor_type(id) ON DELETE CASCADE,
    CONSTRAINT FK_sensor_type_parameter_used_parameter
        FOREIGN KEY (used_parameter_id) REFERENCES sensor_parameter_definition(id) ON DELETE SET NULL,
    CONSTRAINT UQ_sensor_type_parameter_type_code UNIQUE (sensor_type_id, code),
    CONSTRAINT CK_sensor_type_parameter_data_type CHECK (data_type IN (N'decimal', N'integer', N'boolean', N'text'))
);
GO

CREATE INDEX IX_sensor_type_parameter_used_parameter
    ON sensor_type_parameter (used_parameter_id);
GO

-- Which physical sensor type (and, where applicable, which sensor_mac) a respondent has. Presence
-- is the only signal — no enabled/priority state: the mobile app always attempts every one of a
-- respondent's assigned sensors, keeping every source's own reading independently.
CREATE TABLE respondent_sensor_assignment (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    respondent_id UNIQUEIDENTIFIER NOT NULL,
    sensor_type_id UNIQUEIDENTIFIER NOT NULL,
    sensor_mac_id UNIQUEIDENTIFIER NULL,
    CONSTRAINT FK_respondent_sensor_assignment_respondent
        FOREIGN KEY (respondent_id) REFERENCES identity_user(id) ON DELETE CASCADE,
    CONSTRAINT FK_respondent_sensor_assignment_sensor_type
        FOREIGN KEY (sensor_type_id) REFERENCES sensor_type(id),
    CONSTRAINT FK_respondent_sensor_assignment_sensor_mac
        FOREIGN KEY (sensor_mac_id) REFERENCES sensor_mac(id) ON DELETE SET NULL
);
GO

CREATE INDEX IX_respondent_sensor_assignment_respondent
    ON respondent_sensor_assignment (respondent_id);
GO
CREATE UNIQUE INDEX UQ_respondent_sensor_assignment_type
    ON respondent_sensor_assignment (respondent_id, sensor_type_id);
GO
CREATE UNIQUE INDEX UQ_respondent_sensor_assignment_sensor
    ON respondent_sensor_assignment (sensor_mac_id) WHERE sensor_mac_id IS NOT NULL;
GO

INSERT INTO respondent_sensor_assignment (respondent_id, sensor_type_id, sensor_mac_id)
SELECT sm.respondent_id, sm.sensor_type_id, sm.id
FROM sensor_mac sm
WHERE sm.respondent_id IS NOT NULL;
GO

ALTER TABLE sensor_data
    ADD source_sensor_type_id UNIQUEIDENTIFIER NULL,
        source NVARCHAR(32) NULL;
GO

ALTER TABLE sensor_data
    ADD CONSTRAINT FK_sensor_data_source_sensor_type
        FOREIGN KEY (source_sensor_type_id) REFERENCES sensor_type(id);
GO

CREATE INDEX IX_sensor_data_source_sensor_type
    ON sensor_data (source_sensor_type_id);
GO

-- Recreate index without the dropped columns before removing them
DROP INDEX IX_sensor_data_date_time ON sensor_data;
GO

CREATE NONCLUSTERED INDEX IX_sensor_data_date_time
ON [dbo].[sensor_data] ([date_time] ASC)
INCLUDE ([respondent_id], [survey_participation_id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF,
      DROP_EXISTING = OFF, ONLINE = OFF,
      ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON);
GO

ALTER TABLE sensor_data DROP COLUMN temperature;
GO

ALTER TABLE sensor_data DROP COLUMN humidity;
GO

CREATE TABLE sensor_data_parameter_value (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    sensor_data_id UNIQUEIDENTIFIER NOT NULL,
    parameter_definition_id UNIQUEIDENTIFIER NOT NULL,
    value NVARCHAR(256) NOT NULL,
    CONSTRAINT FK_sensor_data_parameter_value_sensor_data
        FOREIGN KEY (sensor_data_id) REFERENCES sensor_data(id) ON DELETE CASCADE,
    CONSTRAINT FK_sensor_data_parameter_value_parameter
        FOREIGN KEY (parameter_definition_id) REFERENCES sensor_parameter_definition(id),
    CONSTRAINT UQ_sensor_data_parameter_value UNIQUE (sensor_data_id, parameter_definition_id)
);
GO

CREATE INDEX IX_sensor_data_parameter_value_sensor_data
    ON sensor_data_parameter_value (sensor_data_id);
GO

CREATE INDEX IX_sensor_data_parameter_value_parameter
    ON sensor_data_parameter_value (parameter_definition_id);
GO
