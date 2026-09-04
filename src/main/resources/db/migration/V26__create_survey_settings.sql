CREATE TABLE survey_settings (
    id INT NOT NULL PRIMARY KEY,
    show_sending_policy_calendar BIT NOT NULL
        CONSTRAINT DF_survey_settings_show_sending_policy_calendar DEFAULT 1,
    CONSTRAINT CK_survey_settings_singleton CHECK (id = 1)
);

INSERT INTO survey_settings (id, show_sending_policy_calendar)
VALUES (1, 1);
