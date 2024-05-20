IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'occupation_category_after_insert')
BEGIN
    DROP TRIGGER occupation_category_after_insert ON occupation_category;
END;

IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'occupation_category_after_insert')
BEGIN
    DROP TRIGGER occupation_category_after_insert ON occupation_category;
END;