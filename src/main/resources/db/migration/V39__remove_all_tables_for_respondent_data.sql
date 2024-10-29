-- Drop foreign keys for all tables and then drop the tables

DECLARE @sql NVARCHAR(MAX);

-- greenery_area_category
SET @sql = '';
SELECT @sql = @sql + 'ALTER TABLE ' + QUOTENAME(OBJECT_NAME(parent_object_id)) +
               ' DROP CONSTRAINT ' + QUOTENAME(name) + '; '
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('greenery_area_category');
EXEC sp_executesql @sql;
DROP TABLE greenery_area_category;

-- occupation_category
SET @sql = '';
SELECT @sql = @sql + 'ALTER TABLE ' + QUOTENAME(OBJECT_NAME(parent_object_id)) +
               ' DROP CONSTRAINT ' + QUOTENAME(name) + '; '
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('occupation_category');
EXEC sp_executesql @sql;
DROP TABLE occupation_category;

-- age_category
SET @sql = '';
SELECT @sql = @sql + 'ALTER TABLE ' + QUOTENAME(OBJECT_NAME(parent_object_id)) +
               ' DROP CONSTRAINT ' + QUOTENAME(name) + '; '
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('age_category');
EXEC sp_executesql @sql;
DROP TABLE age_category;

-- life_satisfaction
SET @sql = '';
SELECT @sql = @sql + 'ALTER TABLE ' + QUOTENAME(OBJECT_NAME(parent_object_id)) +
               ' DROP CONSTRAINT ' + QUOTENAME(name) + '; '
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('life_satisfaction');
EXEC sp_executesql @sql;
DROP TABLE life_satisfaction;

-- stress_level
SET @sql = '';
SELECT @sql = @sql + 'ALTER TABLE ' + QUOTENAME(OBJECT_NAME(parent_object_id)) +
               ' DROP CONSTRAINT ' + QUOTENAME(name) + '; '
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('stress_level');
EXEC sp_executesql @sql;
DROP TABLE stress_level;

-- health_condition
SET @sql = '';
SELECT @sql = @sql + 'ALTER TABLE ' + QUOTENAME(OBJECT_NAME(parent_object_id)) +
               ' DROP CONSTRAINT ' + QUOTENAME(name) + '; '
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('health_condition');
EXEC sp_executesql @sql;
DROP TABLE health_condition;

-- quality_of_sleep
SET @sql = '';
SELECT @sql = @sql + 'ALTER TABLE ' + QUOTENAME(OBJECT_NAME(parent_object_id)) +
               ' DROP CONSTRAINT ' + QUOTENAME(name) + '; '
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('quality_of_sleep');
EXEC sp_executesql @sql;
DROP TABLE quality_of_sleep;

-- education_category
SET @sql = '';
SELECT @sql = @sql + 'ALTER TABLE ' + QUOTENAME(OBJECT_NAME(parent_object_id)) +
               ' DROP CONSTRAINT ' + QUOTENAME(name) + '; '
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('education_category');
EXEC sp_executesql @sql;
DROP TABLE education_category;

-- medication_use
SET @sql = '';
SELECT @sql = @sql + 'ALTER TABLE ' + QUOTENAME(OBJECT_NAME(parent_object_id)) +
               ' DROP CONSTRAINT ' + QUOTENAME(name) + '; '
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('medication_use');
EXEC sp_executesql @sql;
DROP TABLE medication_use;
