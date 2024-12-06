CREATE TABLE survey_participation_time_slot (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    start DATETIMEOFFSET(0) NOT NULL,
    finish DATETIMEOFFSET(0) NOT NULL,
    survey_sending_policy_id UNIQUEIDENTIFIER,
    row_version TIMESTAMP NOT NULL,
    FOREIGN KEY (survey_sending_policy_id) REFERENCES survey_sending_policy(id) ON DELETE CASCADE
);