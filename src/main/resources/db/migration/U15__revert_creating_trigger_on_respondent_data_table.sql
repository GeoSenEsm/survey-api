IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'add_to_group_trigger')
BEGIN
    DROP TRIGGER add_to_group_trigger ON respondent_data;
END;