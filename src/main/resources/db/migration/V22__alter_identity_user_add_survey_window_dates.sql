-- Per-respondent study window. Both columns are nullable so existing
-- respondents keep working without backfill; statistics fall back to
-- first/last participation when either date is NULL.
ALTER TABLE identity_user
    ADD survey_start_date DATE NULL,
        survey_end_date DATE NULL;
GO

ALTER TABLE identity_user
    ADD CONSTRAINT CK_identity_user_survey_window
        CHECK (
            survey_start_date IS NULL
            OR survey_end_date IS NULL
            OR survey_end_date >= survey_start_date
        );
GO

CREATE INDEX IX_identity_user_survey_window
    ON identity_user (survey_start_date, survey_end_date)
    WHERE survey_start_date IS NOT NULL AND survey_end_date IS NOT NULL;
GO
