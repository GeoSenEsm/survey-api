EXEC sp_rename 'temperature_data', 'sensor_data';

ALTER TABLE sensor_data ADD humidity DECIMAL(5, 2);