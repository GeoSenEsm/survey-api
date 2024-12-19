DECLARE @ConstraintName NVARCHAR(128);
SELECT @ConstraintName = name
FROM sys.key_constraints
WHERE parent_object_id = OBJECT_ID('option') AND type = 'UQ';

EXEC('ALTER TABLE [option] DROP CONSTRAINT ' + @ConstraintName);

ALTER TABLE [option] ALTER COLUMN label NVARCHAR(150) NULL;

ALTER TABLE [option] ADD CONSTRAINT UQ_label_question_id UNIQUE (label, question_id);