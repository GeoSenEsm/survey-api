IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'age_category_after_insert')
BEGIN
    DROP TRIGGER age_category_after_insert ON age_category;
END;

IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'age_category_after_update')
BEGIN
    DROP TRIGGER age_category_after_update ON age_category;
END;