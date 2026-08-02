DROP INDEX IF EXISTS IX_sensor_mac_sensor_type_id ON sensor_mac;
GO

ALTER TABLE sensor_mac
    DROP CONSTRAINT FK_sensor_mac_sensor_type;
GO

ALTER TABLE sensor_mac
    DROP COLUMN sensor_type_id;
GO

DROP TABLE IF EXISTS sensor_type;
GO
