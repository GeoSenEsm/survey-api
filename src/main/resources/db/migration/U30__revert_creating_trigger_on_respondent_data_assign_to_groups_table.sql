IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'respondent_data_assign_to_groups_after_insert')
BEGIN
    DROP TRIGGER respondent_data_assign_to_groups_after_insert ON respondent_data;
END;

IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'respondent_data_assign_to_groups_after_update')
BEGIN
    DROP TRIGGER respondent_data_assign_to_groups_after_update ON respondent_data;
END;