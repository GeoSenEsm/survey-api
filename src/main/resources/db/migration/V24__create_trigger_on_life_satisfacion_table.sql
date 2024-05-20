CREATE TRIGGER life_satisfaction_after_insert
ON life_satisfaction
AFTER INSERT
AS
BEGIN
    INSERT INTO respondents_group (name)
    SELECT 'Kategoria zadowolenia z życia ' + inserted.display
    FROM inserted;
END;
GO

CREATE TRIGGER life_satisfaction_after_update
ON life_satisfaction
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
        SET rg.name = 'Kategoria zadowolenia z życia ' + @name
        FROM respondents_group rg
        JOIN deleted d ON rg.name = 'Kategoria zadowolenia z życia ' + d.display;
    END;
    SET NOCOUNT OFF;
END;