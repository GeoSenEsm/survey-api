CREATE TABLE question (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    [order] INT NOT NULL,
    section_id UNIQUEIDENTIFIER,
    content NVARCHAR(250) NOT NULL,
    question_type INT NOT NULL,
    required BIT NOT NULL,
    row_version TIMESTAMP NOT NULL,
    FOREIGN KEY (section_id) REFERENCES survey_section(id) ON DELETE CASCADE,
    UNIQUE ([order], section_id)
);
