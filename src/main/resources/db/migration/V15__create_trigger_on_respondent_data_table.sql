CREATE TRIGGER add_to_group_trigger
ON respondent_data
AFTER INSERT
AS
BEGIN
    DECLARE @groupId UNIQUEIDENTIFIER;

    SELECT @groupId = id
    FROM respondents_group
    WHERE name = 'Wszyscy';

    INSERT INTO respondent_to_group (id, respondent_id, group_id)
    SELECT
        NEWID(),
        inserted.id,
        @groupId
    FROM inserted;
END;