ALTER TABLE survey_settings
    ADD csv_column_separator NVARCHAR(1) NOT NULL
        CONSTRAINT DF_survey_settings_csv_column_separator DEFAULT ',';

ALTER TABLE survey_settings
    ADD csv_decimal_separator NVARCHAR(1) NOT NULL
        CONSTRAINT DF_survey_settings_csv_decimal_separator DEFAULT '.';
