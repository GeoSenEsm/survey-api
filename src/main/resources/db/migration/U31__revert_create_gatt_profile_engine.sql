DROP INDEX UQ_respondent_sensor_assignment_sensor ON respondent_sensor_assignment;
GO

DROP INDEX UQ_respondent_sensor_assignment_type ON respondent_sensor_assignment;
GO

WITH duplicate_assignments AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY respondent_id ORDER BY sensor_id) AS row_number
    FROM sensor_mac
    WHERE respondent_id IS NOT NULL
)
UPDATE sensor_mac
SET respondent_id = NULL
WHERE id IN (SELECT id FROM duplicate_assignments WHERE row_number > 1);
GO

CREATE UNIQUE INDEX UQ_sensor_mac_respondent_id
    ON sensor_mac(respondent_id)
    WHERE respondent_id IS NOT NULL;
GO

DROP TABLE sensor_device_secret;
GO

DROP TABLE sensor_gatt_profile;
GO

DELETE FROM sensor_parameter_source
WHERE sensor_type_id IN (
    SELECT id FROM sensor_type
    WHERE code IN (N'pc_60fw', N'bluetooth_sig_plx', N'flower_care', N'xiaomi_door_sensor_2')
);
GO

DELETE FROM sensor_parameter_definition
WHERE code IN (N'spo2', N'pulse_rate', N'perfusion_index', N'flags', N'light',
               N'moisture', N'conductivity', N'opening', N'battery');
GO

DELETE FROM sensor_type_setting
WHERE sensor_type_id IN (
    SELECT id FROM sensor_type
    WHERE code IN (N'pc_60fw', N'bluetooth_sig_plx', N'flower_care', N'xiaomi_door_sensor_2')
);
GO

DELETE FROM sensor_type
WHERE code IN (N'pc_60fw', N'bluetooth_sig_plx', N'flower_care', N'xiaomi_door_sensor_2');
GO

ALTER TABLE sensor_type DROP CONSTRAINT CK_sensor_type_integration_mode;
GO

ALTER TABLE sensor_type DROP CONSTRAINT DF_sensor_type_integration_mode;
GO

ALTER TABLE sensor_type DROP COLUMN integration_mode;
GO

ALTER TABLE sensor_type DROP COLUMN adapter_key;
GO
