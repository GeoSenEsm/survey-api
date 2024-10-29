CREATE TABLE localization_data (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    participation_id UNIQUEIDENTIFIER,
    date_time DATETIMEOFFSET(0),
    location GEOGRAPHY NOT NULL
    row_version TIMESTAMP NOT NULL,

    FOREIGN KEY (participation_id) REFERENCES survey_participation(id) ON DELETE CASCADE
);