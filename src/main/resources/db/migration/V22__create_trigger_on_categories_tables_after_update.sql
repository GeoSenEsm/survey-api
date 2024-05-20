DECLARE @TableName NVARCHAR(MAX);
DECLARE @DynamicText NVARCHAR(MAX);
DECLARE @SQL NVARCHAR(MAX);

DECLARE TableCursor CURSOR FOR
SELECT TableName, CategoryName FROM #TemporaryCategories;

OPEN TableCursor;
FETCH NEXT FROM TableCursor INTO @TableName, @DynamicText;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @SQL = '
    CREATE TRIGGER ' + @TableName + '_after_update
    ON ' + @TableName + '
    AFTER UPDATE
    AS
    BEGIN
        SET NOCOUNT ON;

        IF UPDATE(display)
        BEGIN
            UPDATE rg
            SET rg.name = ''' + @DynamicText + ''' + i.display
            FROM respondents_group rg
            JOIN deleted d ON rg.name = ''' + @DynamicText + ''' + d.display
            JOIN inserted i ON d.id = i.id;
        END;

        SET NOCOUNT OFF;
    END;
    ';

    EXEC sp_executesql @SQL;

    FETCH NEXT FROM TableCursor INTO @TableName, @DynamicText;

END

CLOSE TableCursor;
DEALLOCATE TableCursor;