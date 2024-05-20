IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'greenery_area_category_after_insert')
BEGIN
    DROP TRIGGER greenery_area_category_after_insert ON greenery_area_category;
END;

IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'greenery_area_category_after_update')
BEGIN
    DROP TRIGGER greenery_area_category_after_update ON greenery_area_category;
END;