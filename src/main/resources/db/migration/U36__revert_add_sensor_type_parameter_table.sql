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
SELECT used_parameter_id, sensor_type_id, priority_order
FROM sensor_type_parameter
WHERE used_parameter_id IS NOT NULL;
GO

DROP TABLE sensor_type_parameter;
GO
