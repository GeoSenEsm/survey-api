package com.survey.application.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.survey.application.services.SensorProfileTemplate.ParameterMapping;

/**
 * Static catalog of BLE sensor templates available on the Integrations page. Each entry mirrors
 * a profile that used to be seeded directly into the database (see the now-purged rows in
 * {@code V31__create_gatt_profile_engine.sql} and {@code V33__add_inkbird_ibs_th1_gatt_profile.sql});
 * they now live here as installable code so a fresh database starts with an empty sensor catalog.
 */
public final class SensorProfileTemplateCatalog {

    private static final List<SensorProfileTemplate> ALL = List.of(
            new SensorProfileTemplate(
                    "xiaomi",
                    "Xiaomi",
                    // Real LYWSD03MMC units in the field broadcast encrypted MiBeacon advertisements
                    // (bind key required, cryptographically unreadable without it) — the
                    // advertisement/MiBeacon decode path tried above can never work for them.
                    // Reverted to the proven v.2.0.1 approach: connect directly to the device's own
                    // GATT service and read plaintext bytes, no encryption or bind key involved.
                    "{\"discovery\":{\"nameExact\":\"LYWSD03MMC\"},\"goldenPackets\":[{\"characteristicUuid\":\"ebe0ccc1-7a0a-4b0c-8a1a-6ff2997da3a6\",\"expected\":{\"humidity\":45,\"temperature\":21.5},\"packetHex\":\"66082D\"}],\"operations\":[{\"acquisition\":{\"maxPackets\":1,\"mode\":\"read\",\"timeoutMs\":10000},\"assertions\":[],\"characteristicUuid\":\"ebe0ccc1-7a0a-4b0c-8a1a-6ff2997da3a6\",\"decoders\":[{\"add\":0,\"endian\":\"little\",\"max\":125,\"min\":-40,\"offset\":0,\"parameter\":\"temperature\",\"scale\":0.01,\"type\":\"uint16\"},{\"add\":0,\"endian\":\"little\",\"max\":100,\"min\":0,\"offset\":2,\"parameter\":\"humidity\",\"scale\":1,\"type\":\"uint8\"}],\"frame\":{\"checksum\":\"none\",\"length\":3,\"prefixHex\":\"\"},\"kind\":\"acquire\",\"serviceUuid\":\"ebe0ccb0-7a0a-4b0c-8a1a-6ff2997da3a6\"}],\"requiredSecrets\":[],\"schemaVersion\":1,\"transport\":\"gatt_sequence\"}",
                    "1.0.0",
                    List.of(new ParameterMapping("temperature", 1), new ParameterMapping("humidity", 1))),
            new SensorProfileTemplate(
                    "kestrel",
                    "Kestrel",
                    "{\"discovery\":{\"nameExact\":\"D2 - {sensorId}\",\"namePrefix\":\"D2 - \",\"serviceUuid\":\"12630000-cc25-497d-9854-9b6c02c77054\"},\"goldenPackets\":[{\"characteristicUuid\":\"12630001-cc25-497d-9854-9b6c02c77054\",\"expected\":{\"temperature\":21},\"packetHex\":\"073408\"},{\"characteristicUuid\":\"12630002-cc25-497d-9854-9b6c02c77054\",\"expected\":{\"humidity\":45},\"packetHex\":\"079411\"}],\"operations\":[{\"acquisition\":{\"maxPackets\":1,\"mode\":\"read\",\"timeoutMs\":10000},\"assertions\":[{\"equals\":7,\"offset\":0}],\"characteristicUuid\":\"12630001-cc25-497d-9854-9b6c02c77054\",\"decoders\":[{\"add\":0,\"endian\":\"little\",\"max\":125,\"min\":-40,\"offset\":1,\"parameter\":\"temperature\",\"scale\":0.01,\"type\":\"uint16\"}],\"frame\":{\"checksum\":\"none\",\"length\":3,\"prefixHex\":\"\"},\"kind\":\"acquire\",\"serviceUuid\":\"12630000-cc25-497d-9854-9b6c02c77054\"},{\"acquisition\":{\"maxPackets\":1,\"mode\":\"read\",\"timeoutMs\":10000},\"assertions\":[{\"equals\":7,\"offset\":0}],\"characteristicUuid\":\"12630002-cc25-497d-9854-9b6c02c77054\",\"decoders\":[{\"add\":0,\"endian\":\"little\",\"max\":100,\"min\":0,\"offset\":1,\"parameter\":\"humidity\",\"scale\":0.01,\"type\":\"uint16\"}],\"frame\":{\"checksum\":\"none\",\"length\":3,\"prefixHex\":\"\"},\"kind\":\"acquire\",\"serviceUuid\":\"12630000-cc25-497d-9854-9b6c02c77054\"}],\"requiredSecrets\":[],\"schemaVersion\":1,\"transport\":\"gatt_sequence\"}",
                    "1.0.0",
                    List.of(new ParameterMapping("temperature", 1), new ParameterMapping("humidity", 1))),
            new SensorProfileTemplate(
                    "pc_60fw",
                    "PC-60FW",
                    "{\"discovery\":{\"namePrefix\":\"PC-60\",\"serviceUuid\":\"6e400001-b5a3-f393-e0a9-e50e24dcca9e\"},\"goldenPackets\":[{\"characteristicUuid\":\"6e400003-b5a3-f393-e0a9-e50e24dcca9e\",\"expected\":{\"perfusion_index\":1.5,\"pulse_rate\":72,\"spo2\":98},\"packetHex\":\"AA550F08016248000F000079\"}],\"operations\":[{\"acquisition\":{\"maxPackets\":100,\"mode\":\"notification\",\"timeoutMs\":30000},\"assertions\":[],\"characteristicUuid\":\"6e400003-b5a3-f393-e0a9-e50e24dcca9e\",\"decoders\":[{\"add\":0,\"endian\":\"little\",\"max\":100,\"min\":0,\"offset\":5,\"parameter\":\"spo2\",\"scale\":1,\"type\":\"uint8\"},{\"add\":0,\"endian\":\"little\",\"max\":255,\"min\":0,\"offset\":6,\"parameter\":\"pulse_rate\",\"scale\":1,\"type\":\"uint8\"},{\"add\":0,\"endian\":\"little\",\"max\":25,\"min\":0,\"offset\":8,\"parameter\":\"perfusion_index\",\"scale\":0.1,\"type\":\"uint8\"}],\"frame\":{\"checksum\":\"crc8_maxim\",\"length\":12,\"prefixHex\":\"AA550F0801\"},\"kind\":\"acquire\",\"serviceUuid\":\"6e400001-b5a3-f393-e0a9-e50e24dcca9e\"}],\"requiredSecrets\":[],\"schemaVersion\":1,\"transport\":\"gatt_sequence\"}",
                    "1.0.0",
                    List.of(new ParameterMapping("spo2", 1), new ParameterMapping("pulse_rate", 1), new ParameterMapping("perfusion_index", 1))),
            new SensorProfileTemplate(
                    "flower_care",
                    "Flower Care",
                    // discovery.serviceUuid must be a service the device actually broadcasts in its
                    // scan advertisement — 00001204 (the sensor-data GATT service used below in
                    // `operations`) is only visible after connecting via discoverServices(), never in
                    // the advertisement itself, so discovery could never match. Real hardware
                    // advertises under fe95 (Xiaomi's shared MiBeacon service); the exact-name check
                    // already disambiguates it from other fe95 devices.
                    "{\"discovery\":{\"nameExact\":\"Flower care\",\"serviceUuid\":\"0000fe95-0000-1000-8000-00805f9b34fb\"},\"goldenPackets\":[{\"characteristicUuid\":\"00001a01-0000-1000-8000-00805f9b34fb\",\"expected\":{\"conductivity\":350,\"light\":12345,\"moisture\":45,\"temperature\":21.5},\"packetHex\":\"D70000393000002D5E01\"}],\"operations\":[{\"characteristicUuid\":\"00001a00-0000-1000-8000-00805f9b34fb\",\"kind\":\"write\",\"payloadHex\":\"A01F\",\"serviceUuid\":\"00001204-0000-1000-8000-00805f9b34fb\",\"timeoutMs\":5000},{\"durationMs\":750,\"kind\":\"delay\"},{\"acquisition\":{\"maxPackets\":1,\"mode\":\"read\",\"timeoutMs\":10000},\"assertions\":[],\"characteristicUuid\":\"00001a01-0000-1000-8000-00805f9b34fb\",\"decoders\":[{\"add\":0,\"endian\":\"little\",\"max\":125,\"min\":-40,\"offset\":0,\"parameter\":\"temperature\",\"scale\":0.1,\"type\":\"int16\"},{\"add\":0,\"endian\":\"little\",\"max\":4294967295,\"min\":0,\"offset\":3,\"parameter\":\"light\",\"scale\":1,\"type\":\"uint32\"},{\"add\":0,\"endian\":\"little\",\"max\":100,\"min\":0,\"offset\":7,\"parameter\":\"moisture\",\"scale\":1,\"type\":\"uint8\"},{\"add\":0,\"endian\":\"little\",\"max\":65535,\"min\":0,\"offset\":8,\"parameter\":\"conductivity\",\"scale\":1,\"type\":\"uint16\"}],\"frame\":{\"checksum\":\"none\",\"length\":10,\"prefixHex\":\"\"},\"kind\":\"acquire\",\"serviceUuid\":\"00001204-0000-1000-8000-00805f9b34fb\"}],\"requiredSecrets\":[],\"schemaVersion\":1,\"transport\":\"gatt_sequence\"}",
                    "1.0.0",
                    List.of(new ParameterMapping("temperature", 1), new ParameterMapping("light", 1),
                            new ParameterMapping("moisture", 1), new ParameterMapping("conductivity", 1))),
            new SensorProfileTemplate(
                    "xiaomi_door_sensor_2",
                    "Xiaomi Door Sensor 2",
                    "{\"advertisement\":{\"decoderId\":\"xiaomi_mibeacon_v4_v5\",\"matcher\":{\"productId\":2443},\"objects\":[{\"objectId\":\"0x1019\",\"parameter\":\"opening\",\"type\":\"uint8\",\"values\":{\"0\":\"open\",\"1\":\"closed\",\"2\":\"left_open\"}},{\"objectId\":\"0x1018\",\"parameter\":\"light_detected\",\"type\":\"bool\",\"values\":{\"0\":\"false\",\"1\":\"true\"}},{\"objectId\":\"0x100A\",\"parameter\":\"battery\",\"type\":\"uint8\"}]},\"goldenPackets\":[{\"advertisementHex\":\"50308B09000000000000000019100100\",\"expected\":{\"opening\":\"open\"}}],\"requiredSecrets\":[\"bind_key\"],\"schemaVersion\":1,\"transport\":\"ble_advertisement\"}",
                    "1.0.0",
                    List.of(new ParameterMapping("opening", 1), new ParameterMapping("light_detected", 1), new ParameterMapping("battery", 1))),
            new SensorProfileTemplate(
                    "inkbird_ibs_th1",
                    "Inkbird IBS-TH1 / TH1 Mini / TH1 Plus",
                    "{\"discovery\":{\"nameExact\":\"sps\",\"serviceUuid\":\"0000fff0-0000-1000-8000-00805f9b34fb\"},\"goldenPackets\":[{\"characteristicUuid\":\"0000fff2-0000-1000-8000-00805f9b34fb\",\"expected\":{\"humidity\":60.81,\"temperature\":19.69},\"packetHex\":\"B107C117000762\"}],\"operations\":[{\"acquisition\":{\"maxPackets\":1,\"mode\":\"read\",\"timeoutMs\":10000},\"assertions\":[],\"characteristicUuid\":\"0000fff2-0000-1000-8000-00805f9b34fb\",\"decoders\":[{\"add\":0,\"endian\":\"little\",\"max\":125,\"min\":-40,\"offset\":0,\"parameter\":\"temperature\",\"scale\":0.01,\"type\":\"int16\"},{\"add\":0,\"endian\":\"little\",\"max\":100,\"min\":0,\"offset\":2,\"parameter\":\"humidity\",\"scale\":0.01,\"type\":\"uint16\"}],\"frame\":{\"checksum\":\"none\",\"length\":7,\"prefixHex\":\"\"},\"kind\":\"acquire\",\"serviceUuid\":\"0000fff0-0000-1000-8000-00805f9b34fb\"}],\"requiredSecrets\":[],\"schemaVersion\":1,\"transport\":\"gatt_sequence\"}",
                    "1.0.0",
                    List.of(new ParameterMapping("temperature", 1), new ParameterMapping("humidity", 1))));

    private static final Map<String, SensorProfileTemplate> BY_CODE = ALL.stream()
            .collect(java.util.stream.Collectors.toMap(SensorProfileTemplate::code, t -> t));

    private SensorProfileTemplateCatalog() {}

    public static List<SensorProfileTemplate> all() {
        return ALL;
    }

    public static Optional<SensorProfileTemplate> findByCode(String code) {
        return Optional.ofNullable(BY_CODE.get(code));
    }
}
