ALTER TABLE sensor_data ADD CONSTRAINT UQ_sensor_data_date_time_respondent_id
UNIQUE (date_time, respondent_id);
