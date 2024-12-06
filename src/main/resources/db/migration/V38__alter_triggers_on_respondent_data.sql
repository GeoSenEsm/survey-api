CREATE TRIGGER add_to_group_trigger
ON respondent_data
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @groupId UNIQUEIDENTIFIER;

    SELECT @groupId = id
    FROM respondents_group
    WHERE name = 'All';

    INSERT INTO respondent_to_group (id, respondent_id, group_id)
    SELECT
        NEWID(),
        inserted.id,
        @groupId
    FROM inserted;

    SET NOCOUNT OFF;
END;
GO

CREATE TRIGGER respondent_data_option_assign_to_groups_after_insert
ON respondent_data_option
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO respondent_to_group (respondent_id, group_id)
    SELECT
        rd.id AS respondent_id,
        rg.id AS group_id
    FROM
        inserted rdo
    INNER JOIN
        respondent_data_question rdq ON rdo.respondent_data_question_id = rdq.id
    INNER JOIN
        respondent_data rd ON rdq.respondent_id = rd.id
    INNER JOIN
        initial_survey_option iso ON rdo.option_id = iso.id
    INNER JOIN
        initial_survey_question isq ON iso.question_id = isq.id
    INNER JOIN
        respondents_group rg ON rg.name = N'' + isq.content + ' - ' + iso.content;

    SET NOCOUNT OFF;
END;
GO

CREATE TRIGGER respondent_data_update_group_id
ON respondent_data_option
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF UPDATE(option_id)
    BEGIN
       UPDATE respondent_to_group
           SET respondent_to_group.group_id = rg.id

           FROM inserted new_rdo
           INNER JOIN
                respondent_data_question new_rdq ON new_rdo.respondent_data_question_id = new_rdq.id
           INNER JOIN
               respondent_data rd ON new_rdq.respondent_id = rd.id
           INNER JOIN
               initial_survey_option iso ON new_rdo.option_id = iso.id
           INNER JOIN
               initial_survey_question isq ON iso.question_id = isq.id
           INNER JOIN
               respondents_group rg ON rg.name = N'' + isq.content + ' - ' + iso.content
           INNER JOIN
                deleted old_rdo ON old_rdo.id = new_rdo.id
           INNER JOIN
                initial_survey_option old_iso ON old_rdo.option_id = old_iso.id
           INNER JOIN
                initial_survey_question old_isq ON old_iso.question_id = old_isq.id
           INNER JOIN
                respondents_group old_rg ON old_rg.name = N'' + old_isq.content + ' - ' + old_iso.content
           WHERE respondent_to_group.group_id = old_rg.id;

    END;

    SET NOCOUNT OFF;
END;

