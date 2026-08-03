INSERT INTO sensor_type (id, code, name, integration_mode, adapter_key)
VALUES
 ('A1000000-0000-4000-8000-000000000001', N'xiaomi', N'Xiaomi', N'profile', NULL),
 ('A1000000-0000-4000-8000-000000000002', N'kestrel', N'Kestrel', N'profile', NULL),
 ('B1000000-0000-4000-8000-000000000001', N'pc_60fw', N'PC-60FW', N'profile', NULL),
 ('B1000000-0000-4000-8000-000000000002', N'bluetooth_sig_plx', N'Bluetooth SIG PLX', N'profile', NULL),
 ('B1000000-0000-4000-8000-000000000003', N'flower_care', N'Flower Care', N'profile', NULL),
 ('B1000000-0000-4000-8000-000000000004', N'xiaomi_door_sensor_2', N'Xiaomi Door Sensor 2', N'profile', NULL),
 ('B1000000-0000-4000-8000-000000000005', N'inkbird_ibs_th1', N'Inkbird IBS-TH1 / TH1 Mini / TH1 Plus', N'profile', NULL);
GO

INSERT INTO sensor_type_setting (sensor_type_id, enabled, connection_timeout_seconds, display_order)
SELECT id, 0, 30,
    CASE code
        WHEN N'xiaomi' THEN 1 WHEN N'kestrel' THEN 2
        WHEN N'pc_60fw' THEN 4 WHEN N'bluetooth_sig_plx' THEN 5
        WHEN N'flower_care' THEN 6 WHEN N'xiaomi_door_sensor_2' THEN 7
        ELSE 8
    END
FROM sensor_type
WHERE code IN (N'xiaomi', N'kestrel', N'pc_60fw', N'bluetooth_sig_plx',
               N'flower_care', N'xiaomi_door_sensor_2', N'inkbird_ibs_th1');
GO

INSERT INTO sensor_parameter_source (parameter_definition_id, sensor_type_id, priority_order)
SELECT parameter.id, sensor.id, 1
FROM sensor_parameter_definition parameter
JOIN sensor_type sensor ON
    (sensor.code = N'xiaomi' AND parameter.code IN (N'temperature', N'humidity'))
 OR (sensor.code = N'kestrel' AND parameter.code IN (N'temperature', N'humidity'))
 OR (sensor.code = N'pc_60fw' AND parameter.code IN (N'spo2', N'pulse_rate', N'perfusion_index'))
 OR (sensor.code = N'bluetooth_sig_plx' AND parameter.code IN (N'spo2', N'pulse_rate', N'flags'))
 OR (sensor.code = N'flower_care' AND parameter.code IN (N'temperature', N'light', N'moisture', N'conductivity'))
 OR (sensor.code = N'xiaomi_door_sensor_2' AND parameter.code IN (N'opening', N'light_detected', N'battery'))
 OR (sensor.code = N'inkbird_ibs_th1' AND parameter.code IN (N'temperature', N'humidity'));
GO

DECLARE @profiles TABLE (code NVARCHAR(32), spec NVARCHAR(MAX));
INSERT INTO @profiles (code, spec) VALUES
(N'xiaomi', N'{"discovery":{"nameExact":"LYWSD03MMC","serviceUuid":"ebe0ccb0-7a0a-4b0c-8a1a-6ff2997da3a6"},"goldenPackets":[{"characteristicUuid":"ebe0ccc1-7a0a-4b0c-8a1a-6ff2997da3a6","expected":{"humidity":45,"temperature":31.24},"packetHex":"340C2D"}],"operations":[{"acquisition":{"maxPackets":1,"mode":"read","timeoutMs":10000},"assertions":[],"characteristicUuid":"ebe0ccc1-7a0a-4b0c-8a1a-6ff2997da3a6","decoders":[{"add":0,"endian":"little","max":125,"min":-40,"offset":0,"parameter":"temperature","scale":0.01,"type":"uint16"},{"add":0,"endian":"little","max":100,"min":0,"offset":2,"parameter":"humidity","scale":1,"type":"uint8"}],"frame":{"checksum":"none","length":3,"prefixHex":""},"kind":"acquire","serviceUuid":"ebe0ccb0-7a0a-4b0c-8a1a-6ff2997da3a6"}],"requiredSecrets":[],"schemaVersion":1,"transport":"gatt_sequence"}'),
(N'kestrel', N'{"discovery":{"nameExact":"D2 - {sensorId}","namePrefix":"D2 - ","serviceUuid":"12630000-cc25-497d-9854-9b6c02c77054"},"goldenPackets":[{"characteristicUuid":"12630001-cc25-497d-9854-9b6c02c77054","expected":{"temperature":21},"packetHex":"073408"},{"characteristicUuid":"12630002-cc25-497d-9854-9b6c02c77054","expected":{"humidity":45},"packetHex":"079411"}],"operations":[{"acquisition":{"maxPackets":1,"mode":"read","timeoutMs":10000},"assertions":[{"equals":7,"offset":0}],"characteristicUuid":"12630001-cc25-497d-9854-9b6c02c77054","decoders":[{"add":0,"endian":"little","max":125,"min":-40,"offset":1,"parameter":"temperature","scale":0.01,"type":"uint16"}],"frame":{"checksum":"none","length":3,"prefixHex":""},"kind":"acquire","serviceUuid":"12630000-cc25-497d-9854-9b6c02c77054"},{"acquisition":{"maxPackets":1,"mode":"read","timeoutMs":10000},"assertions":[{"equals":7,"offset":0}],"characteristicUuid":"12630002-cc25-497d-9854-9b6c02c77054","decoders":[{"add":0,"endian":"little","max":100,"min":0,"offset":1,"parameter":"humidity","scale":0.01,"type":"uint16"}],"frame":{"checksum":"none","length":3,"prefixHex":""},"kind":"acquire","serviceUuid":"12630000-cc25-497d-9854-9b6c02c77054"}],"requiredSecrets":[],"schemaVersion":1,"transport":"gatt_sequence"}'),
(N'pc_60fw', N'{"discovery":{"nameExact":"PC-60FW","serviceUuid":"6e400001-b5a3-f393-e0a9-e50e24dcca9e"},"goldenPackets":[{"characteristicUuid":"6e400003-b5a3-f393-e0a9-e50e24dcca9e","expected":{"perfusion_index":1.5,"pulse_rate":72,"spo2":98},"packetHex":"AA550F08016248000F000079"}],"operations":[{"acquisition":{"maxPackets":10,"mode":"notification","timeoutMs":30000},"assertions":[],"characteristicUuid":"6e400003-b5a3-f393-e0a9-e50e24dcca9e","decoders":[{"add":0,"endian":"little","max":100,"min":0,"offset":5,"parameter":"spo2","scale":1,"type":"uint8"},{"add":0,"endian":"little","max":255,"min":0,"offset":6,"parameter":"pulse_rate","scale":1,"type":"uint8"},{"add":0,"endian":"little","max":25,"min":0,"offset":8,"parameter":"perfusion_index","scale":0.1,"type":"uint8"}],"frame":{"checksum":"crc8_maxim","length":12,"prefixHex":"AA550F0801"},"kind":"acquire","serviceUuid":"6e400001-b5a3-f393-e0a9-e50e24dcca9e"}],"requiredSecrets":[],"schemaVersion":1,"transport":"gatt_sequence"}'),
(N'bluetooth_sig_plx', N'{"discovery":{"serviceUuid":"00001822-0000-1000-8000-00805f9b34fb"},"goldenPackets":[{"characteristicUuid":"00002a5f-0000-1000-8000-00805f9b34fb","expected":{"flags":0,"pulse_rate":72,"spo2":98},"packetHex":"0062004800"}],"operations":[{"acquisition":{"maxPackets":10,"mode":"notification","timeoutMs":30000},"assertions":[],"characteristicUuid":"00002a5f-0000-1000-8000-00805f9b34fb","decoders":[{"add":0,"endian":"little","max":255,"min":0,"offset":0,"parameter":"flags","scale":1,"type":"uint8"},{"add":0,"endian":"little","max":100,"min":0,"offset":1,"parameter":"spo2","scale":1,"type":"sfloat16"},{"add":0,"endian":"little","max":300,"min":0,"offset":3,"parameter":"pulse_rate","scale":1,"type":"sfloat16"}],"frame":{"checksum":"none","length":5,"prefixHex":""},"kind":"acquire","serviceUuid":"00001822-0000-1000-8000-00805f9b34fb"}],"requiredSecrets":[],"schemaVersion":1,"transport":"gatt_sequence"}'),
(N'flower_care', N'{"discovery":{"nameExact":"Flower care","serviceUuid":"00001204-0000-1000-8000-00805f9b34fb"},"goldenPackets":[{"characteristicUuid":"00001a01-0000-1000-8000-00805f9b34fb","expected":{"conductivity":350,"light":12345,"moisture":45,"temperature":21.5},"packetHex":"D70000393000002D5E01"}],"operations":[{"characteristicUuid":"00001a00-0000-1000-8000-00805f9b34fb","kind":"write","payloadHex":"A01F","serviceUuid":"00001204-0000-1000-8000-00805f9b34fb","timeoutMs":5000},{"durationMs":750,"kind":"delay"},{"acquisition":{"maxPackets":1,"mode":"read","timeoutMs":10000},"assertions":[],"characteristicUuid":"00001a01-0000-1000-8000-00805f9b34fb","decoders":[{"add":0,"endian":"little","max":125,"min":-40,"offset":0,"parameter":"temperature","scale":0.1,"type":"int16"},{"add":0,"endian":"little","max":4294967295,"min":0,"offset":3,"parameter":"light","scale":1,"type":"uint32"},{"add":0,"endian":"little","max":100,"min":0,"offset":7,"parameter":"moisture","scale":1,"type":"uint8"},{"add":0,"endian":"little","max":65535,"min":0,"offset":8,"parameter":"conductivity","scale":1,"type":"uint16"}],"frame":{"checksum":"none","length":10,"prefixHex":""},"kind":"acquire","serviceUuid":"00001204-0000-1000-8000-00805f9b34fb"}],"requiredSecrets":[],"schemaVersion":1,"transport":"gatt_sequence"}'),
(N'xiaomi_door_sensor_2', N'{"advertisement":{"decoderId":"xiaomi_mibeacon_v4_v5","matcher":{"productId":2443},"objects":[{"objectId":"0x1019","parameter":"opening","type":"uint8","values":{"0":"open","1":"closed","2":"left_open"}},{"objectId":"0x1018","parameter":"light_detected","type":"bool","values":{"0":"false","1":"true"}},{"objectId":"0x100A","parameter":"battery","type":"uint8"}]},"goldenPackets":[{"advertisementHex":"50308B09000000000000000019100100","expected":{"opening":"open"}}],"requiredSecrets":["bind_key"],"schemaVersion":1,"transport":"ble_advertisement"}'),
(N'inkbird_ibs_th1', N'{"discovery":{"nameExact":"sps","serviceUuid":"0000fff0-0000-1000-8000-00805f9b34fb"},"goldenPackets":[{"characteristicUuid":"0000fff2-0000-1000-8000-00805f9b34fb","expected":{"humidity":60.81,"temperature":19.69},"packetHex":"B107C117000762"}],"operations":[{"acquisition":{"maxPackets":1,"mode":"read","timeoutMs":10000},"assertions":[],"characteristicUuid":"0000fff2-0000-1000-8000-00805f9b34fb","decoders":[{"add":0,"endian":"little","max":125,"min":-40,"offset":0,"parameter":"temperature","scale":0.01,"type":"int16"},{"add":0,"endian":"little","max":100,"min":0,"offset":2,"parameter":"humidity","scale":0.01,"type":"uint16"}],"frame":{"checksum":"none","length":7,"prefixHex":""},"kind":"acquire","serviceUuid":"0000fff0-0000-1000-8000-00805f9b34fb"}],"requiredSecrets":[],"schemaVersion":1,"transport":"gatt_sequence"}');

INSERT INTO sensor_gatt_profile (
    sensor_type_id, revision, status, schema_version, spec_json, spec_hash,
    min_engine_version, read_only, published_at
)
SELECT sensor.id, 1, N'published', 1, profile.spec,
       LOWER(CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', CONVERT(VARBINARY(MAX), profile.spec)), 2)),
       N'1.0.0', 1, sysutcdatetime()
FROM @profiles profile
JOIN sensor_type sensor ON sensor.code = profile.code;
GO
