CREATE TABLE stored_polygon(
    id INT PRIMARY KEY CHECK (id = 1),
    polygon geography NOT NULL
);