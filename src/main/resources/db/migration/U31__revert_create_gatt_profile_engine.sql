DROP INDEX IX_sensor_gatt_profile_sensor_type_revision ON sensor_gatt_profile;
GO

DROP INDEX UQ_sensor_gatt_profile_published ON sensor_gatt_profile;
GO

DROP TABLE sensor_gatt_profile;
GO

ALTER TABLE sensor_type DROP CONSTRAINT CK_sensor_type_integration_mode;
GO

ALTER TABLE sensor_type DROP CONSTRAINT DF_sensor_type_integration_mode;
GO

ALTER TABLE sensor_type DROP COLUMN integration_mode;
GO

ALTER TABLE sensor_type DROP COLUMN adapter_key;
GO
