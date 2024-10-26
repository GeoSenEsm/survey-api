CREATE TABLE initial_survey_question (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    survey_id UNIQUEIDENTIFIER REFERENCES initial_survey(id) ON DELETE CASCADE,
    [order] INT NOT NULL,
    content NVARCHAR(250) NOT NULL,
    row_version TIMESTAMP NOT NULL
);