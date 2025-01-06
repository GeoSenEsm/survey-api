CREATE OR ALTER PROCEDURE UpdateStoredPolygon
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

        IF EXISTS (SELECT 1 FROM stored_polygon WHERE id = 1)
        BEGIN
            UPDATE stored_polygon
            SET polygon = @polygon
            WHERE id = 1;
        END
        ELSE
        BEGIN
            INSERT INTO stored_polygon (id, polygon)
            VALUES (1, @polygon);
        END
    END
    ELSE
    BEGIN
        DELETE FROM stored_polygon WHERE id = 1;
    END
END;
GO
