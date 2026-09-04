package com.survey.application.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.application.dtos.GattProfileValidationDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GattProfileValidatorTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GattProfileValidator validator = new GattProfileValidator(objectMapper);

    @Test
    void validate_supportsBoundedAcquisitionAllPrimitivesAndSfloat() throws Exception {
        JsonNode spec = objectMapper.readTree("""
                {
                  "schemaVersion":1,
                  "transport":"gatt_sequence",
                  "discovery":{"namePrefix":"TEST-","serviceUuid":"12630000-cc25-497d-9854-9b6c02c77054"},
                  "operations":[{
                    "kind":"acquire",
                    "serviceUuid":"12630000-cc25-497d-9854-9b6c02c77054",
                    "characteristicUuid":"12630001-cc25-497d-9854-9b6c02c77054",
                    "acquisition":{"mode":"indication","timeoutMs":5000,"maxPackets":2},
                    "frame":{"length":21,"prefixHex":"AA","checksum":"none"},
                    "assertions":[{"offset":0,"equals":170}],
                    "decoders":[
                      {"parameter":"u8","type":"uint8","offset":1,"endian":"little","scale":1,"add":0,"min":0,"max":255},
                      {"parameter":"i8","type":"int8","offset":2,"endian":"little","scale":1,"add":0,"min":-128,"max":127},
                      {"parameter":"u16","type":"uint16","offset":3,"endian":"little","scale":1,"add":0,"min":0,"max":65535},
                      {"parameter":"i16","type":"int16","offset":5,"endian":"little","scale":1,"add":0,"min":-32768,"max":32767},
                      {"parameter":"u32","type":"uint32","offset":7,"endian":"little","scale":1,"add":0,"min":0,"max":4294967295},
                      {"parameter":"i32","type":"int32","offset":11,"endian":"little","scale":1,"add":0,"min":-2147483648,"max":2147483647},
                      {"parameter":"f32","type":"float32","offset":15,"endian":"little","scale":1,"add":0,"min":-10,"max":10},
                      {"parameter":"sf","type":"sfloat16","offset":19,"endian":"little","scale":1,"add":0,"min":0,"max":100}
                    ]
                  }],
                  "goldenPackets":[{
                    "characteristicUuid":"12630001-cc25-497d-9854-9b6c02c77054",
                    "packetHex":"AAFFFF3412FEFF78563412FFFFFFFF0000C03F6200",
                    "expected":{"u8":255,"i8":-1,"u16":4660,"i16":-2,"u32":305419896,"i32":-1,"f32":1.5,"sf":98}
                  }]
                }
                """);

        GattProfileValidationDto result = validator.validate(spec);

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
        assertThat(result.canonicalHash()).matches("[0-9a-f]{64}");
    }

    @Test
    void validate_acceptsFlowerSequenceAndCrcNotification() throws Exception {
        JsonNode spec = objectMapper.readTree("""
                {
                  "schemaVersion":1,"transport":"gatt_sequence",
                  "discovery":{"nameExact":"PC-60FW","serviceUuid":"6e400001-b5a3-f393-e0a9-e50e24dcca9e"},
                  "operations":[
                    {"kind":"write","serviceUuid":"6e400001-b5a3-f393-e0a9-e50e24dcca9e",
                     "characteristicUuid":"6e400002-b5a3-f393-e0a9-e50e24dcca9e","payloadHex":"A01F","timeoutMs":5000},
                    {"kind":"delay","durationMs":750},
                    {"kind":"acquire","serviceUuid":"6e400001-b5a3-f393-e0a9-e50e24dcca9e",
                     "characteristicUuid":"6e400003-b5a3-f393-e0a9-e50e24dcca9e",
                     "acquisition":{"mode":"notification","timeoutMs":30000,"maxPackets":10},
                     "frame":{"length":12,"prefixHex":"AA550F0801","checksum":"crc8_maxim"},
                     "assertions":[],
                     "decoders":[
                       {"parameter":"spo2","type":"uint8","offset":5,"endian":"little","scale":1,"add":0,"min":0,"max":100}
                     ]}
                  ],
                  "goldenPackets":[{"characteristicUuid":"6e400003-b5a3-f393-e0a9-e50e24dcca9e",
                    "packetHex":"AA550F08016248000F000079","expected":{"spo2":98}}]
                }
                """);

        assertThat(validator.validate(spec).valid()).isTrue();
    }

    @Test
    void validate_rejectsUnboundedOperationsAndUnlistedAdvertisementDecoder() throws Exception {
        JsonNode gatt = objectMapper.readTree("""
                {
                  "schemaVersion":1,"transport":"gatt_sequence",
                  "discovery":{"serviceUuid":"12630000-cc25-497d-9854-9b6c02c77054"},
                  "operations":[
                    {"kind":"write","serviceUuid":"12630000-cc25-497d-9854-9b6c02c77054",
                     "characteristicUuid":"12630001-cc25-497d-9854-9b6c02c77054",
                     "payloadHex":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                     "timeoutMs":90000},
                    {"kind":"delay","durationMs":6000}
                  ],
                  "goldenPackets":[]
                }
                """);
        JsonNode advertisement = objectMapper.readTree("""
                {
                  "schemaVersion":1,"transport":"ble_advertisement",
                  "advertisement":{"matcher":{"productId":2443},"decoderId":"javascript",
                    "objects":[{"objectId":"0x1019","parameter":"opening","type":"uint8"}]},
                  "goldenPackets":[{"advertisementHex":"00","expected":{"opening":0}}]
                }
                """);

        assertThat(validator.validate(gatt).errors())
                .anyMatch(error -> error.contains("exceeds 64 bytes"))
                .anyMatch(error -> error.contains("timeoutMs"))
                .anyMatch(error -> error.contains("durationMs"));
        assertThat(validator.validate(advertisement).errors())
                .anyMatch(error -> error.contains("not whitelisted"));
    }

    @Test
    void validate_acceptsRuuviFixedOffsetAdvertisementAndDecodesItsGoldenPacket() throws Exception {
        JsonNode spec = objectMapper.readTree("""
                {
                  "schemaVersion":1,"transport":"ble_advertisement",
                  "advertisement":{
                    "decoderId":"ruuvi_data_format_5",
                    "matcher":{"manufacturerId":1177},
                    "decoders":[
                      {"parameter":"temperature","type":"int16","offset":1,"endian":"big","scale":0.005,"add":0,"min":-163.835,"max":163.835},
                      {"parameter":"humidity","type":"uint16","offset":3,"endian":"big","scale":0.0025,"add":0,"min":0,"max":163.835},
                      {"parameter":"pressure","type":"uint16","offset":5,"endian":"big","scale":1,"add":50000,"min":50000,"max":115535},
                      {"parameter":"movement","type":"uint8","offset":15,"endian":"big","scale":1,"add":0,"min":0,"max":254}
                    ]
                  },
                  "goldenPackets":[{
                    "advertisementHex":"0512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F",
                    "expected":{"temperature":24.3,"humidity":53.49,"pressure":100044,"movement":66}
                  }]
                }
                """);

        GattProfileValidationDto result = validator.validate(spec);

        assertThat(result.errors()).isEmpty();
        assertThat(result.valid()).isTrue();
        assertThat(result.goldenVectors()).singleElement().satisfies(vector ->
                assertThat(vector.decodedValues())
                        .containsEntry("temperature", 24.3)
                        .containsEntry("humidity", 53.49)
                        .containsEntry("pressure", 100044.0)
                        .containsEntry("movement", 66.0));
    }

    @Test
    void validate_rejectsRuuviSpecMissingManufacturerIdOrCarryingXiaomiFields() throws Exception {
        JsonNode missingManufacturerId = objectMapper.readTree("""
                {
                  "schemaVersion":1,"transport":"ble_advertisement",
                  "advertisement":{
                    "decoderId":"ruuvi_data_format_5",
                    "matcher":{"productId":1},
                    "decoders":[{"parameter":"temperature","type":"int16","offset":1,"endian":"big","scale":0.005,"add":0,"min":-163.835,"max":163.835}]
                  },
                  "goldenPackets":[{"advertisementHex":"00","expected":{"temperature":0}}]
                }
                """);
        JsonNode mixedShapes = objectMapper.readTree("""
                {
                  "schemaVersion":1,"transport":"ble_advertisement",
                  "advertisement":{
                    "decoderId":"ruuvi_data_format_5",
                    "matcher":{"manufacturerId":1177},
                    "objects":[{"objectId":"0x1019","parameter":"opening","type":"uint8"}]
                  },
                  "goldenPackets":[{"advertisementHex":"00","expected":{"opening":0}}]
                }
                """);

        assertThat(validator.validate(missingManufacturerId).errors())
                .anyMatch(error -> error.contains("manufacturerId is required"))
                .anyMatch(error -> error.contains("productId is not used"));
        assertThat(validator.validate(mixedShapes).errors())
                .anyMatch(error -> error.contains("objects is only valid for a TLV decoder"));
    }

    @Test
    void validate_rejectsRuuviGoldenPacketWithWrongExpectedValue() throws Exception {
        JsonNode spec = objectMapper.readTree("""
                {
                  "schemaVersion":1,"transport":"ble_advertisement",
                  "advertisement":{
                    "decoderId":"ruuvi_data_format_5",
                    "matcher":{"manufacturerId":1177},
                    "decoders":[{"parameter":"temperature","type":"int16","offset":1,"endian":"big","scale":0.005,"add":0,"min":-163.835,"max":163.835}]
                  },
                  "goldenPackets":[{
                    "advertisementHex":"0512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F",
                    "expected":{"temperature":99.9}
                  }]
                }
                """);

        assertThat(validator.validate(spec).errors())
                .anyMatch(error -> error.contains("does not match decoded value"));
    }
}
