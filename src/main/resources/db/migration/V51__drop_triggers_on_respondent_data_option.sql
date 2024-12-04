IF OBJECT_ID('dbo.respondent_data_option_assign_to_groups_after_insert', 'TR') IS NOT NULL
BEGIN
     DROP TRIGGER dbo.respondent_data_option_assign_to_groups_after_insert;
END

IF OBJECT_ID('dbo.respondent_data_update_group_id', 'TR') IS NOT NULL
BEGIN
      DROP TRIGGER dbo.respondent_data_update_group_id;
END