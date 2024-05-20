IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'life_satisfaction_after_insert')
BEGIN
    DROP TRIGGER life_satisfaction_after_insert ON life_satisfaction;
END;

IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'life_satisfaction_after_update')
BEGIN
    DROP TRIGGER life_satisfaction_after_update ON life_satisfaction;
END;