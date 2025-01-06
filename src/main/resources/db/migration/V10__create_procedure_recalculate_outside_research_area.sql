CREATE OR ALTER PROCEDURE RecalculateOutsideResearchArea
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (SELECT 1 FROM research_area)
    BEGIN
        DECLARE @polygon geography;

        SELECT @polygon = geography::STGeomFromText(
            'POLYGON((' +
            STRING_AGG(CAST(longitude AS NVARCHAR(20)) + ' ' + CAST(latitude AS NVARCHAR(20)), ', ') WITHIN GROUP (ORDER BY [order]) +
            ', ' +
            (SELECT CAST(longitude AS NVARCHAR(20)) + ' ' + CAST(latitude AS NVARCHAR(20))
             FROM research_area
             WHERE [order] = (SELECT MIN([order]) FROM research_area)) +
            '))', 4326
        ).MakeValid()
        FROM research_area;

        UPDATE ld
        SET outside_research_area =
            CASE
                WHEN @polygon.STContains(geography::Point(ld.latitude, ld.longitude, 4326)) = 0 THEN 1
                ELSE 0
            END
        FROM localization_data ld;
    END
    ELSE
    BEGIN
        UPDATE localization_data
        SET outside_research_area = NULL;
    END
END;