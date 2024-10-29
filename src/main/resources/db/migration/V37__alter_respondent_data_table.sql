DECLARE @sql NVARCHAR(MAX) = '';

SELECT @sql = @sql + 'ALTER TABLE [' + OBJECT_NAME(fk.parent_object_id) + '] DROP CONSTRAINT [' + fk.name + '];' + CHAR(13)
FROM sys.foreign_keys AS fk
WHERE fk.referenced_object_id = OBJECT_ID('respondent_data');

EXEC sp_executesql @sql;
DROP TABLE IF EXISTS respondent_data;
GO

CREATE TABLE respondent_data (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    identity_user_id UNIQUEIDENTIFIER,
    survey_id UNIQUEIDENTIFIER NOT NULL,
    row_version TIMESTAMP NOT NULL,

    UNIQUE (identity_user_id, survey_id),
    FOREIGN KEY (identity_user_id) REFERENCES identity_user(id) ON DELETE SET NULL
);

CREATE TABLE respondent_data_question (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    respondent_id UNIQUEIDENTIFIER NOT NULL,
    question_id UNIQUEIDENTIFIER NOT NULL,
    row_version TIMESTAMP NOT NULL,

    UNIQUE (respondent_id, question_id),
    FOREIGN KEY (respondent_id) REFERENCES respondent_data(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES initial_survey_question(id)
);

CREATE TABLE respondent_data_option (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    respondent_data_question_id UNIQUEIDENTIFIER NOT NULL,
    option_id UNIQUEIDENTIFIER,
    row_version TIMESTAMP NOT NULL,

    FOREIGN KEY (respondent_data_question_id) REFERENCES respondent_data_question(id),
    FOREIGN KEY (option_id) REFERENCES initial_survey_option(id)
);
