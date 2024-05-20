CREATE TRIGGER quality_of_sleep_after_insert
ON quality_of_sleep
AFTER INSERT
AS
BEGIN
    INSERT INTO respondents_group (name)
    SELECT 'Kategoria jakości snu ' + inserted.display
    FROM inserted;
END;
GO

CREATE TRIGGER quality_of_sleep_after_update
ON quality_of_sleep
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
        SET rg.name = 'Kategoria jakości snu ' + @name
        FROM respondents_group rg
        JOIN deleted d ON rg.name = 'Kategoria jakości snu ' + d.display;
    END;
    SET NOCOUNT OFF;
END;