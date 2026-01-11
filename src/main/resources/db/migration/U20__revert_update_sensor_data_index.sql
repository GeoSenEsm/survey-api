-- Revert: Restore sensor_data index to original state without survey_participation_id

-- Drop the updated index
DROP INDEX IF EXISTS IX_sensor_data_date_time ON [dbo].[sensor_data];
GO

-- Recreate with original columns (without survey_participation_id)
CREATE NONCLUSTERED INDEX IX_sensor_data_date_time
ON [dbo].[sensor_data] ([date_time] ASC)
INCLUDE ([respondent_id], [temperature], [humidity])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON);
GO

