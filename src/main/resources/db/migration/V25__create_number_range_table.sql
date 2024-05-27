CREATE TABLE number_range (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    [from] INT NOT NULL,
    [to] INT NOT NULL,
    start_label NVARCHAR(50) NOT NULL,
    end_label NVARCHAR(50) NOT NULL,
    question_id UNIQUEIDENTIFIER NOT NULL,
    row_version TIMESTAMP NOT NULL,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
);