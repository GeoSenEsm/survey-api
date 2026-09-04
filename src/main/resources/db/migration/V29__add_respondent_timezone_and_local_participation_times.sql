-- Respondent IANA timezone (default UTC until first login reports device TZ).
-- Participation local_date / local_time are denormalized wall-clock fields
-- derived from the UTC participation instant in the respondent's timezone;
-- study analytics and result filters query these instead of UTC day bounds.
ALTER TABLE identity_user
    ADD time_zone NVARCHAR(64) NOT NULL
        CONSTRAINT DF_identity_user_time_zone DEFAULT 'UTC';
GO

ALTER TABLE survey_participation
    ADD local_date DATE NULL,
        local_time TIME(0) NULL;
GO

-- Backfill local_* from the UTC face of existing participation timestamps
-- (all respondents default to UTC until they log in with a device timezone).
UPDATE sp
SET
    local_date = CONVERT(date, SWITCHOFFSET(sp.date, '+00:00')),
    local_time = CONVERT(time(0), SWITCHOFFSET(sp.date, '+00:00'))
FROM survey_participation sp
WHERE sp.local_date IS NULL;
GO

ALTER TABLE survey_participation
    ALTER COLUMN local_date DATE NOT NULL;
GO

ALTER TABLE survey_participation
    ALTER COLUMN local_time TIME(0) NOT NULL;
GO

CREATE INDEX IX_survey_participation_local_date_time
    ON survey_participation (local_date, local_time)
    INCLUDE (respondent_id, survey_id);
GO

CREATE INDEX IX_identity_user_time_zone
    ON identity_user (time_zone)
    WHERE role = 'Respondent';
GO
