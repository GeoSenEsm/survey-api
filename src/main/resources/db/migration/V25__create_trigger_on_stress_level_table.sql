CREATE TRIGGER stress_level_after_insert
ON stress_level
AFTER INSERT
AS
BEGIN
    INSERT INTO respondents_group (name)
    SELECT 'Kategoria poziomu stresu ' + inserted.display
    FROM inserted;
END;
GO

CREATE TRIGGER stress_level_after_update
ON stress_level
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
        SET rg.name = 'Kategoria poziomu stresu ' + @name
        FROM respondents_group rg
        JOIN deleted d ON rg.name = 'Kategoria poziomu stresu ' + d.display;
    END;
    SET NOCOUNT OFF;
END;