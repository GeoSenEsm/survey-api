DROP INDEX IF EXISTS IX_identity_user_time_zone ON identity_user;
GO

DROP INDEX IF EXISTS IX_survey_participation_local_date_time ON survey_participation;
GO

ALTER TABLE survey_participation
    DROP COLUMN local_date,
        local_time;
GO

ALTER TABLE identity_user
    DROP CONSTRAINT DF_identity_user_time_zone;
GO

ALTER TABLE identity_user
    DROP COLUMN time_zone;
GO
