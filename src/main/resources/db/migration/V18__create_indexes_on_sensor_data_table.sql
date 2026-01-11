-- Primary index on date_time for queries filtering by date range (most common pattern)
-- This improves performance for queries like: WHERE date_time BETWEEN @from AND @to
-- Covers both cases: with and without respondent_id filter
CREATE NONCLUSTERED INDEX IX_sensor_data_date_time
ON [dbo].[sensor_data] ([date_time] ASC)
INCLUDE ([respondent_id], [temperature], [humidity])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON);
GO

-- Note: The existing UNIQUE constraint on (respondent_id, date_time) already provides
-- an index for queries filtering by both respondent_id AND date_time.
-- Since respondent_id filtering is optional and date_time is always present,
-- we rely on the date_time index above as the primary query optimization.
-- SQL Server query optimizer will choose between these indexes based on the query pattern.

