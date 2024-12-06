DROP TRIGGER age_category_after_update;
DROP TRIGGER education_category_after_update;
DROP TRIGGER greenery_area_category_after_update;
DROP TRIGGER health_condition_after_update;
DROP TRIGGER life_satisfaction_after_update;
DROP TRIGGER medication_use_after_update;
DROP TRIGGER occupation_category_after_update;
DROP TRIGGER quality_of_sleep_after_update;
DROP TRIGGER stress_level_after_update;

DROP TRIGGER age_category_after_insert;
DROP TRIGGER education_category_after_insert;
DROP TRIGGER greenery_area_category_after_insert;
DROP TRIGGER health_condition_after_insert;
DROP TRIGGER life_satisfaction_after_insert;
DROP TRIGGER medication_use_after_insert;
DROP TRIGGER occupation_category_after_insert;
DROP TRIGGER quality_of_sleep_after_insert;
DROP TRIGGER stress_level_after_insert;

DROP TRIGGER respondent_data_assign_to_groups_after_update;
DROP TRIGGER respondent_data_assign_to_groups_after_insert;
GO

EXEC sp_rename 'age_category.display', 'polish_display', 'COLUMN';
EXEC sp_rename 'education_category.display', 'polish_display', 'COLUMN';
EXEC sp_rename 'greenery_area_category.display', 'polish_display', 'COLUMN';
EXEC sp_rename 'health_condition.display', 'polish_display', 'COLUMN';
EXEC sp_rename 'life_satisfaction.display', 'polish_display', 'COLUMN';
EXEC sp_rename 'medication_use.display', 'polish_display', 'COLUMN';
EXEC sp_rename 'occupation_category.display', 'polish_display', 'COLUMN';
EXEC sp_rename 'quality_of_sleep.display', 'polish_display', 'COLUMN';
EXEC sp_rename 'stress_level.display', 'polish_display', 'COLUMN';
EXEC sp_rename 'respondents_group.name', 'polish_name', 'COLUMN';
GO

ALTER TABLE age_category ADD english_display NVARCHAR(255) NULL;
ALTER TABLE education_category ADD english_display NVARCHAR(255) NULL;
ALTER TABLE greenery_area_category ADD english_display NVARCHAR(255) NULL;
ALTER TABLE health_condition ADD english_display NVARCHAR(255) NULL;
ALTER TABLE life_satisfaction ADD english_display NVARCHAR(255) NULL;
ALTER TABLE medication_use ADD english_display NVARCHAR(255) NULL;
ALTER TABLE occupation_category ADD english_display NVARCHAR(255) NULL;
ALTER TABLE quality_of_sleep ADD english_display NVARCHAR(255) NULL;
ALTER TABLE stress_level ADD english_display NVARCHAR(255) NULL;
ALTER TABLE respondents_group ADD english_name NVARCHAR(250) NULL;
GO


UPDATE age_category SET english_display = polish_display;
UPDATE education_category SET english_display = polish_display;
UPDATE greenery_area_category SET english_display = polish_display;
UPDATE health_condition SET english_display = polish_display;
UPDATE life_satisfaction SET english_display = polish_display;
UPDATE medication_use SET english_display = polish_display;
UPDATE occupation_category SET english_display = polish_display;
UPDATE quality_of_sleep SET english_display = polish_display;
UPDATE stress_level SET english_display = polish_display;
UPDATE respondents_group SET english_name = polish_name;

ALTER TABLE age_category ALTER COLUMN english_display NVARCHAR(255) NULL;
ALTER TABLE education_category ALTER COLUMN english_display NVARCHAR(255) NULL;
ALTER TABLE greenery_area_category ALTER COLUMN english_display NVARCHAR(255) NULL;
ALTER TABLE health_condition ALTER COLUMN english_display NVARCHAR(255) NULL;
ALTER TABLE life_satisfaction ALTER COLUMN english_display NVARCHAR(255) NULL;
ALTER TABLE medication_use ALTER COLUMN english_display NVARCHAR(255) NULL;
ALTER TABLE occupation_category ALTER COLUMN english_display NVARCHAR(255) NULL;
ALTER TABLE quality_of_sleep ALTER COLUMN english_display NVARCHAR(255) NULL;
ALTER TABLE stress_level ALTER COLUMN english_display NVARCHAR(255) NULL;
ALTER TABLE respondents_group ALTER COLUMN english_name NVARCHAR(250) NULL;

ALTER TABLE age_category ADD UNIQUE(english_display);
ALTER TABLE education_category ADD UNIQUE(english_display);
ALTER TABLE greenery_area_category ADD UNIQUE(english_display);
ALTER TABLE health_condition ADD UNIQUE(english_display);
ALTER TABLE life_satisfaction ADD UNIQUE(english_display);
ALTER TABLE medication_use ADD UNIQUE(english_display);
ALTER TABLE occupation_category ADD UNIQUE(english_display);
ALTER TABLE quality_of_sleep ADD UNIQUE(english_display);
ALTER TABLE stress_level ADD UNIQUE(english_display);
ALTER TABLE respondents_group ADD UNIQUE(english_name);

UPDATE education_category
SET english_display = CASE polish_display
    WHEN N'podstawowe' THEN 'primary'
    WHEN N'średnie' THEN 'secondary'
    WHEN N'wyższe' THEN 'higher'
    WHEN N'zawodowe' THEN 'vocational'
    ELSE english_display
END;

UPDATE greenery_area_category
SET english_display = CASE polish_display
    WHEN N'małe zagęszczenie' THEN 'low density'
    WHEN N'średnie zagęszczenie' THEN 'medium density'
    WHEN N'wysokie zagęszczenie' THEN 'high density'
    ELSE english_display
END;

UPDATE health_condition
SET english_display = CASE polish_display
    WHEN N'niska' THEN 'low'
    WHEN N'średnia' THEN 'medium'
    WHEN N'wysoka' THEN 'high'
    ELSE english_display
END

UPDATE life_satisfaction
SET english_display = CASE polish_display
    WHEN N'niskie' THEN 'low'
    WHEN N'średnie' THEN 'medium'
    WHEN N'wysokie' THEN 'high'
    ELSE english_display
END

UPDATE medication_use
SET english_display = CASE polish_display
    WHEN N'tak' THEN 'yes'
    WHEN N'nie' THEN 'no'
    ELSE english_display
END

UPDATE occupation_category
SET english_display = CASE polish_display
    WHEN N'zatrudniony' THEN 'employed'
    WHEN N'niezatrudniony' THEN 'unemployed'
    ELSE english_display
END

UPDATE quality_of_sleep
SET english_display = CASE polish_display
    WHEN N'niska' THEN 'low'
    WHEN N'średnia' THEN 'medium'
    WHEN N'wysoka' THEN 'high'
    ELSE english_display
END

UPDATE stress_level
SET english_display = CASE polish_display
    WHEN N'niski' THEN 'low'
    WHEN N'wysoki' THEN 'high'
    WHEN N'średni' THEN 'medium'
    ELSE english_display
END

UPDATE respondents_group
SET english_name = CASE polish_name
    WHEN N'Zadowolenie z zycia średnie' THEN 'Life satisfaction medium'
    WHEN N'Uzycie leków nie' THEN 'Medication use no'
    WHEN N'Kategoria wyksztalcenia średnie' THEN 'Education category secondary'
    WHEN N'Kategoria terenu zielonego małe zagęszczenie' THEN 'Greenery area category low density'
    WHEN N'Kategoria zatrudnienia niezatrudniony' THEN 'Occupation category unemployed'
    WHEN N'Kategoria wyksztalcenia podstawowe' THEN 'Education category primary'
    WHEN N'Poziom stresu średni' THEN 'Stress level medium'
    WHEN N'Kategoria wiekowa 50-59' THEN 'Age category 50-59'
    WHEN N'Stan zdrowia wysoka' THEN 'Health condition high'
    WHEN N'Stan zdrowia niska' THEN 'Health condition low'
    WHEN N'Uzycie leków tak' THEN 'Medication use yes'
    WHEN N'Kategoria terenu zielonego wysokie zagęszczenie' THEN 'Greenery area category high density'
    WHEN N'Stan zdrowia średnia' THEN 'Health condition medium'
    WHEN N'Kategoria wyksztalcenia zawodowe' THEN 'Education category vocational'
    WHEN N'Zadowolenie z zycia wysokie' THEN 'Life satisfaction high'
    WHEN N'Wszyscy' THEN 'All'
    WHEN N'Poziom stresu wysoki' THEN 'Stress level high'
    WHEN N'Kategoria wiekowa 60-69' THEN 'Age category 60-69'
    WHEN N'Kategoria wyksztalcenia wyższe' THEN 'Education category higher'
    WHEN N'Jakosc snu średnia' THEN 'Quality of sleep medium'
    WHEN N'Kategoria terenu zielonego średnie zagęszczenie' THEN 'Greenery area category medium density'
    WHEN N'Jakosc snu wysoka' THEN 'Quality of sleep high'
    WHEN N'Poziom stresu niski' THEN 'Stress level low'
    WHEN N'Zadowolenie z zycia niskie' THEN 'Life satisfaction low'
    WHEN N'Jakosc snu niska' THEN 'Quality of sleep low'
    WHEN N'Kategoria zatrudnienia zatrudniony' THEN 'Occupation category employed'
    WHEN N'Kategoria wiekowa 70+' THEN 'Age category 70+'
    ELSE english_name
END;
GO
-- update triggers, so they consider english language
CREATE TABLE #TemporaryCategories (
    TableName NVARCHAR(MAX),
    PolishCategoryName NVARCHAR(MAX),
    EnglishCategoryName NVARCHAR(MAX)
);

INSERT INTO #TemporaryCategories (TableName, PolishCategoryName, EnglishCategoryName)
VALUES
    ('greenery_area_category', 'Kategoria terenu zielonego ', 'Greenery area category '),
    ('occupation_category', 'Kategoria zatrudnienia ', 'Occupation category'),
    ('age_category', 'Kategoria wiekowa ', 'Age category '),
    ('life_satisfaction', 'Zadowolenie z życia ', 'Life satisfaction '),
    ('stress_level', 'Poziom stresu ', 'Stress level '),
    ('health_condition', 'Stan zdrowia ', 'Health condition '),
    ('quality_of_sleep', 'Jakość snu ', 'Quality of sleep '),
    ('education_category', 'Kategoria wykształcenia ', 'Education category '),
    ('medication_use', 'Użycie leków ', 'Medication use');

DECLARE @TableName NVARCHAR(MAX);
DECLARE @PL NVARCHAR(MAX);
DECLARE @EN NVARCHAR(MAX);
DECLARE @SQL NVARCHAR(MAX);

DECLARE TableCursor CURSOR FOR
SELECT TableName, PolishCategoryName, EnglishCategoryName FROM #TemporaryCategories;

OPEN TableCursor;
FETCH NEXT FROM TableCursor INTO @TableName, @PL, @EN;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @SQL = '
    CREATE TRIGGER ' + @TableName + '_after_update
    ON ' + @TableName + '
    AFTER UPDATE
    AS
    BEGIN
        SET NOCOUNT ON;

        IF UPDATE(polish_display) OR UPDATE(english_display)
        BEGIN
            UPDATE rg
            SET rg.polish_name = ''' + @PL + ''' + i.polish_display, rg.english_name  = ''' + @EN + ''' + i.english_display
            FROM respondents_group rg
            JOIN deleted d ON rg.polish_name = ''' + @PL + ''' + d.polish_display
            JOIN inserted i ON d.id = i.id;
        END;

        SET NOCOUNT OFF;
    END;
    ';

    EXEC sp_executesql @SQL;

    SET @SQL = '
    CREATE TRIGGER ' + @TableName + '_after_insert
    ON ' + @TableName + '
    AFTER INSERT
    AS
    BEGIN
        SET NOCOUNT ON;

        INSERT INTO respondents_group (polish_name, english_name)
        SELECT ''' + @PL + ''' + i.polish_display, ''' + @EN + ''' + i.english_display
        FROM inserted i;

        SET NOCOUNT OFF;
    END;
    ';

    EXEC sp_executesql @SQL;

    FETCH NEXT FROM TableCursor INTO @TableName, @PL, @EN;

END

CLOSE TableCursor;
DEALLOCATE TableCursor;
DROP TABLE #TemporaryCategories;
GO
--update trigger with auto assigning to groups

CREATE TRIGGER respondent_data_assign_to_groups_after_insert
ON respondent_data
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN age_category gac ON rd.age_category_id = gac.id
        JOIN respondents_group rg ON rg.polish_name = 'Kategoria wiekowa ' + gac.polish_display;

    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN occupation_category gac ON rd.occupation_category_id = gac.id
        JOIN respondents_group rg ON rg.polish_name = 'Kategoria zatrudnienia ' + gac.polish_display;

    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN education_category gac ON rd.education_category_id = gac.id
        JOIN respondents_group rg ON rg.polish_name = 'Kategoria wykształcenia ' + gac.polish_display;

    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN health_condition gac ON rd.health_condition_id = gac.id
        JOIN respondents_group rg ON rg.polish_name = 'Stan zdrowia ' + gac.polish_display;


    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN medication_use gac ON rd.medication_use_id = gac.id
        JOIN respondents_group rg ON rg.polish_name = 'Użycie leków ' + gac.polish_display;


    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN life_satisfaction gac ON rd.life_satisfaction_id = gac.id
        JOIN respondents_group rg ON rg.polish_name = 'Zadowolenie z życia ' + gac.polish_display;


    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN stress_level gac ON rd.stress_level_id = gac.id
        JOIN respondents_group rg ON rg.polish_name = 'Poziom stresu ' + gac.polish_display;


    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN quality_of_sleep gac ON rd.quality_of_sleep_id = gac.id
        JOIN respondents_group rg ON rg.polish_name = 'Jakość snu ' + gac.polish_display;


    INSERT INTO respondent_to_group (respondent_id, group_id)
    SELECT
        rd.id AS respondent_id,
        rg.id AS group_id
    FROM inserted rd
    JOIN greenery_area_category gac ON rd.greenery_area_category_id = gac.id
    JOIN respondents_group rg ON rg.polish_name = 'Kategoria terenu zielonego ' + gac.polish_display;

    SET NOCOUNT OFF;
END;
GO

CREATE TRIGGER respondent_data_assign_to_groups_after_update
ON respondent_data
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF UPDATE(age_category_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN age_category new_gac ON new_rd.age_category_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.polish_name = 'Kategoria wiekowa ' + new_gac.polish_display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN age_category old_gac ON old_rd.age_category_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.polish_name = 'Kategoria wiekowa ' + old_gac.polish_display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(occupation_category_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN occupation_category new_gac ON new_rd.occupation_category_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.polish_name = 'Kategoria zatrudnienia ' + new_gac.polish_display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN occupation_category old_gac ON old_rd.occupation_category_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.polish_name = 'Kategoria zatrudnienia ' + old_gac.polish_display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(education_category_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN education_category new_gac ON new_rd.education_category_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.polish_name = 'Kategoria wykształcenia ' + new_gac.polish_display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN education_category old_gac ON old_rd.education_category_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.polish_name = 'Kategoria wykształcenia ' + old_gac.polish_display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(health_condition_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN health_condition new_gac ON new_rd.health_condition_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.polish_name = 'Stan zdrowia ' + new_gac.polish_display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN health_condition old_gac ON old_rd.health_condition_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.polish_name = 'Stan zdrowia ' + old_gac.polish_display
            WHERE rtg.group_id = old_rg.id;
        END;

IF UPDATE(medication_use_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN medication_use new_gac ON new_rd.medication_use_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.polish_name = 'Użycie leków ' + new_gac.polish_display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN medication_use old_gac ON old_rd.medication_use_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.polish_name = 'Użycie leków ' + old_gac.polish_display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(life_satisfaction_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN life_satisfaction new_gac ON new_rd.life_satisfaction_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.polish_name = 'Zadowolenie z życia ' + new_gac.polish_display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN life_satisfaction old_gac ON old_rd.life_satisfaction_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.polish_name = 'Zadowolenie z życia ' + old_gac.polish_display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(stress_level_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN stress_level new_gac ON new_rd.stress_level_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.polish_name = 'Poziom stresu ' + new_gac.polish_display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN stress_level old_gac ON old_rd.stress_level_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.polish_name = 'Poziom stresu ' + old_gac.polish_display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(quality_of_sleep_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN quality_of_sleep new_gac ON new_rd.quality_of_sleep_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.polish_name = 'Jakość snu ' + new_gac.polish_display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN quality_of_sleep old_gac ON old_rd.quality_of_sleep_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.polish_name = 'Jakość snu ' + old_gac.polish_display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(greenery_area_category_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN greenery_area_category new_gac ON new_rd.greenery_area_category_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.polish_name = 'Kategoria terenu zielonego ' + new_gac.polish_display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN greenery_area_category old_gac ON old_rd.greenery_area_category_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.polish_name = 'Kategoria terenu zielonego ' + old_gac.polish_display
            WHERE rtg.group_id = old_rg.id;
        END;

    SET NOCOUNT OFF;
END;
GO

ALTER TRIGGER [add_to_group_trigger]
ON [respondent_data]
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @groupId UNIQUEIDENTIFIER;

    SELECT @groupId = id
    FROM respondents_group
    WHERE polish_name = 'Wszyscy';

    INSERT INTO respondent_to_group (id, respondent_id, group_id)
    SELECT
        NEWID(),
        inserted.id,
        @groupId
    FROM inserted;

    SET NOCOUNT OFF;
END;
GO