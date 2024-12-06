CREATE TABLE section_to_user_group (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    section_id UNIQUEIDENTIFIER NOT NULL,
    group_id UNIQUEIDENTIFIER NOT NULL,
    row_version TIMESTAMP NOT NULL,
    FOREIGN KEY (section_id) REFERENCES survey_section(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES respondents_group(id) ON DELETE CASCADE
);
