CREATE TABLE option_selection (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    question_answer_id UNIQUEIDENTIFIER NOT NULL,
    option_id UNIQUEIDENTIFIER,
    row_version TIMESTAMP NOT NULL,

    FOREIGN KEY (question_answer_id) REFERENCES question_answer(id),
    FOREIGN KEY (option_id) REFERENCES [option](id)
);