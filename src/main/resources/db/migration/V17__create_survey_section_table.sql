CREATE TABLE survey_section (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    [order] INT NOT NULL,
    name NVARCHAR(100),
    survey_id UNIQUEIDENTIFIER,
    visibility INT NOT NULL,
    row_version TIMESTAMP NOT NULL,
    FOREIGN KEY (survey_id) REFERENCES survey(id) ON DELETE CASCADE,
    UNIQUE ([order], survey_id)
);