-- A sensor device no longer needs a pre-registered MAC: sensor types matched by discovery
-- (advertised name/service/product ID, e.g. Xiaomi) can be assigned without knowing which
-- physical unit a respondent has in advance. Replace the plain UNIQUE constraint (which allows
-- only one NULL under SQL Server's default semantics) with a filtered unique index so any
-- number of MAC-less devices can coexist.
DECLARE @ConstraintName NVARCHAR(128);
SELECT @ConstraintName = kc.name
FROM sys.key_constraints kc
JOIN sys.index_columns ic
    ON ic.object_id = kc.parent_object_id AND ic.index_id = kc.unique_index_id
JOIN sys.columns c
    ON c.object_id = ic.object_id AND c.column_id = ic.column_id
WHERE kc.parent_object_id = OBJECT_ID('sensor_mac')
  AND kc.type = 'UQ'
  AND c.name = 'sensor_mac';

IF @ConstraintName IS NOT NULL
BEGIN
    EXEC('ALTER TABLE sensor_mac DROP CONSTRAINT ' + @ConstraintName);
END
GO

ALTER TABLE sensor_mac
    ALTER COLUMN sensor_mac NVARCHAR(17) NULL;
GO

CREATE UNIQUE INDEX UQ_sensor_mac_sensor_mac
    ON sensor_mac (sensor_mac)
    WHERE sensor_mac IS NOT NULL;
GO
