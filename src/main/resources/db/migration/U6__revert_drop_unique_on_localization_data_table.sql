ALTER TABLE localization_data ADD CONSTRAINT UQ_localization_data_date_time_respondent_id
UNIQUE (date_time, respondent_id);
