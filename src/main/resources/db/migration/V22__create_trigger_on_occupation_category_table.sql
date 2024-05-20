CREATE TRIGGER occupation_category_after_insert
ON occupation_category
AFTER INSERT
AS
BEGIN
    INSERT INTO respondents_group (name)
    SELECT 'Kategoria zatrudnienia ' + inserted.display
    FROM inserted;
END;
GO

CREATE TRIGGER occupation_category_after_update
ON occupation_category
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
        SET rg.name = 'Kategoria zatrudnienia ' + @name
        FROM respondents_group rg
        JOIN deleted d ON rg.name = 'Kategoria zatrudnienia ' + d.display;
    END;
    SET NOCOUNT OFF;
END;