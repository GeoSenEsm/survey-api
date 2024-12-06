CREATE TABLE number_range (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    [from] INT NOT NULL,
    [to] INT NOT NULL,
    from_label NVARCHAR(50),
    to_label NVARCHAR(50),
    question_id UNIQUEIDENTIFIER NOT NULL,
    row_version TIMESTAMP NOT NULL,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
);