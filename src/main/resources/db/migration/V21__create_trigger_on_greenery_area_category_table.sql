CREATE TRIGGER greenery_area_category_after_insert
ON greenery_area_category
AFTER INSERT
AS
BEGIN
    INSERT INTO respondents_group (name)
    SELECT 'Kategoria terenu zielonego ' + inserted.display
    FROM inserted;
END;
GO

CREATE TRIGGER greenery_area_category_after_update
ON greenery_area_category
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @name NVARCHAR(255);

    SELECT @name = inserted.display
    FROM inserted;

    IF UPDATE(display)
    BEGIN
        UPDATE rg
        SET rg.name = 'Kategoria terenu zielonego ' + @name
        FROM respondents_group rg
        JOIN deleted d ON rg.name = 'Kategoria terenu zielonego ' + d.display;
    END;
    SET NOCOUNT OFF;
END;