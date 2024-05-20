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
    CREATE TRIGGER ' + @TableName + 'assign_to_group_after_update
    ON respondent_data
    AFTER UPDATE
    AS
    BEGIN
        SET NOCOUNT ON;
            IF UPDATE(' + @TableName + '_id)
                BEGIN
                    UPDATE rtg
                    SET rtg.group_id = new_rg.id
                    FROM respondent_to_group rtg
                    JOIN inserted new_rd ON rtg.respondent_id = new_rd.id
                    JOIN respondents_group new_rg ON new_rg.name = CONCAT(''' + @DynamicText + ''', (SELECT display FROM ' + @TableName + ' WHERE id = new_rd.' + @TableName + '_id))
                    JOIN deleted old_rd ON rtg.respondent_id = old_rd.id
                    JOIN respondents_group old_rg ON old_rg.name = CONCAT(''' + @DynamicText + ''', (SELECT display FROM ' + @TableName + ' WHERE id = old_rd.' + @TableName + '_id))
                    WHERE rtg.group_id = old_rg.id;
                END;


        SET NOCOUNT OFF;
    END;
    ';

    EXEC sp_executesql @SQL;

    FETCH NEXT FROM TableCursor INTO @TableName, @DynamicText;
END

CLOSE TableCursor;
DEALLOCATE TableCursor;