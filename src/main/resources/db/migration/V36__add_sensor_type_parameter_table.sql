CREATE TABLE sensor_type_parameter (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    sensor_type_id UNIQUEIDENTIFIER NOT NULL,
    code NVARCHAR(64) NOT NULL,
    name NVARCHAR(128) NOT NULL,
    data_type NVARCHAR(32) NOT NULL,
    unit NVARCHAR(32) NULL,
    used_parameter_id UNIQUEIDENTIFIER NULL,
    priority_order INT NOT NULL DEFAULT 0,
    CONSTRAINT FK_sensor_type_parameter_sensor_type
        FOREIGN KEY (sensor_type_id) REFERENCES sensor_type(id) ON DELETE CASCADE,
    CONSTRAINT FK_sensor_type_parameter_used_parameter
        FOREIGN KEY (used_parameter_id) REFERENCES sensor_parameter_definition(id) ON DELETE SET NULL,
    CONSTRAINT UQ_sensor_type_parameter_type_code UNIQUE (sensor_type_id, code),
    CONSTRAINT CK_sensor_type_parameter_data_type CHECK (data_type IN (N'decimal', N'integer', N'boolean', N'text'))
);
GO

-- Every existing sensor_parameter_source row becomes a raw catalog entry that is already
-- "used" (linked), preserving today's wiring (built-in templates, multi-sensor fallback
-- chains) unchanged. Raw code/name/data_type/unit are copied from the definition it feeds,
-- since prior to this migration the raw identity and the used identity were the same string.
INSERT INTO sensor_type_parameter (id, sensor_type_id, code, name, data_type, unit, used_parameter_id, priority_order)
SELECT NEWID(), s.sensor_type_id, d.code, d.name, d.data_type, d.unit, s.parameter_definition_id, s.priority_order
FROM sensor_parameter_source s
JOIN sensor_parameter_definition d ON d.id = s.parameter_definition_id;
GO

CREATE INDEX IX_sensor_type_parameter_used_parameter
    ON sensor_type_parameter (used_parameter_id);
GO

DROP TABLE sensor_parameter_source;
GO
