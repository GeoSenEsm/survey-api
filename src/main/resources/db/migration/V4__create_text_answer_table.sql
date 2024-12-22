CREATE TABLE text_answer (
	id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
	question_answer_id UNIQUEIDENTIFIER NOT NULL,
	text_answer_content NVARCHAR(150) NOT NULL,
	row_version TIMESTAMP NOT NULL,

	FOREIGN KEY (question_answer_id) REFERENCES question_answer(id) ON DELETE CASCADE
);