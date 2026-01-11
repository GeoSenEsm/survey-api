-- Optimized index strategy for localization_data table
-- Based on query pattern analysis:
--   1. date_time filter: ALWAYS present (100% of queries)
--   2. survey_id filter: Common (~60% of queries) via participation_id join
--   3. respondent_id filter: Optional (~20% of queries)
--   4. outside_research_area filter: Optional (~10% of queries)

-- Primary index on date_time for queries filtering by date range (most common pattern)
-- This improves performance for all queries since date filter is always present
CREATE NONCLUSTERED INDEX IX_localization_data_date_time
ON [dbo].[localization_data] ([date_time] ASC)
INCLUDE ([respondent_id], [participation_id], [latitude], [longitude], [outside_research_area], [accuracy_meters])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON);
GO

-- Note on existing indexes:
-- 1. UNIQUE (respondent_id, date_time) - Handles respondent+date queries and enforces uniqueness
-- 2. idx_localization_data_participation_outside (participation_id, outside_research_area) - Handles survey+area queries
-- 3. New IX_localization_data_date_time - Optimizes date-first queries (most common)
--
-- SQL Server query optimizer will choose the best index based on query predicates and statistics:
-- - Date only: Uses IX_localization_data_date_time (SEEK)
-- - Date + Respondent: Uses UNIQUE index or new index (both fast)
-- - Date + Survey: Uses new index + JOIN (fast, date narrows result set)
-- - Date + Survey + Area: Optimizer chooses between new index and participation index

