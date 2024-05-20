CREATE TRIGGER medication_use_after_insert
ON medication_use
AFTER INSERT
AS
BEGIN
    INSERT INTO respondents_group (name)
    SELECT 'Kategoria stosowania leków ' + inserted.display
    FROM inserted;
END;
GO

CREATE TRIGGER medication_use_after_update
ON medication_use
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
        SET rg.name = 'Kategoria stosowania leków ' + @name
        FROM respondents_group rg
        JOIN deleted d ON rg.name = 'Kategoria stosowania leków ' + d.display;
    END;
    SET NOCOUNT OFF;
END;