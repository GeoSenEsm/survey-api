CREATE TABLE [option] (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    [order] INT NOT NULL,
    question_id UNIQUEIDENTIFIER,
    label NVARCHAR(50),
    row_version TIMESTAMP NOT NULL,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    UNIQUE ([order], question_id),
    UNIQUE (label, question_id)
);
