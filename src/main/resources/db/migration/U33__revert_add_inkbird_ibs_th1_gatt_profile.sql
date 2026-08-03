DELETE FROM sensor_gatt_profile
WHERE sensor_type_id IN (
    SELECT id FROM sensor_type
    WHERE code = N'inkbird_ibs_th1'
);
GO

DELETE FROM sensor_parameter_source
WHERE sensor_type_id IN (
    SELECT id FROM sensor_type
    WHERE code = N'inkbird_ibs_th1'
);
GO

DELETE FROM sensor_type_setting
WHERE sensor_type_id IN (
    SELECT id FROM sensor_type
    WHERE code = N'inkbird_ibs_th1'
);
GO

DELETE FROM sensor_type
WHERE code = N'inkbird_ibs_th1';
GO
