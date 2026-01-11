-- Update sensor_data index to include survey_participation_id for better JOIN performance
-- This recreates the IX_sensor_data_date_time index with additional INCLUDE column

-- Drop the existing index
DROP INDEX IF EXISTS IX_sensor_data_date_time ON [dbo].[sensor_data];
GO

-- Recreate with survey_participation_id included
-- This improves performance for queries with fetch joins on surveyParticipation
CREATE NONCLUSTERED INDEX IX_sensor_data_date_time
ON [dbo].[sensor_data] ([date_time] ASC)
INCLUDE ([respondent_id], [temperature], [humidity], [survey_participation_id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON);
GO

