CREATE UNIQUE NONCLUSTERED INDEX UQ_Respondent_Participation
ON localization_data (respondent_id, participation_id)
WHERE participation_id IS NOT NULL;