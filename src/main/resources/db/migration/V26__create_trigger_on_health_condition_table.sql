CREATE TRIGGER health_condition_after_insert
ON health_condition
AFTER INSERT
AS
BEGIN
    INSERT INTO respondents_group (name)
    SELECT 'Kategoria stanu zdrowia ' + inserted.display
    FROM inserted;
END;
GO

CREATE TRIGGER health_condition_after_update
ON health_condition
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
        SET rg.name = 'Kategoria stanu zdrowia ' + @name
        FROM respondents_group rg
        JOIN deleted d ON rg.name = 'Kategoria stanu zdrowia ' + d.display;
    END;
    SET NOCOUNT OFF;
END;