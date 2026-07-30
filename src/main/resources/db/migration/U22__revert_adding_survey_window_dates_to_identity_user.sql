DROP INDEX IF EXISTS IX_identity_user_survey_window ON identity_user;
GO

ALTER TABLE identity_user
    DROP CONSTRAINT CK_identity_user_survey_window;
GO

ALTER TABLE identity_user
    DROP COLUMN survey_start_date,
                survey_end_date;
GO
