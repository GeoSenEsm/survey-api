IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'health_condition_after_insert')
BEGIN
    DROP TRIGGER health_condition_after_insert ON health_condition;
END;

IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'health_condition_after_update')
BEGIN
    DROP TRIGGER health_condition_after_update ON health_condition;
END;