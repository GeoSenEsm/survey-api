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
       CASE WHEN code IN (N'xiaomi', N'kestrel', N'manual') THEN 1 ELSE 0 END,
       30,
       CASE code
           WHEN N'xiaomi' THEN 1
           WHEN N'kestrel' THEN 2
           WHEN N'manual' THEN 3
           ELSE 99
       END
FROM sensor_type
WHERE code IN (N'xiaomi', N'kestrel', N'manual', N'none');
GO

CREATE TABLE sensor_parameter_definition (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    code NVARCHAR(64) NOT NULL,
    name NVARCHAR(128) NOT NULL,
    data_type NVARCHAR(32) NOT NULL,
    unit NVARCHAR(32) NULL,
    required BIT NOT NULL DEFAULT 1,
    active BIT NOT NULL DEFAULT 1,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT UQ_sensor_parameter_definition_code UNIQUE (code)
);
GO

INSERT INTO sensor_parameter_definition (id, code, name, data_type, unit, required, active, display_order)
VALUES
    ('A2000000-0000-4000-8000-000000000001', N'temperature', N'Temperature', N'decimal', N'C', 1, 1, 1),
    ('A2000000-0000-4000-8000-000000000002', N'humidity', N'Humidity', N'decimal', N'%', 1, 1, 2);
GO

CREATE TABLE sensor_parameter_source (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    parameter_definition_id UNIQUEIDENTIFIER NOT NULL,
    sensor_type_id UNIQUEIDENTIFIER NOT NULL,
    priority_order INT NOT NULL DEFAULT 0,
    CONSTRAINT FK_sensor_parameter_source_parameter
        FOREIGN KEY (parameter_definition_id) REFERENCES sensor_parameter_definition(id) ON DELETE CASCADE,
    CONSTRAINT FK_sensor_parameter_source_sensor_type
        FOREIGN KEY (sensor_type_id) REFERENCES sensor_type(id),
    CONSTRAINT UQ_sensor_parameter_source UNIQUE (parameter_definition_id, sensor_type_id)
);
GO

INSERT INTO sensor_parameter_source (parameter_definition_id, sensor_type_id, priority_order)
SELECT p.id, st.id,
       CASE st.code
           WHEN N'xiaomi' THEN 1
           WHEN N'kestrel' THEN 2
           WHEN N'manual' THEN 3
           ELSE 99
       END
FROM sensor_parameter_definition p
CROSS JOIN sensor_type st
WHERE p.code IN (N'temperature', N'humidity')
  AND st.code IN (N'xiaomi', N'kestrel', N'manual');
GO

CREATE TABLE respondent_sensor_assignment (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    respondent_id UNIQUEIDENTIFIER NOT NULL,
    sensor_type_id UNIQUEIDENTIFIER NOT NULL,
    sensor_mac_id UNIQUEIDENTIFIER NULL,
    enabled BIT NOT NULL DEFAULT 1,
    priority_order INT NOT NULL DEFAULT 0,
    CONSTRAINT FK_respondent_sensor_assignment_respondent
        FOREIGN KEY (respondent_id) REFERENCES identity_user(id) ON DELETE CASCADE,
    CONSTRAINT FK_respondent_sensor_assignment_sensor_type
        FOREIGN KEY (sensor_type_id) REFERENCES sensor_type(id),
    CONSTRAINT FK_respondent_sensor_assignment_sensor_mac
        FOREIGN KEY (sensor_mac_id) REFERENCES sensor_mac(id) ON DELETE SET NULL
);
GO

CREATE INDEX IX_respondent_sensor_assignment_respondent
    ON respondent_sensor_assignment (respondent_id, enabled, priority_order);
GO

INSERT INTO respondent_sensor_assignment (respondent_id, sensor_type_id, sensor_mac_id, enabled, priority_order)
SELECT sm.respondent_id, sm.sensor_type_id, sm.id, 1, 0
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
