CREATE OR ALTER TRIGGER trg_compute_outside_research_area_on_insert
ON localization_data
AFTER INSERT
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
        FROM localization_data ld
        INNER JOIN inserted i ON ld.id = i.id;
    END
    ELSE
    BEGIN
        UPDATE ld
        SET outside_research_area = NULL
        FROM localization_data ld
        INNER JOIN inserted i ON ld.id = i.id;
    END
END;
GO
