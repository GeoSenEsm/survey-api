CREATE INDEX idx_survey_row_version ON survey (row_version);
CREATE INDEX idx_survey_section_row_version ON survey_section (row_version);
CREATE INDEX idx_question_row_version ON question (row_version);
CREATE INDEX idx_option_row_version ON [option] (row_version);
CREATE INDEX idx_number_range_row_version ON number_range (row_version);
CREATE INDEX idx_section_to_user_group_row_version ON section_to_user_group (row_version);
CREATE INDEX idx_survey_sending_policy_row_version ON survey_sending_policy (row_version);
CREATE INDEX idx_survey_participation_time_slot_row_version ON survey_participation_time_slot (row_version);
CREATE INDEX idx_survey_participation_respondent_row_version
ON survey_participation (respondent_id, row_version);
