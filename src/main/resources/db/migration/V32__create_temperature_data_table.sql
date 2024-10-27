CREATE TABLE temperature_data (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    respondent_id UNIQUEIDENTIFIER NOT NULL,
    date_time DATETIMEOFFSET(0) NOT NULL,
    temperature DECIMAL(4, 2) NOT NULL,
    FOREIGN KEY (respondent_id) REFERENCES identity_user(id) ON DELETE CASCADE,
    UNIQUE(respondent_id, date_time)
)