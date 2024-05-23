CREATE TABLE survey_sending_policy (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    survey_id UNIQUEIDENTIFIER,
    row_version TIMESTAMP NOT NULL,
    FOREIGN KEY (survey_id) REFERENCES survey(id),
);