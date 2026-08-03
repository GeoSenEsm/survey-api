-- Sensor types that ship as GATT profiles (xiaomi, kestrel, pc_60fw, bluetooth_sig_plx,
-- flower_care, xiaomi_door_sensor_2, inkbird_ibs_th1) are no longer auto-seeded: they now live as
-- code-defined "templates" (SensorProfileTemplateCatalog) that an admin installs on demand from
-- the Integrations page. This purges those seeded rows from every database, including ones that
-- already migrated through V31/V33, so the catalog starts empty everywhere. 'manual' and 'none'
-- are core, always-present types (not BLE profiles) and are left untouched.
--
-- Historical sensor_data readings and sensor_parameter_definition rows are preserved: only the
-- catalog entries (sensor_type / sensor_type_setting / sensor_parameter_source / sensor_gatt_profile)
-- and the device registry rows tied to them (sensor_mac / sensor_device_secret /
-- respondent_sensor_assignment) are removed. Re-installing a template later reuses the existing
-- parameter definitions by code.

UPDATE sensor_data
SET source_sensor_type_id = NULL, source = NULL
WHERE source_sensor_type_id IN (SELECT id FROM sensor_type
    WHERE code IN (N'xiaomi', N'kestrel', N'pc_60fw', N'bluetooth_sig_plx',
                   N'flower_care', N'xiaomi_door_sensor_2', N'inkbird_ibs_th1'));
GO

DELETE FROM respondent_sensor_assignment
WHERE sensor_type_id IN (SELECT id FROM sensor_type
    WHERE code IN (N'xiaomi', N'kestrel', N'pc_60fw', N'bluetooth_sig_plx',
                   N'flower_care', N'xiaomi_door_sensor_2', N'inkbird_ibs_th1'));
GO

-- Cascades to sensor_device_secret via FK_sensor_device_secret_sensor_mac.
DELETE FROM sensor_mac
WHERE sensor_type_id IN (SELECT id FROM sensor_type
    WHERE code IN (N'xiaomi', N'kestrel', N'pc_60fw', N'bluetooth_sig_plx',
                   N'flower_care', N'xiaomi_door_sensor_2', N'inkbird_ibs_th1'));
GO

DELETE FROM sensor_parameter_source
WHERE sensor_type_id IN (SELECT id FROM sensor_type
    WHERE code IN (N'xiaomi', N'kestrel', N'pc_60fw', N'bluetooth_sig_plx',
                   N'flower_care', N'xiaomi_door_sensor_2', N'inkbird_ibs_th1'));
GO

DELETE FROM sensor_type_setting
WHERE sensor_type_id IN (SELECT id FROM sensor_type
    WHERE code IN (N'xiaomi', N'kestrel', N'pc_60fw', N'bluetooth_sig_plx',
                   N'flower_care', N'xiaomi_door_sensor_2', N'inkbird_ibs_th1'));
GO

-- Cascades to sensor_gatt_profile via FK_sensor_gatt_profile_sensor_type.
DELETE FROM sensor_type
WHERE code IN (N'xiaomi', N'kestrel', N'pc_60fw', N'bluetooth_sig_plx',
               N'flower_care', N'xiaomi_door_sensor_2', N'inkbird_ibs_th1');
GO
