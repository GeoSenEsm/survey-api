DECLARE @ConstraintName NVARCHAR(128);

SELECT @ConstraintName = fk.name
FROM sys.foreign_keys AS fk
INNER JOIN sys.tables AS t ON fk.parent_object_id = t.object_id
WHERE t.name = 'respondent_data_option'
  AND fk.referenced_object_id = OBJECT_ID('respondent_data_question');

IF @ConstraintName IS NOT NULL
BEGIN
    EXEC('ALTER TABLE respondent_data_option DROP CONSTRAINT ' + @ConstraintName);
END

ALTER TABLE respondent_data_option
ADD CONSTRAINT fk_respondent_data_option_respondent_data_question
FOREIGN KEY (respondent_data_question_id)
REFERENCES respondent_data_question(id)
ON DELETE CASCADE;
