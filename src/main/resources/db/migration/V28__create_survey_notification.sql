CREATE TABLE survey_notification (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    survey_id UNIQUEIDENTIFIER NOT NULL,
    [order] INT NOT NULL,
    relative_to INT NOT NULL,
    minutes_before INT NOT NULL,
    row_version TIMESTAMP NOT NULL,
    CONSTRAINT FK_survey_notification_survey
        FOREIGN KEY (survey_id) REFERENCES survey(id) ON DELETE CASCADE,
    CONSTRAINT CK_survey_notification_relative_to
        CHECK (relative_to IN (0, 1)),
    CONSTRAINT CK_survey_notification_minutes_before
        CHECK (minutes_before >= 0),
    CONSTRAINT UQ_survey_notification_survey_order
        UNIQUE (survey_id, [order])
);

CREATE INDEX IX_survey_notification_survey
    ON survey_notification (survey_id);

-- Defaults matching previous mobile behaviour: at survey start, and 15 minutes before end.
INSERT INTO survey_notification (id, survey_id, [order], relative_to, minutes_before)
SELECT NEWID(), s.id, 0, 0, 0
FROM survey s;

INSERT INTO survey_notification (id, survey_id, [order], relative_to, minutes_before)
SELECT NEWID(), s.id, 1, 1, 15
FROM survey s;
