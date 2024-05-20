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
    CREATE TRIGGER ' + @TableName + 'assign_to_group_after_insert
    ON respondent_data
    AFTER INSERT
    AS
    BEGIN
        SET NOCOUNT ON;

        INSERT INTO respondent_to_group (respondent_id, group_id)
            SELECT
                rd.id AS respondent_id,
                rg.id AS group_id
            FROM inserted rd
            JOIN respondents_group rg ON rg.name = ''' + @DynamicText + ''' + (SELECT display FROM ' + @TableName + ' WHERE id = rd.' + @TableName + '_id);

        SET NOCOUNT OFF;
    END;
    ';

    EXEC sp_executesql @SQL;

    FETCH NEXT FROM TableCursor INTO @TableName, @DynamicText;
END

CLOSE TableCursor;
DEALLOCATE TableCursor;