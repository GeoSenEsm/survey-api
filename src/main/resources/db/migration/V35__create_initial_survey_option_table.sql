CREATE TABLE initial_survey_option (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    question_id UNIQUEIDENTIFIER REFERENCES initial_survey_question(id) ON DELETE CASCADE,
    [order] INT NOT NULL,
    content NVARCHAR(250) NOT NULL,
    row_version TIMESTAMP NOT NULL
);