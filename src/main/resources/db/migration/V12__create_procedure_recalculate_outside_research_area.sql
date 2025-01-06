CREATE OR ALTER PROCEDURE RecalculateOutsideResearchArea
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @polygon geography;

    SELECT @polygon = polygon FROM stored_polygon WHERE id = 1;

    IF @polygon IS NOT NULL
    BEGIN
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
GO
