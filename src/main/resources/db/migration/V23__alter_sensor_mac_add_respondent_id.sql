-- Optional 1:1 assignment of a sensor to a respondent.
-- ON DELETE SET NULL so removing a respondent does not delete the sensor row.
ALTER TABLE sensor_mac
    ADD respondent_id UNIQUEIDENTIFIER NULL;
GO

ALTER TABLE sensor_mac
    ADD CONSTRAINT FK_sensor_mac_respondent
        FOREIGN KEY (respondent_id) REFERENCES identity_user(id)
        ON DELETE SET NULL;
GO

-- Non-unique: uniqueness of "one sensor type per respondent" and "one respondent per sensor_mac"
-- is enforced on respondent_sensor_assignment instead (see V31); this is a supporting index for
-- respondent-scoped lookups on this FK-like column only.
CREATE INDEX IX_sensor_mac_respondent_id
    ON sensor_mac (respondent_id)
    WHERE respondent_id IS NOT NULL;
GO
