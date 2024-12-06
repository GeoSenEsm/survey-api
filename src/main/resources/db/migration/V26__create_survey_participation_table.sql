CREATE TABLE survey_participation (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    respondent_id UNIQUEIDENTIFIER,
    survey_id UNIQUEIDENTIFIER NOT NULL,
    [date] DATETIMEOFFSET(0) NOT NULL,
    row_version TIMESTAMP NOT NULL,

    UNIQUE (respondent_id, [date], survey_id),
    FOREIGN KEY (respondent_id) REFERENCES identity_user(id) ON DELETE SET NULL
);