CREATE TABLE #TemporaryCategories (
    TableName NVARCHAR(MAX),
    CategoryName NVARCHAR(MAX)
);

INSERT INTO #TemporaryCategories (TableName, CategoryName)
VALUES
    ('greenery_area_category', 'Kategoria terenu zielonego '),
    ('occupation_category', 'Kategoria zatrudnienia '),
    ('age_category', 'Kategoria wiekowa '),
    ('life_satisfaction', 'Zadowolenie z życia '),
    ('stress_level', 'Poziom stresu '),
    ('health_condition', 'Stan zdrowia '),
    ('quality_of_sleep', 'Jakość snu '),
    ('education_category', 'Kategoria wykształcenia '),
    ('medication_use', 'Użycie leków ');
