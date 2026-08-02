DROP INDEX IX_sensor_data_parameter_value_sensor_data ON sensor_data_parameter_value;
GO

DROP TABLE sensor_data_parameter_value;
GO

ALTER TABLE sensor_data
    ADD temperature DECIMAL(4,2) NULL,
        humidity DECIMAL(5,2) NULL;
GO

-- Restore original index that includes temperature and humidity
DROP INDEX IX_sensor_data_date_time ON sensor_data;
GO

CREATE NONCLUSTERED INDEX IX_sensor_data_date_time
ON [dbo].[sensor_data] ([date_time] ASC)
INCLUDE ([respondent_id], [temperature], [humidity], [survey_participation_id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF,
      DROP_EXISTING = OFF, ONLINE = OFF,
      ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON);
GO

ALTER TABLE sensor_data DROP CONSTRAINT FK_sensor_data_source_sensor_type;
GO

ALTER TABLE sensor_data DROP COLUMN source_sensor_type_id;
GO

ALTER TABLE sensor_data DROP COLUMN source;
GO

DROP INDEX IX_respondent_sensor_assignment_respondent ON respondent_sensor_assignment;
GO

DROP TABLE respondent_sensor_assignment;
GO

DROP TABLE sensor_parameter_source;
GO

DROP TABLE sensor_parameter_definition;
GO

DROP TABLE sensor_type_setting;
GO

DROP TABLE survey_sensor_settings;
GO
