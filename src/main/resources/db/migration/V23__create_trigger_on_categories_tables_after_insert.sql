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
    CREATE TRIGGER ' + @TableName + '_after_insert
    ON ' + @TableName + '
    AFTER INSERT
    AS
    BEGIN
        SET NOCOUNT ON;

        INSERT INTO respondents_group (name)
        SELECT ''' + @DynamicText + ''' + i.display
        FROM inserted i;

        SET NOCOUNT OFF;
    END;
    ';

    EXEC sp_executesql @SQL;

    FETCH NEXT FROM TableCursor INTO @TableName, @DynamicText;
END

CLOSE TableCursor;
DEALLOCATE TableCursor;