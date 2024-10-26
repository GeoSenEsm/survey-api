DECLARE @sql NVARCHAR(MAX);

SET @sql = '';

SELECT @sql = @sql + 'ALTER TABLE ' + QUOTENAME(OBJECT_NAME(parent_object_id)) +
                   ' DROP CONSTRAINT ' + QUOTENAME(name) + '; '
FROM sys.key_constraints
WHERE type = 'UQ'
AND parent_object_id = OBJECT_ID('respondents_group');

EXEC sp_executesql @sql;

ALTER TABLE respondents_group
DROP COLUMN polish_name;

EXEC sp_rename 'respondents_group.english_name', 'name', 'COLUMN';
GO

CREATE TRIGGER fill_respondents_group_trigger
ON initial_survey_option
AFTER INSERT
AS
BEGIN
SET NOCOUNT ON;
    INSERT INTO respondents_group (name)
    SELECT
        N'' + q.content + ' - ' + i.content
    FROM inserted i
    INNER JOIN initial_survey_question q
        ON i.question_id = q.id
    INNER JOIN initial_survey s
        ON q.survey_id = s.id;
    SET NOCOUNT OFF;
END;
GO