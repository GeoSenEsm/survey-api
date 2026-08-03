-- The Xiaomi Door Sensor 2 previously reused the 'light' parameter (integer lux, from Flower Care)
-- to store its boolean ambient-light-detected flag. Sharing one definition across two different
-- units (lux vs. a plain on/off flag) is misleading in the UI and CSV export. Parameters that
-- represent different units are different parameters, so split them and only ever treat a
-- (name, unit) pair as identifying the "same" parameter going forward.

INSERT INTO sensor_parameter_definition (id, code, name, data_type, unit, required, active, display_order)
VALUES ('A2000000-0000-4000-8000-00000000000C', N'light_detected', N'Light', N'boolean', NULL, 0, 1, 12);
GO

UPDATE parameter_value
SET parameter_definition_id = (SELECT id FROM sensor_parameter_definition WHERE code = N'light_detected')
FROM sensor_data_parameter_value parameter_value
JOIN sensor_data sd ON sd.id = parameter_value.sensor_data_id
WHERE sd.source_sensor_type_id = (SELECT id FROM sensor_type WHERE code = N'xiaomi_door_sensor_2')
  AND parameter_value.parameter_definition_id = (SELECT id FROM sensor_parameter_definition WHERE code = N'light');
GO

DELETE FROM sensor_parameter_source
WHERE sensor_type_id = (SELECT id FROM sensor_type WHERE code = N'xiaomi_door_sensor_2')
  AND parameter_definition_id = (SELECT id FROM sensor_parameter_definition WHERE code = N'light');
GO

INSERT INTO sensor_parameter_source (parameter_definition_id, sensor_type_id, priority_order)
SELECT (SELECT id FROM sensor_parameter_definition WHERE code = N'light_detected'),
       (SELECT id FROM sensor_type WHERE code = N'xiaomi_door_sensor_2'),
       1;
GO

-- Repoint the seeded profile's advertisement mapping from 'light' to 'light_detected' and
-- recompute spec_hash to match. Flower Care's profile also contains "parameter":"light", so the
-- replacement is scoped to xiaomi_door_sensor_2's profile row only.
UPDATE profile
SET spec_json = REPLACE(profile.spec_json, N'"parameter":"light"', N'"parameter":"light_detected"'),
    spec_hash = LOWER(CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', CONVERT(VARBINARY(MAX),
        REPLACE(profile.spec_json, N'"parameter":"light"', N'"parameter":"light_detected"'))), 2)),
    updated_at = sysutcdatetime()
FROM sensor_gatt_profile profile
JOIN sensor_type st ON st.id = profile.sensor_type_id
WHERE st.code = N'xiaomi_door_sensor_2';
GO

-- Going forward, two parameter definitions may only share a name if their units differ (and vice
-- versa): identity is the (name, unit) pair, not the name alone.
ALTER TABLE sensor_parameter_definition
    ADD CONSTRAINT UQ_sensor_parameter_definition_name_unit UNIQUE (name, unit);
GO
