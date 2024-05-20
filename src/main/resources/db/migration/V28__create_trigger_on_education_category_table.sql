CREATE TRIGGER education_category_after_insert
ON education_category
AFTER INSERT
AS
BEGIN
    INSERT INTO respondents_group (name)
    SELECT 'Kategoria wykształcenia ' + inserted.display
    FROM inserted;
END;
GO

CREATE TRIGGER education_category_after_update
ON education_category
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
        SET rg.name = 'Kategoria wykształcenia ' + @name
        FROM respondents_group rg
        JOIN deleted d ON rg.name = 'Kategoria wykształcenia ' + d.display;
    END;
    SET NOCOUNT OFF;
END;