CREATE TABLE research_area (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    latitude DECIMAL(8, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL,
    row_version TIMESTAMP NOT NULL,
);