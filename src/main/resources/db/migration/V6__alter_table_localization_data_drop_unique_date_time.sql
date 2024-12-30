DECLARE @ConstraintName NVARCHAR(128);
SELECT @ConstraintName = name
FROM sys.key_constraints
WHERE parent_object_id = OBJECT_ID('localization_data') AND type = 'UQ';

IF @ConstraintName IS NOT NULL
BEGIN
    EXEC('ALTER TABLE localization_data DROP CONSTRAINT ' + @ConstraintName);
END


