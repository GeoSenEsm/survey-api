CREATE INDEX idx_survey_participation_filters
    ON survey_participation(survey_id, date, respondent_id);

CREATE INDEX idx_localization_data_participation_outside
    ON localization_data(participation_id, outside_research_area);