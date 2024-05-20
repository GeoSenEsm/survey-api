CREATE TRIGGER age_category_after_insert
ON age_category
AFTER INSERT
AS
BEGIN
    INSERT INTO respondents_group (name)
    SELECT 'Kategoria wiekowa ' + inserted.display
    FROM inserted;
END;
GO

CREATE TRIGGER age_category_after_update
ON age_category
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
        SET rg.name = 'Kategoria wiekowa ' + @name
        FROM respondents_group rg
        JOIN deleted d ON rg.name = 'Kategoria wiekowa ' + d.display;
    END;
    SET NOCOUNT OFF;
END;