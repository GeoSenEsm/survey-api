IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'education_category_after_insert')
BEGIN
    DROP TRIGGER education_category_after_insert ON education_category;
END;

IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'education_category_after_update')
BEGIN
    DROP TRIGGER education_category_after_update ON education_category;
END;