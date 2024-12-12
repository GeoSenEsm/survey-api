DECLARE @constraint_name NVARCHAR(256);

SELECT @constraint_name = fk.name
FROM sys.foreign_keys fk
INNER JOIN sys.foreign_key_columns fkc
    ON fk.object_id = fkc.constraint_object_id
WHERE
    fkc.parent_object_id = OBJECT_ID('option_selection')
    AND fkc.referenced_object_id = OBJECT_ID('question_answer');

IF @constraint_name IS NOT NULL
BEGIN
    EXEC('ALTER TABLE option_selection DROP CONSTRAINT ' + @constraint_name);
END
GO

ALTER TABLE option_selection
ADD CONSTRAINT FK_option_selection_question_answer_id
FOREIGN KEY (question_answer_id)
REFERENCES question_answer(id)
ON DELETE CASCADE;
GO

DECLARE @constraint_name NVARCHAR(256);

SELECT @constraint_name = fk.name
FROM sys.foreign_keys fk
INNER JOIN sys.foreign_key_columns fkc
    ON fk.object_id = fkc.constraint_object_id
WHERE
    fkc.parent_object_id = OBJECT_ID('option_selection')
    AND fkc.referenced_object_id = OBJECT_ID('option');

IF @constraint_name IS NOT NULL
BEGIN
    EXEC('ALTER TABLE option_selection DROP CONSTRAINT ' + @constraint_name);
END
GO

ALTER TABLE option_selection
ADD CONSTRAINT FK_option_selection_option_id
FOREIGN KEY (option_id)
REFERENCES [option](id)
ON DELETE CASCADE;
