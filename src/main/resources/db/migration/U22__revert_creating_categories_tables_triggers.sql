DECLARE @TriggerName NVARCHAR(MAX);
DECLARE @DropSQL NVARCHAR(MAX);

DECLARE TriggerCursor CURSOR FOR
SELECT name
FROM sys.triggers
WHERE name LIKE '%_after_update' AND name like '%_after_insert';

OPEN TriggerCursor;
FETCH NEXT FROM TriggerCursor INTO @TriggerName;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @DropSQL = 'DROP TRIGGER ' + @TriggerName;
    EXEC sp_executesql @DropSQL;

    FETCH NEXT FROM TriggerCursor INTO @TriggerName;
END

CLOSE TriggerCursor;
DEALLOCATE TriggerCursor;
