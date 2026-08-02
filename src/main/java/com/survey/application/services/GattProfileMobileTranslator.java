package com.survey.application.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.survey.domain.models.SensorGattProfile;
import org.springframework.stereotype.Component;

import java.util.HexFormat;

@Component
public class GattProfileMobileTranslator {

    private final ObjectMapper objectMapper;

    public GattProfileMobileTranslator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public record MobileProfile(String sensorTypeCode, JsonNode spec) {}

    public MobileProfile translate(SensorGattProfile entity) {
        JsonNode spec = parseSpec(entity.getSpecJson());
        String transport = spec.path("transport").asText("gatt_sequence");
        String sensorTypeCode = entity.getSensorType().getCode();
        int revision = entity.getRevision();
        int minEngineVersion = parseMajorVersion(entity.getMinEngineVersion());

        ObjectNode mobile = objectMapper.createObjectNode();
        mobile.put("schemaVersion", 1);
        mobile.put("revision", revision);
        mobile.put("sensorTypeCode", sensorTypeCode);
        mobile.put("minEngineVersion", minEngineVersion);
        mobile.put("transport", transport);

        if ("gatt_sequence".equals(transport)) {
            mobile.set("discovery", translateGattDiscovery(spec.path("discovery")));
            translateOperations(spec.path("operations"), mobile);
        } else {
            mobile.set("discovery", translateAdvertisementDiscovery(spec.path("advertisement")));
            mobile.set("advertisement", translateAdvertisement(spec.path("advertisement")));
            mobile.set("reads", objectMapper.createArrayNode());
            mobile.set("actions", objectMapper.createArrayNode());
        }
        mobile.set("goldenVectors", objectMapper.createArrayNode());
        return new MobileProfile(sensorTypeCode, mobile);
    }

    private ObjectNode translateGattDiscovery(JsonNode discovery) {
        ObjectNode out = objectMapper.createObjectNode();
        // Mobile treats nameExact + namePrefix together as ambiguous, so prefer the more
        // specific exact match (it may still contain a "{sensorId}" template mobile resolves
        // per-device) and only fall back to a prefix when no exact name is configured.
        if (discovery.has("nameExact")) {
            out.put("exactName", discovery.path("nameExact").asText());
        } else if (discovery.has("namePrefix")) {
            out.put("namePrefix", discovery.path("namePrefix").asText());
        }
        if (discovery.has("serviceUuid")) {
            out.put("advertisedServiceUuid", discovery.path("serviceUuid").asText());
        }
        return out;
    }

    private ObjectNode translateAdvertisementDiscovery(JsonNode advertisement) {
        ObjectNode out = objectMapper.createObjectNode();
        JsonNode matcher = advertisement.path("matcher");
        if (matcher.has("serviceUuid")) {
            out.put("advertisedServiceUuid", matcher.path("serviceUuid").asText());
        } else if (advertisement.has("matcher") && matcher.has("productId")) {
            out.put("advertisedServiceUuid", "0000fe95-0000-1000-8000-00805f9b34fb");
        }
        return out;
    }

    private ObjectNode translateAdvertisement(JsonNode advertisement) {
        ObjectNode out = objectMapper.createObjectNode();
        out.put("decoderId", advertisement.path("decoderId").asText());
        JsonNode matcher = advertisement.path("matcher");
        String serviceUuid = matcher.has("serviceUuid")
                ? matcher.path("serviceUuid").asText()
                : "0000fe95-0000-1000-8000-00805f9b34fb";
        out.put("serviceUuid", serviceUuid);
        if (matcher.has("manufacturerId")) {
            out.put("dataSource", "manufacturer_data");
            out.put("manufacturerId", matcher.path("manufacturerId").asInt());
        } else {
            out.put("dataSource", "service_data");
        }
        out.put("productId", matcher.path("productId").asInt());
        out.put("timeoutMilliseconds", 10000);
        out.put("maxPackets", 100);
        ArrayNode objects = objectMapper.createArrayNode();
        advertisement.path("objects").forEach(object -> {
            ObjectNode mobileObject = objectMapper.createObjectNode();
            mobileObject.put("objectId", object.path("objectId").asText());
            mobileObject.put("parameterCode", object.path("parameter").asText());
            mobileObject.put("type", object.path("type").asText());
            objects.add(mobileObject);
        });
        out.set("objects", objects);
        return out;
    }

    private void translateOperations(JsonNode operations, ObjectNode target) {
        ArrayNode actions = objectMapper.createArrayNode();
        ArrayNode reads = objectMapper.createArrayNode();

        if (operations.isArray()) {
            for (JsonNode op : operations) {
                String kind = op.path("kind").asText();
                switch (kind) {
                    case "write" -> actions.add(translateWriteAction(op));
                    case "delay" -> actions.add(translateDelayAction(op));
                    case "acquire" -> reads.add(translateAcquireRead(op));
                    default -> { }
                }
            }
        }
        target.set("actions", actions);
        target.set("reads", reads);
    }

    private ObjectNode translateWriteAction(JsonNode op) {
        ObjectNode action = objectMapper.createObjectNode();
        action.put("type", "write");
        action.put("serviceUuid", op.path("serviceUuid").asText());
        action.put("characteristicUuid", op.path("characteristicUuid").asText());
        byte[] bytes = parseHexQuiet(op.path("payloadHex").asText(""));
        ArrayNode value = objectMapper.createArrayNode();
        for (byte b : bytes) {
            value.add(Byte.toUnsignedInt(b));
        }
        action.set("value", value);
        return action;
    }

    private ObjectNode translateDelayAction(JsonNode op) {
        ObjectNode action = objectMapper.createObjectNode();
        action.put("type", "delay");
        action.put("milliseconds", op.path("durationMs").asInt());
        return action;
    }

    private ObjectNode translateAcquireRead(JsonNode op) {
        ObjectNode read = objectMapper.createObjectNode();
        read.put("serviceUuid", op.path("serviceUuid").asText());
        read.put("characteristicUuid", op.path("characteristicUuid").asText());
        read.set("acquisition", translateAcquisition(op.path("acquisition")));
        read.set("frame", translateFrame(op.path("frame")));
        read.set("assertions", translateAssertions(op.path("assertions")));
        read.set("fields", translateDecoders(op.path("decoders")));
        return read;
    }

    private ObjectNode translateAcquisition(JsonNode acquisition) {
        ObjectNode out = objectMapper.createObjectNode();
        out.put("mode", acquisition.path("mode").asText("read"));
        out.put("timeoutMilliseconds", acquisition.path("timeoutMs").asInt(5000));
        out.put("maxPackets", acquisition.path("maxPackets").asInt(1));
        return out;
    }

    private ObjectNode translateFrame(JsonNode frame) {
        ObjectNode out = objectMapper.createObjectNode();
        int length = frame.path("length").asInt(0);
        if (length > 0) {
            out.put("exactLength", length);
        }
        String prefixHex = frame.path("prefixHex").asText("");
        if (!prefixHex.isEmpty()) {
            byte[] bytes = parseHexQuiet(prefixHex);
            ArrayNode prefix = objectMapper.createArrayNode();
            for (byte b : bytes) {
                prefix.add(Byte.toUnsignedInt(b));
            }
            out.set("prefix", prefix);
        }
        out.put("checksum", frame.path("checksum").asText("none"));
        return out;
    }

    private ArrayNode translateAssertions(JsonNode assertions) {
        ArrayNode out = objectMapper.createArrayNode();
        if (assertions.isArray()) {
            for (JsonNode assertion : assertions) {
                ObjectNode a = objectMapper.createObjectNode();
                a.put("byteOffset", assertion.path("offset").asInt());
                ArrayNode equals = objectMapper.createArrayNode();
                equals.add(assertion.path("equals").asInt());
                a.set("equals", equals);
                out.add(a);
            }
        }
        return out;
    }

    private ArrayNode translateDecoders(JsonNode decoders) {
        ArrayNode out = objectMapper.createArrayNode();
        if (decoders.isArray()) {
            for (JsonNode decoder : decoders) {
                ObjectNode field = objectMapper.createObjectNode();
                field.put("parameterCode", decoder.path("parameter").asText());
                field.put("type", decoder.path("type").asText());
                field.put("endian", decoder.path("endian").asText("little"));
                field.put("byteOffset", decoder.path("offset").asInt());
                field.put("scale", decoder.path("scale").asDouble(1.0));
                field.put("valueOffset", decoder.path("add").asDouble(0.0));
                if (decoder.has("min")) {
                    field.put("minimum", decoder.path("min").asDouble());
                }
                if (decoder.has("max")) {
                    field.put("maximum", decoder.path("max").asDouble());
                }
                out.add(field);
            }
        }
        return out;
    }

    private static int parseMajorVersion(String version) {
        if (version == null || version.isBlank()) {
            return 1;
        }
        try {
            return Integer.parseInt(version.split("\\.")[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            return 1;
        }
    }

    private JsonNode parseSpec(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception exception) {
            throw new IllegalStateException("Stored GATT profile spec is invalid.", exception);
        }
    }

    private static byte[] parseHexQuiet(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        try {
            return HexFormat.of().parseHex(hex);
        } catch (IllegalArgumentException ignored) {
            return new byte[0];
        }
    }
}
