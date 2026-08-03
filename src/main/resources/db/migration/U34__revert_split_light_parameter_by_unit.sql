ALTER TABLE sensor_parameter_definition
    DROP CONSTRAINT UQ_sensor_parameter_definition_name_unit;
GO

UPDATE profile
SET spec_json = REPLACE(profile.spec_json, N'"parameter":"light_detected"', N'"parameter":"light"'),
    spec_hash = LOWER(CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', CONVERT(VARBINARY(MAX),
        REPLACE(profile.spec_json, N'"parameter":"light_detected"', N'"parameter":"light"'))), 2)),
    updated_at = sysutcdatetime()
FROM sensor_gatt_profile profile
JOIN sensor_type st ON st.id = profile.sensor_type_id
WHERE st.code = N'xiaomi_door_sensor_2';
GO

DELETE FROM sensor_parameter_source
WHERE sensor_type_id = (SELECT id FROM sensor_type WHERE code = N'xiaomi_door_sensor_2')
  AND parameter_definition_id = (SELECT id FROM sensor_parameter_definition WHERE code = N'light_detected');
GO

INSERT INTO sensor_parameter_source (parameter_definition_id, sensor_type_id, priority_order)
SELECT (SELECT id FROM sensor_parameter_definition WHERE code = N'light'),
       (SELECT id FROM sensor_type WHERE code = N'xiaomi_door_sensor_2'),
       1;
GO

UPDATE parameter_value
SET parameter_definition_id = (SELECT id FROM sensor_parameter_definition WHERE code = N'light')
FROM sensor_data_parameter_value parameter_value
JOIN sensor_data sd ON sd.id = parameter_value.sensor_data_id
WHERE sd.source_sensor_type_id = (SELECT id FROM sensor_type WHERE code = N'xiaomi_door_sensor_2')
  AND parameter_value.parameter_definition_id =
      (SELECT id FROM sensor_parameter_definition WHERE code = N'light_detected');
GO

DELETE FROM sensor_parameter_definition WHERE code = N'light_detected';
GO
