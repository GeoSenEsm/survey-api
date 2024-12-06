CREATE TABLE question_answer (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    participation_id UNIQUEIDENTIFIER NOT NULL,
    question_id UNIQUEIDENTIFIER NOT NULL,
    numeric_answer INTEGER DEFAULT NULL,
    row_version TIMESTAMP NOT NULL,

    UNIQUE (participation_id, question_id),
    FOREIGN KEY (participation_id) REFERENCES survey_participation(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(id)
);