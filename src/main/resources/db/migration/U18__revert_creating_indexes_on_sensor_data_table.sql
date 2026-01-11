-- Revert: Drop index on sensor_data table
DROP INDEX IF EXISTS IX_sensor_data_date_time ON [dbo].[sensor_data];
GO

