CREATE TABLE education_category (
    id INTEGER IDENTITY(1,1) PRIMARY KEY,
    display NVARCHAR(255) UNIQUE NOT NULL,
    row_version TIMESTAMP NOT NULL
);
