INSERT INTO sensor_type (id, code, name, integration_mode, adapter_key)
VALUES ('B1000000-0000-4000-8000-000000000005', N'inkbird_ibs_th1', N'Inkbird IBS-TH1 / TH1 Mini / TH1 Plus', N'profile', NULL);
GO

INSERT INTO sensor_type_setting (sensor_type_id, enabled, connection_timeout_seconds, display_order)
SELECT id, 0, 30, 8
FROM sensor_type
WHERE code = N'inkbird_ibs_th1';
GO

INSERT INTO sensor_parameter_source (parameter_definition_id, sensor_type_id, priority_order)
SELECT parameter.id, sensor.id, 1
FROM sensor_parameter_definition parameter
CROSS JOIN sensor_type sensor
WHERE sensor.code = N'inkbird_ibs_th1'
  AND parameter.code IN (N'temperature', N'humidity');
GO

DECLARE @spec NVARCHAR(MAX) = N'{"discovery":{"nameExact":"sps","serviceUuid":"0000fff0-0000-1000-8000-00805f9b34fb"},"goldenPackets":[{"characteristicUuid":"0000fff2-0000-1000-8000-00805f9b34fb","expected":{"humidity":60.81,"temperature":19.69},"packetHex":"B107C117000762"}],"operations":[{"acquisition":{"maxPackets":1,"mode":"read","timeoutMs":10000},"assertions":[],"characteristicUuid":"0000fff2-0000-1000-8000-00805f9b34fb","decoders":[{"add":0,"endian":"little","max":125,"min":-40,"offset":0,"parameter":"temperature","scale":0.01,"type":"int16"},{"add":0,"endian":"little","max":100,"min":0,"offset":2,"parameter":"humidity","scale":0.01,"type":"uint16"}],"frame":{"checksum":"none","length":7,"prefixHex":""},"kind":"acquire","serviceUuid":"0000fff0-0000-1000-8000-00805f9b34fb"}],"requiredSecrets":[],"schemaVersion":1,"transport":"gatt_sequence"}';

INSERT INTO sensor_gatt_profile (
    sensor_type_id, revision, status, schema_version, spec_json, spec_hash,
    min_engine_version, read_only, published_at
)
SELECT sensor.id, 1, N'published', 1, @spec,
       LOWER(CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', CONVERT(VARBINARY(MAX), @spec)), 2)),
       N'1.0.0', 1, sysutcdatetime()
FROM sensor_type sensor
WHERE sensor.code = N'inkbird_ibs_th1';
GO
