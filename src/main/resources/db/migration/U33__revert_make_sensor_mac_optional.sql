DROP INDEX IF EXISTS UQ_sensor_mac_sensor_mac ON sensor_mac;
GO

ALTER TABLE sensor_mac
    ALTER COLUMN sensor_mac NVARCHAR(17) NOT NULL;
GO

ALTER TABLE sensor_mac
    ADD CONSTRAINT UQ_sensor_mac_sensor_mac UNIQUE (sensor_mac);
GO
