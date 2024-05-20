IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'medication_use_after_insert')
BEGIN
    DROP TRIGGER medication_use_after_insert ON medication_use;
END;

IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'medication_use_after_update')
BEGIN
    DROP TRIGGER medication_use_after_update ON medication_use;
END;