ALTER TABLE sensor_data
ADD survey_participation_id UNIQUEIDENTIFIER NULL DEFAULT NULL REFERENCES survey_participation(id);