DROP INDEX IF EXISTS IX_sensor_mac_respondent_id ON sensor_mac;
GO

ALTER TABLE sensor_mac
    DROP CONSTRAINT FK_sensor_mac_respondent;
GO

ALTER TABLE sensor_mac
    DROP COLUMN respondent_id;
GO
