-- Revert: Drop index on localization_data table
DROP INDEX IF EXISTS IX_localization_data_date_time ON [dbo].[localization_data];
GO

