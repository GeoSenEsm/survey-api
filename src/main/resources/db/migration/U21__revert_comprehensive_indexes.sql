-- ============================================================================
-- REVERT V21: Restore database to pre-V21 state
-- ============================================================================
-- This migration:
-- 1. Drops all new indexes created in V21
-- 2. Restores the old row_version indexes from V2
-- 3. Restores the old V17 index
-- ============================================================================

-- Drop all new indexes created in V21
DROP INDEX IF EXISTS IX_survey_participation_survey_date ON [dbo].[survey_participation];
DROP INDEX IF EXISTS IX_survey_participation_date ON [dbo].[survey_participation];
DROP INDEX IF EXISTS IX_question_answer_participation ON [dbo].[question_answer];
DROP INDEX IF EXISTS IX_question_answer_question ON [dbo].[question_answer];
DROP INDEX IF EXISTS IX_option_selection_question_answer ON [dbo].[option_selection];
DROP INDEX IF EXISTS IX_option_selection_option ON [dbo].[option_selection];
DROP INDEX IF EXISTS IX_text_answer_question_answer ON [dbo].[text_answer];
DROP INDEX IF EXISTS IX_question_section ON [dbo].[question];
DROP INDEX IF EXISTS IX_option_question ON [dbo].[option];
DROP INDEX IF EXISTS IX_survey_section_survey ON [dbo].[survey_section];
DROP INDEX IF EXISTS IX_number_range_question ON [dbo].[number_range];
DROP INDEX IF EXISTS IX_survey_sending_policy_survey ON [dbo].[survey_sending_policy];
DROP INDEX IF EXISTS IX_survey_participation_time_slot_policy ON [dbo].[survey_participation_time_slot];
DROP INDEX IF EXISTS IX_respondent_to_group_respondent ON [dbo].[respondent_to_group];
DROP INDEX IF EXISTS IX_respondent_to_group_group ON [dbo].[respondent_to_group];
DROP INDEX IF EXISTS IX_section_to_user_group_section ON [dbo].[section_to_user_group];
DROP INDEX IF EXISTS IX_section_to_user_group_group ON [dbo].[section_to_user_group];
DROP INDEX IF EXISTS IX_respondent_data_option_question ON [dbo].[respondent_data_option];
DROP INDEX IF EXISTS IX_respondent_data_option_option ON [dbo].[respondent_data_option];
DROP INDEX IF EXISTS IX_initial_survey_question_survey ON [dbo].[initial_survey_question];
DROP INDEX IF EXISTS IX_initial_survey_option_question ON [dbo].[initial_survey_option];
DROP INDEX IF EXISTS IX_identity_user_role ON [dbo].[identity_user];
DROP INDEX IF EXISTS IX_survey_creation_date ON [dbo].[survey];
DROP INDEX IF EXISTS IX_survey_state ON [dbo].[survey];
GO

-- Restore old row_version indexes from V2 (that were dropped in V21)
CREATE INDEX idx_survey_row_version ON survey (row_version);
CREATE INDEX idx_survey_section_row_version ON survey_section (row_version);
CREATE INDEX idx_question_row_version ON question (row_version);
CREATE INDEX idx_option_row_version ON [option] (row_version);
CREATE INDEX idx_number_range_row_version ON number_range (row_version);
CREATE INDEX idx_section_to_user_group_row_version ON section_to_user_group (row_version);
CREATE INDEX idx_survey_sending_policy_row_version ON survey_sending_policy (row_version);
CREATE INDEX idx_survey_participation_time_slot_row_version ON survey_participation_time_slot (row_version);
CREATE INDEX idx_survey_participation_respondent_row_version ON survey_participation (respondent_id, row_version);
GO

-- Restore old V17 index (that was dropped in V21)
CREATE INDEX idx_survey_participation_filters ON survey_participation(survey_id, date, respondent_id);
GO

