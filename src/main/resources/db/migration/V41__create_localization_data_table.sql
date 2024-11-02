CREATE TABLE localization_data (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    respondent_id UNIQUEIDENTIFIER NOT NULL,
    participation_id UNIQUEIDENTIFIER,
    date_time DATETIMEOFFSET(0) NOT NULL,
    latitude DECIMAL(8, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL,
    row_version TIMESTAMP NOT NULL,

    FOREIGN KEY (participation_id) REFERENCES survey_participation(id) ON DELETE SET NULL,
    FOREIGN KEY (respondent_id) REFERENCES identity_user(id) ON DELETE CASCADE
);