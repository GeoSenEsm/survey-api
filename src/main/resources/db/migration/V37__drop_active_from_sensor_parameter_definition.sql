-- The "used sensor data" list no longer has a soft-hide `active` flag: a parameter is either on
-- the list or removed via DELETE /api/surveysettings/sensordata/parameters/{id}.
DECLARE @constraintName NVARCHAR(200);

SELECT @constraintName = dc.name
FROM sys.default_constraints dc
JOIN sys.columns c
    ON dc.parent_object_id = c.object_id
   AND dc.parent_column_id = c.column_id
WHERE dc.parent_object_id = OBJECT_ID('dbo.sensor_parameter_definition')
  AND c.name = 'active';

IF @constraintName IS NOT NULL
BEGIN
    EXEC('ALTER TABLE dbo.sensor_parameter_definition DROP CONSTRAINT ' + @constraintName);
END

ALTER TABLE dbo.sensor_parameter_definition DROP COLUMN active;
GO
