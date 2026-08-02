ALTER TABLE sensor_type
    ADD integration_mode NVARCHAR(32) NOT NULL
            CONSTRAINT DF_sensor_type_integration_mode DEFAULT N'native',
        adapter_key NVARCHAR(64) NULL;
GO

UPDATE sensor_type
SET integration_mode = CASE code
    WHEN N'xiaomi' THEN N'profile'
    WHEN N'kestrel' THEN N'profile'
    WHEN N'manual' THEN N'manual'
    WHEN N'none' THEN N'none'
    ELSE N'native'
END;
GO

ALTER TABLE sensor_type ADD CONSTRAINT CK_sensor_type_integration_mode
    CHECK (integration_mode IN (N'profile', N'native', N'manual', N'none'));
GO

INSERT INTO sensor_type (id, code, name, integration_mode, adapter_key)
VALUES
 ('B1000000-0000-4000-8000-000000000001', N'pc_60fw', N'PC-60FW', N'profile', NULL),
 ('B1000000-0000-4000-8000-000000000002', N'bluetooth_sig_plx', N'Bluetooth SIG PLX', N'profile', NULL),
 ('B1000000-0000-4000-8000-000000000003', N'flower_care', N'Flower Care', N'profile', NULL),
 ('B1000000-0000-4000-8000-000000000004', N'xiaomi_door_sensor_2', N'Xiaomi Door Sensor 2', N'profile', NULL);
GO

INSERT INTO sensor_type_setting (sensor_type_id, enabled, connection_timeout_seconds, display_order)
SELECT id, 0, 30,
    CASE code WHEN N'pc_60fw' THEN 4 WHEN N'bluetooth_sig_plx' THEN 5
              WHEN N'flower_care' THEN 6 ELSE 7 END
FROM sensor_type
WHERE code IN (N'pc_60fw', N'bluetooth_sig_plx', N'flower_care', N'xiaomi_door_sensor_2');
GO

INSERT INTO sensor_parameter_definition
    (id, code, name, data_type, unit, required, active, display_order)
VALUES
 ('A2000000-0000-4000-8000-000000000003', N'spo2', N'SpO2', N'decimal', N'%', 0, 1, 3),
 ('A2000000-0000-4000-8000-000000000004', N'pulse_rate', N'Pulse rate', N'decimal', N'bpm', 0, 1, 4),
 ('A2000000-0000-4000-8000-000000000005', N'perfusion_index', N'Perfusion index', N'decimal', N'%', 0, 1, 5),
 ('A2000000-0000-4000-8000-000000000006', N'flags', N'Flags', N'integer', NULL, 0, 1, 6),
 ('A2000000-0000-4000-8000-000000000007', N'light', N'Light', N'integer', N'lux', 0, 1, 7),
 ('A2000000-0000-4000-8000-000000000008', N'moisture', N'Moisture', N'decimal', N'%', 0, 1, 8),
 ('A2000000-0000-4000-8000-000000000009', N'conductivity', N'Conductivity', N'integer', N'uS/cm', 0, 1, 9),
 ('A2000000-0000-4000-8000-00000000000A', N'opening', N'Opening', N'text', NULL, 0, 1, 10),
 ('A2000000-0000-4000-8000-00000000000B', N'battery', N'Battery', N'integer', N'%', 0, 1, 11);
GO

INSERT INTO sensor_parameter_source (parameter_definition_id, sensor_type_id, priority_order)
SELECT parameter.id, sensor.id, 1
FROM sensor_parameter_definition parameter
JOIN sensor_type sensor ON
    (sensor.code = N'pc_60fw'
        AND parameter.code IN (N'spo2', N'pulse_rate', N'perfusion_index'))
 OR (sensor.code = N'bluetooth_sig_plx'
        AND parameter.code IN (N'spo2', N'pulse_rate', N'flags'))
 OR (sensor.code = N'flower_care'
        AND parameter.code IN (N'temperature', N'light', N'moisture', N'conductivity'))
 OR (sensor.code = N'xiaomi_door_sensor_2'
        AND parameter.code IN (N'opening', N'light', N'battery'));
GO

CREATE TABLE sensor_gatt_profile (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    sensor_type_id UNIQUEIDENTIFIER NOT NULL,
    revision INT NOT NULL,
    status NVARCHAR(16) NOT NULL,
    schema_version INT NOT NULL,
    spec_json NVARCHAR(MAX) NOT NULL,
    spec_hash CHAR(64) NOT NULL,
    min_engine_version NVARCHAR(32) NOT NULL,
    read_only BIT NOT NULL DEFAULT 0,
    created_at DATETIMEOFFSET(0) NOT NULL DEFAULT sysutcdatetime(),
    updated_at DATETIMEOFFSET(0) NOT NULL DEFAULT sysutcdatetime(),
    published_at DATETIMEOFFSET(0) NULL,
    row_version TIMESTAMP NOT NULL,
    CONSTRAINT FK_sensor_gatt_profile_sensor_type
        FOREIGN KEY (sensor_type_id) REFERENCES sensor_type(id) ON DELETE CASCADE,
    CONSTRAINT UQ_sensor_gatt_profile_revision UNIQUE (sensor_type_id, revision),
    CONSTRAINT CK_sensor_gatt_profile_revision CHECK (revision > 0),
    CONSTRAINT CK_sensor_gatt_profile_schema_version CHECK (schema_version = 1),
    CONSTRAINT CK_sensor_gatt_profile_status CHECK (status IN (N'draft', N'published', N'archived')),
    CONSTRAINT CK_sensor_gatt_profile_json CHECK (ISJSON(spec_json) = 1),
    CONSTRAINT CK_sensor_gatt_profile_published_at CHECK (
        (status = N'published' AND published_at IS NOT NULL)
        OR (status <> N'published' AND published_at IS NULL))
);
GO

CREATE UNIQUE INDEX UQ_sensor_gatt_profile_published
    ON sensor_gatt_profile(sensor_type_id) WHERE status = N'published';
GO
CREATE INDEX IX_sensor_gatt_profile_sensor_type_revision
    ON sensor_gatt_profile(sensor_type_id, revision DESC);
GO

DECLARE @profiles TABLE (code NVARCHAR(32), spec NVARCHAR(MAX));
INSERT INTO @profiles (code, spec) VALUES
(N'xiaomi', N'{"discovery":{"nameExact":"LYWSD03MMC","serviceUuid":"ebe0ccb0-7a0a-4b0c-8a1a-6ff2997da3a6"},"goldenPackets":[{"characteristicUuid":"ebe0ccc1-7a0a-4b0c-8a1a-6ff2997da3a6","expected":{"humidity":45,"temperature":31.24},"packetHex":"340C2D"}],"operations":[{"acquisition":{"maxPackets":1,"mode":"read","timeoutMs":10000},"assertions":[],"characteristicUuid":"ebe0ccc1-7a0a-4b0c-8a1a-6ff2997da3a6","decoders":[{"add":0,"endian":"little","max":125,"min":-40,"offset":0,"parameter":"temperature","scale":0.01,"type":"uint16"},{"add":0,"endian":"little","max":100,"min":0,"offset":2,"parameter":"humidity","scale":1,"type":"uint8"}],"frame":{"checksum":"none","length":3,"prefixHex":""},"kind":"acquire","serviceUuid":"ebe0ccb0-7a0a-4b0c-8a1a-6ff2997da3a6"}],"requiredSecrets":[],"schemaVersion":1,"transport":"gatt_sequence"}'),
(N'kestrel', N'{"discovery":{"nameExact":"D2 - {sensorId}","namePrefix":"D2 - ","serviceUuid":"12630000-cc25-497d-9854-9b6c02c77054"},"goldenPackets":[{"characteristicUuid":"12630001-cc25-497d-9854-9b6c02c77054","expected":{"temperature":21},"packetHex":"073408"},{"characteristicUuid":"12630002-cc25-497d-9854-9b6c02c77054","expected":{"humidity":45},"packetHex":"079411"}],"operations":[{"acquisition":{"maxPackets":1,"mode":"read","timeoutMs":10000},"assertions":[{"equals":7,"offset":0}],"characteristicUuid":"12630001-cc25-497d-9854-9b6c02c77054","decoders":[{"add":0,"endian":"little","max":125,"min":-40,"offset":1,"parameter":"temperature","scale":0.01,"type":"uint16"}],"frame":{"checksum":"none","length":3,"prefixHex":""},"kind":"acquire","serviceUuid":"12630000-cc25-497d-9854-9b6c02c77054"},{"acquisition":{"maxPackets":1,"mode":"read","timeoutMs":10000},"assertions":[{"equals":7,"offset":0}],"characteristicUuid":"12630002-cc25-497d-9854-9b6c02c77054","decoders":[{"add":0,"endian":"little","max":100,"min":0,"offset":1,"parameter":"humidity","scale":0.01,"type":"uint16"}],"frame":{"checksum":"none","length":3,"prefixHex":""},"kind":"acquire","serviceUuid":"12630000-cc25-497d-9854-9b6c02c77054"}],"requiredSecrets":[],"schemaVersion":1,"transport":"gatt_sequence"}'),
(N'pc_60fw', N'{"discovery":{"nameExact":"PC-60FW","serviceUuid":"6e400001-b5a3-f393-e0a9-e50e24dcca9e"},"goldenPackets":[{"characteristicUuid":"6e400003-b5a3-f393-e0a9-e50e24dcca9e","expected":{"perfusion_index":1.5,"pulse_rate":72,"spo2":98},"packetHex":"AA550F08016248000F000079"}],"operations":[{"acquisition":{"maxPackets":10,"mode":"notification","timeoutMs":30000},"assertions":[],"characteristicUuid":"6e400003-b5a3-f393-e0a9-e50e24dcca9e","decoders":[{"add":0,"endian":"little","max":100,"min":0,"offset":5,"parameter":"spo2","scale":1,"type":"uint8"},{"add":0,"endian":"little","max":255,"min":0,"offset":6,"parameter":"pulse_rate","scale":1,"type":"uint8"},{"add":0,"endian":"little","max":25,"min":0,"offset":8,"parameter":"perfusion_index","scale":0.1,"type":"uint8"}],"frame":{"checksum":"crc8_maxim","length":12,"prefixHex":"AA550F0801"},"kind":"acquire","serviceUuid":"6e400001-b5a3-f393-e0a9-e50e24dcca9e"}],"requiredSecrets":[],"schemaVersion":1,"transport":"gatt_sequence"}'),
(N'bluetooth_sig_plx', N'{"discovery":{"serviceUuid":"00001822-0000-1000-8000-00805f9b34fb"},"goldenPackets":[{"characteristicUuid":"00002a5f-0000-1000-8000-00805f9b34fb","expected":{"flags":0,"pulse_rate":72,"spo2":98},"packetHex":"0062004800"}],"operations":[{"acquisition":{"maxPackets":10,"mode":"notification","timeoutMs":30000},"assertions":[],"characteristicUuid":"00002a5f-0000-1000-8000-00805f9b34fb","decoders":[{"add":0,"endian":"little","max":255,"min":0,"offset":0,"parameter":"flags","scale":1,"type":"uint8"},{"add":0,"endian":"little","max":100,"min":0,"offset":1,"parameter":"spo2","scale":1,"type":"sfloat16"},{"add":0,"endian":"little","max":300,"min":0,"offset":3,"parameter":"pulse_rate","scale":1,"type":"sfloat16"}],"frame":{"checksum":"none","length":5,"prefixHex":""},"kind":"acquire","serviceUuid":"00001822-0000-1000-8000-00805f9b34fb"}],"requiredSecrets":[],"schemaVersion":1,"transport":"gatt_sequence"}'),
(N'flower_care', N'{"discovery":{"nameExact":"Flower care","serviceUuid":"00001204-0000-1000-8000-00805f9b34fb"},"goldenPackets":[{"characteristicUuid":"00001a01-0000-1000-8000-00805f9b34fb","expected":{"conductivity":350,"light":12345,"moisture":45,"temperature":21.5},"packetHex":"D70000393000002D5E01"}],"operations":[{"characteristicUuid":"00001a00-0000-1000-8000-00805f9b34fb","kind":"write","payloadHex":"A01F","serviceUuid":"00001204-0000-1000-8000-00805f9b34fb","timeoutMs":5000},{"durationMs":750,"kind":"delay"},{"acquisition":{"maxPackets":1,"mode":"read","timeoutMs":10000},"assertions":[],"characteristicUuid":"00001a01-0000-1000-8000-00805f9b34fb","decoders":[{"add":0,"endian":"little","max":125,"min":-40,"offset":0,"parameter":"temperature","scale":0.1,"type":"int16"},{"add":0,"endian":"little","max":4294967295,"min":0,"offset":3,"parameter":"light","scale":1,"type":"uint32"},{"add":0,"endian":"little","max":100,"min":0,"offset":7,"parameter":"moisture","scale":1,"type":"uint8"},{"add":0,"endian":"little","max":65535,"min":0,"offset":8,"parameter":"conductivity","scale":1,"type":"uint16"}],"frame":{"checksum":"none","length":10,"prefixHex":""},"kind":"acquire","serviceUuid":"00001204-0000-1000-8000-00805f9b34fb"}],"requiredSecrets":[],"schemaVersion":1,"transport":"gatt_sequence"}'),
(N'xiaomi_door_sensor_2', N'{"advertisement":{"decoderId":"xiaomi_mibeacon_v4_v5","matcher":{"productId":2443},"objects":[{"objectId":"0x1019","parameter":"opening","type":"uint8","values":{"0":"open","1":"closed","2":"left_open"}},{"objectId":"0x1018","parameter":"light","type":"bool","values":{"0":"false","1":"true"}},{"objectId":"0x100A","parameter":"battery","type":"uint8"}]},"goldenPackets":[{"advertisementHex":"50308B09000000000000000019100100","expected":{"opening":"open"}}],"requiredSecrets":["bind_key"],"schemaVersion":1,"transport":"ble_advertisement"}');

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

CREATE TABLE sensor_device_secret (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    sensor_mac_id UNIQUEIDENTIFIER NOT NULL,
    secret_name NVARCHAR(32) NOT NULL,
    ciphertext VARBINARY(256) NOT NULL,
    nonce BINARY(12) NOT NULL,
    created_at DATETIMEOFFSET(0) NOT NULL DEFAULT sysutcdatetime(),
    updated_at DATETIMEOFFSET(0) NOT NULL DEFAULT sysutcdatetime(),
    row_version TIMESTAMP NOT NULL,
    CONSTRAINT FK_sensor_device_secret_sensor_mac
        FOREIGN KEY (sensor_mac_id) REFERENCES sensor_mac(id) ON DELETE CASCADE,
    CONSTRAINT UQ_sensor_device_secret_name UNIQUE (sensor_mac_id, secret_name),
    CONSTRAINT CK_sensor_device_secret_name CHECK (secret_name NOT LIKE N'%[^a-z0-9_]%' AND LEN(secret_name) > 0)
);
GO

DROP INDEX UQ_sensor_mac_respondent_id ON sensor_mac;
GO
CREATE UNIQUE INDEX UQ_respondent_sensor_assignment_type
    ON respondent_sensor_assignment(respondent_id, sensor_type_id);
GO
CREATE UNIQUE INDEX UQ_respondent_sensor_assignment_sensor
    ON respondent_sensor_assignment(sensor_mac_id) WHERE sensor_mac_id IS NOT NULL;
GO
