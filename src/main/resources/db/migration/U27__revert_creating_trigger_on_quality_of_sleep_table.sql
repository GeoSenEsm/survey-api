IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'quality_of_sleep_after_insert')
BEGIN
    DROP TRIGGER quality_of_sleep_after_insert ON quality_of_sleep;
END;

IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'quality_of_sleep_after_update')
BEGIN
    DROP TRIGGER quality_of_sleep_after_update ON quality_of_sleep;
END;