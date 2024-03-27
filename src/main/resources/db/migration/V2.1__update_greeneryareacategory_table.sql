INSERT INTO greenery_area_category (display)
SELECT 'low-density' WHERE NOT EXISTS (SELECT * FROM greenery_area_category WHERE display = 'low-density');

INSERT INTO greenery_area_category (display)
SELECT 'medium-density' WHERE NOT EXISTS (SELECT * FROM greenery_area_category WHERE display = 'medium-density');

INSERT INTO greenery_area_category (display)
SELECT 'high-density' WHERE NOT EXISTS (SELECT * FROM greenery_area_category WHERE display = 'high-density');