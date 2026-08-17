package com.survey.application.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.survey.application.services.SensorProfileTemplate.ParameterMapping;

/**
 * Static catalog of BLE sensor templates available on the Integrations page, installable on
 * demand so a fresh database starts with an empty sensor catalog. Nothing here is pre-seeded by
 * migration, including the "used sensor data" parameters each template needs
 * ({@code temperature}, {@code pressure}, etc.): {@link SensorProfileTemplateServiceImpl} creates
 * a used parameter the first time some installed template actually needs it, reusing it by code
 * if an earlier install already created it. That keeps the "used sensor data" list limited to
 * parameters an active integration actually produces, by construction — there is no separate
 * filter to keep in sync.
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
                    "{\"discovery\":{\"nameExact\":\"LYWSD03MMC\"},\"goldenPackets\":[{\"characteristicUuid\":\"ebe0ccc1-7a0a-4b0c-8a1a-6ff2997da3a6\",\"expected\":{\"humidity\":45,\"temperature\":21.5},\"packetHex\":\"66082D\"}],\"operations\":[{\"acquisition\":{\"maxPackets\":1,\"mode\":\"read\",\"timeoutMs\":10000},\"assertions\":[],\"characteristicUuid\":\"ebe0ccc1-7a0a-4b0c-8a1a-6ff2997da3a6\",\"decoders\":[{\"add\":0,\"endian\":\"little\",\"max\":125,\"min\":-40,\"offset\":0,\"parameter\":\"temperature\",\"scale\":0.01,\"type\":\"uint16\"},{\"add\":0,\"endian\":\"little\",\"max\":100,\"min\":0,\"offset\":2,\"parameter\":\"humidity\",\"scale\":1,\"type\":\"uint8\"}],\"frame\":{\"checksum\":\"none\",\"length\":3,\"prefixHex\":\"\"},\"kind\":\"acquire\",\"serviceUuid\":\"ebe0ccb0-7a0a-4b0c-8a1a-6ff2997da3a6\"}],\"schemaVersion\":1,\"transport\":\"gatt_sequence\"}",
                    "1.0.0",
                    List.of(new ParameterMapping("temperature", "Temperature", "decimal", "C"),
                            new ParameterMapping("humidity", "Humidity", "decimal", "%"))),
            new SensorProfileTemplate(
                    "kestrel",
                    "Kestrel",
                    "{\"discovery\":{\"nameExact\":\"D2 - {sensorId}\",\"namePrefix\":\"D2 - \",\"serviceUuid\":\"12630000-cc25-497d-9854-9b6c02c77054\"},\"goldenPackets\":[{\"characteristicUuid\":\"12630001-cc25-497d-9854-9b6c02c77054\",\"expected\":{\"temperature\":21},\"packetHex\":\"073408\"},{\"characteristicUuid\":\"12630002-cc25-497d-9854-9b6c02c77054\",\"expected\":{\"humidity\":45},\"packetHex\":\"079411\"}],\"operations\":[{\"acquisition\":{\"maxPackets\":1,\"mode\":\"read\",\"timeoutMs\":10000},\"assertions\":[{\"equals\":7,\"offset\":0}],\"characteristicUuid\":\"12630001-cc25-497d-9854-9b6c02c77054\",\"decoders\":[{\"add\":0,\"endian\":\"little\",\"max\":125,\"min\":-40,\"offset\":1,\"parameter\":\"temperature\",\"scale\":0.01,\"type\":\"uint16\"}],\"frame\":{\"checksum\":\"none\",\"length\":3,\"prefixHex\":\"\"},\"kind\":\"acquire\",\"serviceUuid\":\"12630000-cc25-497d-9854-9b6c02c77054\"},{\"acquisition\":{\"maxPackets\":1,\"mode\":\"read\",\"timeoutMs\":10000},\"assertions\":[{\"equals\":7,\"offset\":0}],\"characteristicUuid\":\"12630002-cc25-497d-9854-9b6c02c77054\",\"decoders\":[{\"add\":0,\"endian\":\"little\",\"max\":100,\"min\":0,\"offset\":1,\"parameter\":\"humidity\",\"scale\":0.01,\"type\":\"uint16\"}],\"frame\":{\"checksum\":\"none\",\"length\":3,\"prefixHex\":\"\"},\"kind\":\"acquire\",\"serviceUuid\":\"12630000-cc25-497d-9854-9b6c02c77054\"}],\"schemaVersion\":1,\"transport\":\"gatt_sequence\"}",
                    "1.0.0",
                    List.of(new ParameterMapping("temperature", "Temperature", "decimal", "C"),
                            new ParameterMapping("humidity", "Humidity", "decimal", "%"))),
            new SensorProfileTemplate(
                    "pc_60fw",
                    "PC-60FW",
                    "{\"discovery\":{\"namePrefix\":\"PC-60\",\"serviceUuid\":\"6e400001-b5a3-f393-e0a9-e50e24dcca9e\"},\"goldenPackets\":[{\"characteristicUuid\":\"6e400003-b5a3-f393-e0a9-e50e24dcca9e\",\"expected\":{\"perfusion_index\":1.5,\"pulse_rate\":72,\"spo2\":98},\"packetHex\":\"AA550F08016248000F000079\"}],\"operations\":[{\"acquisition\":{\"maxPackets\":100,\"mode\":\"notification\",\"timeoutMs\":30000},\"assertions\":[],\"characteristicUuid\":\"6e400003-b5a3-f393-e0a9-e50e24dcca9e\",\"decoders\":[{\"add\":0,\"endian\":\"little\",\"max\":100,\"min\":0,\"offset\":5,\"parameter\":\"spo2\",\"scale\":1,\"type\":\"uint8\"},{\"add\":0,\"endian\":\"little\",\"max\":255,\"min\":0,\"offset\":6,\"parameter\":\"pulse_rate\",\"scale\":1,\"type\":\"uint8\"},{\"add\":0,\"endian\":\"little\",\"max\":25,\"min\":0,\"offset\":8,\"parameter\":\"perfusion_index\",\"scale\":0.1,\"type\":\"uint8\"}],\"frame\":{\"checksum\":\"crc8_maxim\",\"length\":12,\"prefixHex\":\"AA550F0801\"},\"kind\":\"acquire\",\"serviceUuid\":\"6e400001-b5a3-f393-e0a9-e50e24dcca9e\"}],\"schemaVersion\":1,\"transport\":\"gatt_sequence\"}",
                    "1.0.0",
                    List.of(new ParameterMapping("spo2", "SpO2", "decimal", "%"),
                            new ParameterMapping("pulse_rate", "Pulse rate", "decimal", "bpm"),
                            new ParameterMapping("perfusion_index", "Perfusion index", "decimal", "%"))),
            new SensorProfileTemplate(
                    "flower_care",
                    "Flower Care",
                    // discovery.serviceUuid must be a service the device actually broadcasts in its
                    // scan advertisement — 00001204 (the sensor-data GATT service used below in
                    // `operations`) is only visible after connecting via discoverServices(), never in
                    // the advertisement itself, so discovery could never match. Real hardware
                    // advertises under fe95 (Xiaomi's shared MiBeacon service); the exact-name check
                    // already disambiguates it from other fe95 devices.
                    "{\"discovery\":{\"nameExact\":\"Flower care\",\"serviceUuid\":\"0000fe95-0000-1000-8000-00805f9b34fb\"},\"goldenPackets\":[{\"characteristicUuid\":\"00001a01-0000-1000-8000-00805f9b34fb\",\"expected\":{\"conductivity\":350,\"light\":12345,\"moisture\":45,\"temperature\":21.5},\"packetHex\":\"D70000393000002D5E01\"}],\"operations\":[{\"characteristicUuid\":\"00001a00-0000-1000-8000-00805f9b34fb\",\"kind\":\"write\",\"payloadHex\":\"A01F\",\"serviceUuid\":\"00001204-0000-1000-8000-00805f9b34fb\",\"timeoutMs\":5000},{\"durationMs\":750,\"kind\":\"delay\"},{\"acquisition\":{\"maxPackets\":1,\"mode\":\"read\",\"timeoutMs\":10000},\"assertions\":[],\"characteristicUuid\":\"00001a01-0000-1000-8000-00805f9b34fb\",\"decoders\":[{\"add\":0,\"endian\":\"little\",\"max\":125,\"min\":-40,\"offset\":0,\"parameter\":\"temperature\",\"scale\":0.1,\"type\":\"int16\"},{\"add\":0,\"endian\":\"little\",\"max\":4294967295,\"min\":0,\"offset\":3,\"parameter\":\"light\",\"scale\":1,\"type\":\"uint32\"},{\"add\":0,\"endian\":\"little\",\"max\":100,\"min\":0,\"offset\":7,\"parameter\":\"moisture\",\"scale\":1,\"type\":\"uint8\"},{\"add\":0,\"endian\":\"little\",\"max\":65535,\"min\":0,\"offset\":8,\"parameter\":\"conductivity\",\"scale\":1,\"type\":\"uint16\"}],\"frame\":{\"checksum\":\"none\",\"length\":10,\"prefixHex\":\"\"},\"kind\":\"acquire\",\"serviceUuid\":\"00001204-0000-1000-8000-00805f9b34fb\"}],\"schemaVersion\":1,\"transport\":\"gatt_sequence\"}",
                    "1.0.0",
                    List.of(new ParameterMapping("temperature", "Temperature", "decimal", "C"),
                            new ParameterMapping("light", "Light", "integer", "lux"),
                            new ParameterMapping("moisture", "Moisture", "decimal", "%"),
                            new ParameterMapping("conductivity", "Conductivity", "integer", "uS/cm"))),
            new SensorProfileTemplate(
                    "inkbird_ibs_th1",
                    "Inkbird IBS-TH1 / TH1 Mini / TH1 Plus",
                    "{\"discovery\":{\"nameExact\":\"sps\",\"serviceUuid\":\"0000fff0-0000-1000-8000-00805f9b34fb\"},\"goldenPackets\":[{\"characteristicUuid\":\"0000fff2-0000-1000-8000-00805f9b34fb\",\"expected\":{\"humidity\":60.81,\"temperature\":19.69},\"packetHex\":\"B107C117000762\"}],\"operations\":[{\"acquisition\":{\"maxPackets\":1,\"mode\":\"read\",\"timeoutMs\":10000},\"assertions\":[],\"characteristicUuid\":\"0000fff2-0000-1000-8000-00805f9b34fb\",\"decoders\":[{\"add\":0,\"endian\":\"little\",\"max\":125,\"min\":-40,\"offset\":0,\"parameter\":\"temperature\",\"scale\":0.01,\"type\":\"int16\"},{\"add\":0,\"endian\":\"little\",\"max\":100,\"min\":0,\"offset\":2,\"parameter\":\"humidity\",\"scale\":0.01,\"type\":\"uint16\"}],\"frame\":{\"checksum\":\"none\",\"length\":7,\"prefixHex\":\"\"},\"kind\":\"acquire\",\"serviceUuid\":\"0000fff0-0000-1000-8000-00805f9b34fb\"}],\"schemaVersion\":1,\"transport\":\"gatt_sequence\"}",
                    "1.0.0",
                    List.of(new ParameterMapping("temperature", "Temperature", "decimal", "C"),
                            new ParameterMapping("humidity", "Humidity", "decimal", "%"))),
            new SensorProfileTemplate(
                    "ruuvi",
                    "Ruuvi Tag 4-in-1",
                    // RuuviTag broadcasts a fixed 24-byte struct as manufacturer-specific data
                    // (company id 0x0499, Ruuvi Innovations) rather than a TLV-framed MiBeacon
                    // payload, so this uses the fixed-offset "decoders" shape (identical to a
                    // gatt_sequence read's decoders) instead of xiaomi_mibeacon_v4_v5's "objects".
                    // Byte layout and the golden packet are from Ruuvi's own published Data Format 5
                    // (RAWv2) specification: temperature/humidity/pressure/acceleration/power/movement/
                    // sequence/MAC, all big-endian; only the four "4-in-1" measurements are decoded.
                    // Pressure is decoded directly to hPa (scale 0.01, add 500) rather than Ruuvi's
                    // native whole-Pascal units: Ruuvi's 1 Pa resolution maps exactly to 2 decimal
                    // digits in hPa, so this is a unit fix, not a precision loss.
                    "{\"advertisement\":{\"decoderId\":\"ruuvi_data_format_5\",\"decoders\":[{\"add\":0,\"endian\":\"big\",\"max\":163.835,\"min\":-163.835,\"offset\":1,\"parameter\":\"temperature\",\"scale\":0.005,\"type\":\"int16\"},{\"add\":0,\"endian\":\"big\",\"max\":163.835,\"min\":0,\"offset\":3,\"parameter\":\"humidity\",\"scale\":0.0025,\"type\":\"uint16\"},{\"add\":500,\"endian\":\"big\",\"max\":1155.35,\"min\":500,\"offset\":5,\"parameter\":\"pressure\",\"scale\":0.01,\"type\":\"uint16\"},{\"add\":0,\"endian\":\"big\",\"max\":254,\"min\":0,\"offset\":15,\"parameter\":\"movement\",\"scale\":1,\"type\":\"uint8\"}],\"matcher\":{\"manufacturerId\":1177}},\"goldenPackets\":[{\"advertisementHex\":\"0512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F\",\"expected\":{\"humidity\":53.49,\"movement\":66,\"pressure\":1000.44,\"temperature\":24.3}}],\"schemaVersion\":1,\"transport\":\"ble_advertisement\"}",
                    "1.0.0",
                    List.of(new ParameterMapping("temperature", "Temperature", "decimal", "C"),
                            new ParameterMapping("humidity", "Humidity", "decimal", "%"),
                            new ParameterMapping("pressure", "Pressure", "decimal", "hPa"),
                            new ParameterMapping("movement", "Movement", "integer"))));

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
