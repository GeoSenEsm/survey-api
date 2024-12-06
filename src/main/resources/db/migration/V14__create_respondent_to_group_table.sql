CREATE TABLE respondent_to_group (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    respondent_id UNIQUEIDENTIFIER,
    group_id UNIQUEIDENTIFIER,
    row_version TIMESTAMP NOT NULL,
    FOREIGN KEY (respondent_id) REFERENCES respondent_data(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES respondents_group(id) ON DELETE CASCADE
);