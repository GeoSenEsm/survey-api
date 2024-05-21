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
        JOIN respondents_group rg ON rg.name = 'Kategoria wiekowa ' + gac.display;

    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN occupation_category gac ON rd.occupation_category_id = gac.id
        JOIN respondents_group rg ON rg.name = 'Kategoria zatrudnienia ' + gac.display;

    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN education_category gac ON rd.education_category_id = gac.id
        JOIN respondents_group rg ON rg.name = 'Kategoria wykształcenia ' + gac.display;

    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN health_condition gac ON rd.health_condition_id = gac.id
        JOIN respondents_group rg ON rg.name = 'Kategoria stanu zdrowia ' + gac.display;

    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN medication_use gac ON rd.medication_use_id = gac.id
        JOIN respondents_group rg ON rg.name = 'Kategoria stosowania leków ' + gac.display;

    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN life_satisfaction gac ON rd.life_satisfaction_id = gac.id
        JOIN respondents_group rg ON rg.name = 'Kategoria zadowolenia z życia ' + gac.display;

    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN stress_level gac ON rd.stress_level_id = gac.id
        JOIN respondents_group rg ON rg.name = 'Kategoria poziomu stresu ' + gac.display;

    INSERT INTO respondent_to_group (respondent_id, group_id)
        SELECT
            rd.id AS respondent_id,
            rg.id AS group_id
        FROM inserted rd
        JOIN quality_of_sleep gac ON rd.quality_of_sleep_id = gac.id
        JOIN respondents_group rg ON rg.name = 'Kategoria jakości snu ' + gac.display;

    INSERT INTO respondent_to_group (respondent_id, group_id)
    SELECT
        rd.id AS respondent_id,
        rg.id AS group_id
    FROM inserted rd
    JOIN greenery_area_category gac ON rd.greenery_area_category_id = gac.id
    JOIN respondents_group rg ON rg.name = 'Kategoria terenu zielonego ' + gac.display;

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
            JOIN respondents_group new_rg ON new_rg.name = 'Kategoria wiekowa ' + new_gac.display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN age_category old_gac ON old_rd.age_category_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.name = 'Kategoria wiekowa ' + old_gac.display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(occupation_category_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN occupation_category new_gac ON new_rd.occupation_category_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.name = 'Kategoria zatrudnienia ' + new_gac.display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN occupation_category old_gac ON old_rd.occupation_category_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.name = 'Kategoria zatrudnienia ' + old_gac.display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(education_category_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN education_category new_gac ON new_rd.education_category_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.name = 'Kategoria wykształcenia ' + new_gac.display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN education_category old_gac ON old_rd.education_category_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.name = 'Kategoria wykształcenia ' + old_gac.display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(health_condition_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN health_condition new_gac ON new_rd.health_condition_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.name = 'Kategoria stanu zdrowia ' + new_gac.display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN health_condition old_gac ON old_rd.health_condition_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.name = 'Kategoria stanu zdrowia ' + old_gac.display
            WHERE rtg.group_id = old_rg.id;
        END;

IF UPDATE(medication_use_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN medication_use new_gac ON new_rd.medication_use_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.name = 'Kategoria stosowania leków ' + new_gac.display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN medication_use old_gac ON old_rd.medication_use_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.name = 'Kategoria stosowania leków ' + old_gac.display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(life_satisfaction_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN life_satisfaction new_gac ON new_rd.life_satisfaction_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.name = 'Kategoria zadowolenia z życia ' + new_gac.display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN life_satisfaction old_gac ON old_rd.life_satisfaction_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.name = 'Kategoria zadowolenia z życia ' + old_gac.display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(stress_level_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN stress_level new_gac ON new_rd.stress_level_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.name = 'Kategoria poziomu stresu ' + new_gac.display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN stress_level old_gac ON old_rd.stress_level_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.name = 'Kategoria poziomu stresu ' + old_gac.display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(quality_of_sleep_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN quality_of_sleep new_gac ON new_rd.quality_of_sleep_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.name = 'Kategoria jakości snu ' + new_gac.display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN quality_of_sleep old_gac ON old_rd.quality_of_sleep_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.name = 'Kategoria jakości snu ' + old_gac.display
            WHERE rtg.group_id = old_rg.id;
        END;

    IF UPDATE(greenery_area_category_id)
        BEGIN
            UPDATE rtg
            SET rtg.group_id = new_rg.id
            FROM respondent_to_group rtg
            JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
            JOIN greenery_area_category new_gac ON new_rd.greenery_area_category_id = new_gac.id
            JOIN respondents_group new_rg ON new_rg.name = 'Kategoria terenu zielonego ' + new_gac.display
            JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
            JOIN greenery_area_category old_gac ON old_rd.greenery_area_category_id = old_gac.id
            JOIN respondents_group old_rg ON old_rg.name = 'Kategoria terenu zielonego ' + old_gac.display
            WHERE rtg.group_id = old_rg.id;
        END;

    SET NOCOUNT OFF;
END;
GO