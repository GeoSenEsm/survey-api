IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'stress_level_after_insert')
BEGIN
    DROP TRIGGER stress_level_after_insert ON stress_level;
END;

IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'stress_level_after_update')
BEGIN
    DROP TRIGGER stress_level_after_update ON stress_level;
END;