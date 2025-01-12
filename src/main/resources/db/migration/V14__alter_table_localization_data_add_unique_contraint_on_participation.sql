WITH CTE AS (
    SELECT
        respondent_id,
        participation_id,
        ROW_NUMBER() OVER (PARTITION BY respondent_id, participation_id ORDER BY respondent_id) AS rn,
        id
    FROM localization_data
    WHERE participation_id IS NOT NULL
)
DELETE FROM localization_data
WHERE id IN (
    SELECT id
    FROM CTE
    WHERE rn > 1
);

CREATE UNIQUE NONCLUSTERED INDEX UQ_Respondent_Participation
ON localization_data (respondent_id, participation_id)
WHERE participation_id IS NOT NULL;