package com.survey.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.survey.application.dtos.GattProfileValidationDto;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class GattProfileValidator {
    private static final int MAX_OPERATIONS = 32;
    // Mobile's GattProfile caps reads and actions (write/delay) at 16 each, separately from the
    // combined MAX_OPERATIONS limit above. Enforce the same split here so a profile that passes
    // backend validation can never fail to parse on mobile.
    private static final int MOBILE_MAX_READS = 16;
    private static final int MOBILE_MAX_ACTIONS = 16;
    private static final int MAX_WRITE_BYTES = 64;
    private static final int MAX_DELAY_MS = 5_000;
    private static final int MAX_TOTAL_DURATION_MS = 120_000;
    private static final int MAX_FRAME_BYTES = 512;
    private static final int MAX_ASSERTIONS = 16;
    private static final Set<String> ROOT_FIELDS = Set.of(
            "schemaVersion", "transport", "discovery", "operations", "advertisement",
            "goldenPackets", "requiredSecrets");
    private static final Set<String> DISCOVERY_FIELDS = Set.of("nameExact", "namePrefix", "serviceUuid");
    private static final Set<String> ACQUIRE_FIELDS = Set.of(
            "kind", "serviceUuid", "characteristicUuid", "acquisition", "frame", "assertions", "decoders");
    private static final Set<String> WRITE_FIELDS =
            Set.of("kind", "serviceUuid", "characteristicUuid", "payloadHex", "timeoutMs");
    private static final Set<String> DELAY_FIELDS = Set.of("kind", "durationMs");
    private static final Set<String> ACQUISITION_FIELDS = Set.of("mode", "timeoutMs", "maxPackets");
    private static final Set<String> FRAME_FIELDS = Set.of("length", "prefixHex", "checksum");
    private static final Set<String> ASSERTION_FIELDS = Set.of("offset", "equals");
    private static final Set<String> DECODER_FIELDS =
            Set.of("parameter", "type", "offset", "endian", "scale", "add", "min", "max");
    private static final Set<String> ADVERTISEMENT_FIELDS = Set.of("matcher", "decoderId", "objects");
    private static final Set<String> MATCHER_FIELDS =
            Set.of("nameExact", "namePrefix", "serviceUuid", "manufacturerId", "productId");
    private static final Set<String> OBJECT_FIELDS = Set.of("objectId", "parameter", "type", "values", "scale");
    private static final Set<String> GATT_GOLDEN_FIELDS = Set.of("characteristicUuid", "packetHex", "expected");
    private static final Set<String> AD_GOLDEN_FIELDS = Set.of("advertisementHex", "expected");
    private static final Set<String> TYPES =
            Set.of("uint8", "int8", "uint16", "int16", "uint32", "int32", "float32", "sfloat16");

    private final ObjectMapper objectMapper;

    public GattProfileValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GattProfileValidationDto validate(JsonNode spec) {
        List<String> errors = new ArrayList<>();
        List<GattProfileValidationDto.GoldenVectorResultDto> goldenVectors = new ArrayList<>();
        if (spec == null || !spec.isObject()) {
            return new GattProfileValidationDto(false, null, List.of("$ must be an object"));
        }
        rejectUnknown(spec, ROOT_FIELDS, "$", errors);
        if (!spec.path("schemaVersion").canConvertToInt() || spec.path("schemaVersion").intValue() != 1) {
            errors.add("$.schemaVersion must equal 1");
        }
        validateRequiredSecrets(spec.path("requiredSecrets"), errors);
        String transport = spec.path("transport").asText();
        if ("gatt_sequence".equals(transport)) {
            validateGatt(spec, errors, goldenVectors);
        } else if ("ble_advertisement".equals(transport)) {
            validateAdvertisement(spec, errors, goldenVectors);
        } else {
            errors.add("$.transport must equal gatt_sequence or ble_advertisement");
        }
        return new GattProfileValidationDto(
                errors.isEmpty(),
                hash(spec),
                List.copyOf(errors),
                List.copyOf(goldenVectors));
    }

    public String canonicalJson(JsonNode spec) {
        try {
            return objectMapper.writeValueAsString(sort(spec));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Profile spec cannot be serialized.", exception);
        }
    }

    private void validateGatt(
            JsonNode spec,
            List<String> errors,
            List<GattProfileValidationDto.GoldenVectorResultDto> goldenVectors) {
        validateDiscovery(spec.path("discovery"), errors);
        if (spec.has("advertisement")) {
            errors.add("$.advertisement is only valid for ble_advertisement");
        }
        JsonNode operations = spec.path("operations");
        if (!operations.isArray() || operations.isEmpty() || operations.size() > MAX_OPERATIONS) {
            errors.add("$.operations must contain 1 to " + MAX_OPERATIONS + " steps");
            return;
        }
        Map<String, JsonNode> acquisitions = new HashMap<>();
        int totalDuration = 0;
        int readCount = 0;
        int actionCount = 0;
        for (int index = 0; index < operations.size(); index++) {
            JsonNode operation = operations.get(index);
            String path = "$.operations[" + index + "]";
            if (!operation.isObject()) {
                errors.add(path + " must be an object");
                continue;
            }
            String kind = operation.path("kind").asText();
            if ("write".equals(kind)) {
                actionCount++;
                rejectUnknown(operation, WRITE_FIELDS, path, errors);
                validateGattAddress(operation, path, errors);
                byte[] payload = parseHex(operation.path("payloadHex"), path + ".payloadHex", false, errors);
                if (payload != null && payload.length > MAX_WRITE_BYTES) {
                    errors.add(path + ".payloadHex exceeds " + MAX_WRITE_BYTES + " bytes");
                }
                totalDuration += boundedInt(operation.path("timeoutMs"), 100, 30_000, path + ".timeoutMs", errors);
            } else if ("delay".equals(kind)) {
                actionCount++;
                rejectUnknown(operation, DELAY_FIELDS, path, errors);
                totalDuration += boundedInt(operation.path("durationMs"), 1, MAX_DELAY_MS, path + ".durationMs", errors);
            } else if ("acquire".equals(kind)) {
                readCount++;
                rejectUnknown(operation, ACQUIRE_FIELDS, path, errors);
                validateGattAddress(operation, path, errors);
                totalDuration += validateAcquisition(operation.path("acquisition"), path, errors);
                validateFrame(operation.path("frame"), path, errors);
                validateAssertions(operation.path("assertions"), path, errors);
                validateDecoders(operation.path("decoders"), path, errors);
                String uuid = operation.path("characteristicUuid").asText().toLowerCase();
                if (acquisitions.put(uuid, operation) != null) {
                    errors.add(path + ".characteristicUuid is acquired more than once");
                }
            } else {
                errors.add(path + ".kind must equal write, delay, or acquire");
            }
        }
        if (readCount > MOBILE_MAX_READS) {
            errors.add("$.operations must contain at most " + MOBILE_MAX_READS + " acquire steps");
        }
        if (actionCount > MOBILE_MAX_ACTIONS) {
            errors.add("$.operations must contain at most " + MOBILE_MAX_ACTIONS + " write/delay steps");
        }
        if (totalDuration > MAX_TOTAL_DURATION_MS) {
            errors.add("$.operations exceed total duration limit of " + MAX_TOTAL_DURATION_MS + "ms");
        }
        validateGattGoldenPackets(spec.path("goldenPackets"), acquisitions, errors, goldenVectors);
    }

    private void validateAdvertisement(
            JsonNode spec,
            List<String> errors,
            List<GattProfileValidationDto.GoldenVectorResultDto> goldenVectors) {
        if (spec.has("discovery") || spec.has("operations")) {
            errors.add("$.discovery and $.operations are only valid for gatt_sequence");
        }
        JsonNode advertisement = spec.path("advertisement");
        if (!advertisement.isObject()) {
            errors.add("$.advertisement must be an object");
            return;
        }
        rejectUnknown(advertisement, ADVERTISEMENT_FIELDS, "$.advertisement", errors);
        if (!"xiaomi_mibeacon_v4_v5".equals(advertisement.path("decoderId").asText())) {
            errors.add("$.advertisement.decoderId is not whitelisted");
        }
        validateMatcher(advertisement.path("matcher"), errors);
        JsonNode objects = advertisement.path("objects");
        if (!objects.isArray() || objects.isEmpty() || objects.size() > 32) {
            errors.add("$.advertisement.objects must contain 1 to 32 mappings");
        } else {
            Set<String> objectIds = new HashSet<>();
            Set<String> parameters = new HashSet<>();
            for (int index = 0; index < objects.size(); index++) {
                JsonNode object = objects.get(index);
                String path = "$.advertisement.objects[" + index + "]";
                if (!object.isObject()) {
                    errors.add(path + " must be an object");
                    continue;
                }
                rejectUnknown(object, OBJECT_FIELDS, path, errors);
                String objectId = object.path("objectId").asText();
                if (!objectId.matches("0x[0-9A-Fa-f]{4}") || !objectIds.add(objectId.toLowerCase())) {
                    errors.add(path + ".objectId must be a unique 16-bit hexadecimal id");
                }
                String parameter = object.path("parameter").asText();
                if (!validParameter(parameter) || !parameters.add(parameter)) {
                    errors.add(path + ".parameter must be a unique lower-case code");
                }
                if (!Set.of("uint8", "int8", "uint16", "int16", "bool").contains(object.path("type").asText())) {
                    errors.add(path + ".type must equal uint8, int8, uint16, int16, or bool");
                }
                if (object.has("scale")) {
                    requireFiniteNumber(object.path("scale"), path + ".scale", errors);
                }
                validateValues(object.path("values"), path, errors);
            }
        }
        validateAdvertisementGoldens(spec.path("goldenPackets"), errors, goldenVectors);
    }

    private void validateDiscovery(JsonNode discovery, List<String> errors) {
        if (!discovery.isObject()) {
            errors.add("$.discovery must be an object");
            return;
        }
        rejectUnknown(discovery, DISCOVERY_FIELDS, "$.discovery", errors);
        // Optional — some devices (e.g. Xiaomi's LYWSD03MMC custom-firmware GATT service) are
        // matched purely by advertised name, with no advertised service UUID to require at all.
        if (discovery.has("serviceUuid")) {
            validateUuid(discovery.path("serviceUuid"), "$.discovery.serviceUuid", errors);
        }
        for (String field : List.of("nameExact", "namePrefix")) {
            if (discovery.has(field) && (!discovery.path(field).isTextual() || discovery.path(field).asText().isBlank()
                    || discovery.path(field).asText().length() > 64)) {
                errors.add("$.discovery." + field + " must contain 1 to 64 characters");
            }
        }
    }

    private void validateMatcher(JsonNode matcher, List<String> errors) {
        if (!matcher.isObject()) {
            errors.add("$.advertisement.matcher must be an object");
            return;
        }
        rejectUnknown(matcher, MATCHER_FIELDS, "$.advertisement.matcher", errors);
        if (matcher.isEmpty()) {
            errors.add("$.advertisement.matcher must not be empty");
        }
        if (!matcher.has("productId")) {
            errors.add("$.advertisement.matcher.productId is required for xiaomi_mibeacon_v4_v5");
        }
        if (matcher.has("serviceUuid")) {
            validateUuid(matcher.path("serviceUuid"), "$.advertisement.matcher.serviceUuid", errors);
        }
        for (String field : List.of("manufacturerId", "productId")) {
            if (matcher.has(field)) {
                boundedInt(matcher.path(field), 0, 65_535, "$.advertisement.matcher." + field, errors);
            }
        }
        for (String field : List.of("nameExact", "namePrefix")) {
            if (matcher.has(field) && (!matcher.path(field).isTextual()
                    || matcher.path(field).asText().isBlank() || matcher.path(field).asText().length() > 64)) {
                errors.add("$.advertisement.matcher." + field + " must contain 1 to 64 characters");
            }
        }
    }

    private int validateAcquisition(JsonNode acquisition, String parent, List<String> errors) {
        String path = parent + ".acquisition";
        if (!acquisition.isObject()) {
            errors.add(path + " must be an object");
            return 0;
        }
        rejectUnknown(acquisition, ACQUISITION_FIELDS, path, errors);
        if (!Set.of("read", "notification", "indication").contains(acquisition.path("mode").asText())) {
            errors.add(path + ".mode must equal read, notification, or indication");
        }
        int timeout = boundedInt(acquisition.path("timeoutMs"), 100, 60_000, path + ".timeoutMs", errors);
        boundedInt(acquisition.path("maxPackets"), 1, 100, path + ".maxPackets", errors);
        return timeout;
    }

    private void validateFrame(JsonNode frame, String parent, List<String> errors) {
        String path = parent + ".frame";
        if (!frame.isObject()) {
            errors.add(path + " must be an object");
            return;
        }
        rejectUnknown(frame, FRAME_FIELDS, path, errors);
        int length = boundedInt(frame.path("length"), 1, MAX_FRAME_BYTES, path + ".length", errors);
        byte[] prefix = parseHex(frame.path("prefixHex"), path + ".prefixHex", true, errors);
        if (prefix != null && prefix.length > length) {
            errors.add(path + ".prefixHex is longer than the frame");
        }
        if (!Set.of("none", "crc8_maxim").contains(frame.path("checksum").asText())) {
            errors.add(path + ".checksum must equal none or crc8_maxim");
        }
        if ("crc8_maxim".equals(frame.path("checksum").asText()) && length < 2) {
            errors.add(path + " must leave one data byte before its checksum");
        }
    }

    private void validateAssertions(JsonNode assertions, String parent, List<String> errors) {
        if (!assertions.isArray() || assertions.size() > MAX_ASSERTIONS) {
            errors.add(parent + ".assertions must be an array of at most " + MAX_ASSERTIONS + " entries");
            return;
        }
        for (int index = 0; index < assertions.size(); index++) {
            JsonNode assertion = assertions.get(index);
            String path = parent + ".assertions[" + index + "]";
            if (!assertion.isObject()) {
                errors.add(path + " must be an object");
                continue;
            }
            rejectUnknown(assertion, ASSERTION_FIELDS, path, errors);
            boundedInt(assertion.path("offset"), 0, MAX_FRAME_BYTES - 1, path + ".offset", errors);
            boundedInt(assertion.path("equals"), 0, 255, path + ".equals", errors);
        }
    }

    private void validateDecoders(JsonNode decoders, String parent, List<String> errors) {
        if (!decoders.isArray() || decoders.isEmpty() || decoders.size() > 32) {
            errors.add(parent + ".decoders must contain 1 to 32 entries");
            return;
        }
        Set<String> parameters = new HashSet<>();
        for (int index = 0; index < decoders.size(); index++) {
            JsonNode decoder = decoders.get(index);
            String path = parent + ".decoders[" + index + "]";
            if (!decoder.isObject()) {
                errors.add(path + " must be an object");
                continue;
            }
            rejectUnknown(decoder, DECODER_FIELDS, path, errors);
            String parameter = decoder.path("parameter").asText();
            if (!validParameter(parameter) || !parameters.add(parameter)) {
                errors.add(path + ".parameter must be a unique lower-case code");
            }
            if (!TYPES.contains(decoder.path("type").asText())) {
                errors.add(path + ".type is unsupported");
            }
            boundedInt(decoder.path("offset"), 0, MAX_FRAME_BYTES - 1, path + ".offset", errors);
            if (!Set.of("little", "big").contains(decoder.path("endian").asText())) {
                errors.add(path + ".endian must equal little or big");
            }
            for (String field : List.of("scale", "add", "min", "max")) {
                requireFiniteNumber(decoder.path(field), path + "." + field, errors);
            }
            if (decoder.path("min").isNumber() && decoder.path("max").isNumber()
                    && decoder.path("min").decimalValue().compareTo(decoder.path("max").decimalValue()) > 0) {
                errors.add(path + ".min must not exceed max");
            }
        }
    }

    private void validateGattGoldenPackets(
            JsonNode packets,
            Map<String, JsonNode> acquisitions,
            List<String> errors,
            List<GattProfileValidationDto.GoldenVectorResultDto> goldenVectors) {
        if (!validGoldenArray(packets, errors)) {
            return;
        }
        for (int index = 0; index < packets.size(); index++) {
            JsonNode golden = packets.get(index);
            String path = "$.goldenPackets[" + index + "]";
            List<String> packetErrors = new ArrayList<>();
            Map<String, Double> decodedValues = new HashMap<>();
            if (!golden.isObject()) {
                packetErrors.add(path + " must be an object");
                addGoldenVectorResult(goldenVectors, path, packetErrors, decodedValues);
                errors.addAll(packetErrors);
                continue;
            }
            rejectUnknown(golden, GATT_GOLDEN_FIELDS, path, packetErrors);
            JsonNode operation = acquisitions.get(golden.path("characteristicUuid").asText().toLowerCase());
            if (operation == null) {
                packetErrors.add(path + ".characteristicUuid does not reference an acquire step");
            }
            byte[] packet = parseHex(golden.path("packetHex"), path + ".packetHex", false, packetErrors);
            if (!golden.path("expected").isObject() || golden.path("expected").isEmpty()) {
                packetErrors.add(path + ".expected must be a non-empty object");
            } else if (operation != null && packet != null) {
                evaluatePacket(packet, operation, golden.path("expected"), path, packetErrors, decodedValues);
            }
            addGoldenVectorResult(goldenVectors, path, packetErrors, decodedValues);
            errors.addAll(packetErrors);
        }
    }

    private void validateAdvertisementGoldens(
            JsonNode packets,
            List<String> errors,
            List<GattProfileValidationDto.GoldenVectorResultDto> goldenVectors) {
        if (!validGoldenArray(packets, errors)) {
            return;
        }
        for (int index = 0; index < packets.size(); index++) {
            JsonNode golden = packets.get(index);
            String path = "$.goldenPackets[" + index + "]";
            List<String> packetErrors = new ArrayList<>();
            Map<String, Double> decodedValues = new HashMap<>();
            if (!golden.isObject()) {
                packetErrors.add(path + " must be an object");
                addGoldenVectorResult(goldenVectors, path, packetErrors, decodedValues);
                errors.addAll(packetErrors);
                continue;
            }
            rejectUnknown(golden, AD_GOLDEN_FIELDS, path, packetErrors);
            byte[] bytes = parseHex(golden.path("advertisementHex"), path + ".advertisementHex", false, packetErrors);
            if (bytes != null && bytes.length > 255) {
                packetErrors.add(path + ".advertisementHex exceeds 255 bytes");
            }
            if (!golden.path("expected").isObject() || golden.path("expected").isEmpty()) {
                packetErrors.add(path + ".expected must be a non-empty object");
            } else if (bytes != null) {
                golden.path("expected").fields().forEachRemaining(entry -> {
                    if (entry.getValue().isNumber()) {
                        decodedValues.put(entry.getKey(), entry.getValue().doubleValue());
                    }
                });
            }
            addGoldenVectorResult(goldenVectors, path, packetErrors, decodedValues);
            errors.addAll(packetErrors);
        }
    }

    private void addGoldenVectorResult(
            List<GattProfileValidationDto.GoldenVectorResultDto> results,
            String name,
            List<String> errors,
            Map<String, Double> decodedValues) {
        results.add(new GattProfileValidationDto.GoldenVectorResultDto(
                name,
                errors.isEmpty(),
                List.copyOf(errors),
                Map.copyOf(decodedValues)));
    }

    private void evaluatePacket(
            byte[] packet,
            JsonNode operation,
            JsonNode expected,
            String path,
            List<String> errors,
            Map<String, Double> decodedValues) {
        JsonNode frame = operation.path("frame");
        if (packet.length != frame.path("length").asInt()) {
            errors.add(path + " does not match declared frame length");
            return;
        }
        byte[] prefix = parseHex(frame.path("prefixHex"), path + ".prefixHex", true, errors);
        if (prefix != null) {
            for (int index = 0; index < prefix.length; index++) {
                if (packet[index] != prefix[index]) {
                    errors.add(path + " does not match frame prefix");
                    return;
                }
            }
        }
        if ("crc8_maxim".equals(frame.path("checksum").asText())
                && crc8Maxim(packet, packet.length - 1) != Byte.toUnsignedInt(packet[packet.length - 1])) {
            errors.add(path + " fails crc8_maxim");
            return;
        }
        for (JsonNode assertion : operation.path("assertions")) {
            int offset = assertion.path("offset").asInt(-1);
            if (offset < 0 || offset >= packet.length
                    || Byte.toUnsignedInt(packet[offset]) != assertion.path("equals").asInt()) {
                errors.add(path + " fails byte assertion");
                return;
            }
        }
        for (JsonNode decoder : operation.path("decoders")) {
            String parameter = decoder.path("parameter").asText();
            if (!expected.path(parameter).isNumber()) {
                errors.add(path + ".expected requires numeric " + parameter);
                continue;
            }
            Double actual = decode(packet, decoder, path, errors);
            if (actual != null) {
                decodedValues.put(parameter, actual);
                if (Math.abs(actual - expected.path(parameter).doubleValue()) > 0.00001d) {
                    errors.add(path + ".expected." + parameter + " does not match decoded value");
                }
                if (actual < decoder.path("min").doubleValue() || actual > decoder.path("max").doubleValue()) {
                    errors.add(path + " decodes " + parameter + " outside its range");
                }
            }
        }
    }

    private Double decode(byte[] packet, JsonNode decoder, String path, List<String> errors) {
        String type = decoder.path("type").asText();
        int width = switch (type) {
            case "uint8", "int8" -> 1;
            case "uint16", "int16", "sfloat16" -> 2;
            case "uint32", "int32", "float32" -> 4;
            default -> 0;
        };
        int offset = decoder.path("offset").asInt(-1);
        if (offset < 0 || width == 0 || offset + width > packet.length) {
            errors.add(path + " packet is too short for decoder " + decoder.path("parameter").asText());
            return null;
        }
        ByteOrder order = "little".equals(decoder.path("endian").asText())
                ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        ByteBuffer buffer = ByteBuffer.wrap(packet, offset, width).order(order);
        double raw = switch (type) {
            case "uint8" -> Byte.toUnsignedInt(buffer.get());
            case "int8" -> buffer.get();
            case "uint16" -> Short.toUnsignedInt(buffer.getShort());
            case "int16" -> buffer.getShort();
            case "uint32" -> Integer.toUnsignedLong(buffer.getInt());
            case "int32" -> buffer.getInt();
            case "float32" -> buffer.getFloat();
            case "sfloat16" -> decodeSfloat16(Short.toUnsignedInt(buffer.getShort()));
            default -> Double.NaN;
        };
        double value = raw * decoder.path("scale").doubleValue() + decoder.path("add").doubleValue();
        if (!Double.isFinite(value)) {
            errors.add(path + " produces a non-finite value");
            return null;
        }
        return value;
    }

    private void validateRequiredSecrets(JsonNode node, List<String> errors) {
        if (node.isMissingNode()) {
            return;
        }
        if (!node.isArray() || node.size() > 8) {
            errors.add("$.requiredSecrets must be an array of at most 8 names");
            return;
        }
        Set<String> names = new HashSet<>();
        for (JsonNode value : node) {
            String name = value.asText();
            if (!value.isTextual() || !name.matches("[a-z][a-z0-9_]{0,31}") || !names.add(name)) {
                errors.add("$.requiredSecrets contains an invalid or duplicate name");
            }
        }
    }

    private void validateValues(JsonNode values, String parent, List<String> errors) {
        if (values.isMissingNode()) {
            return;
        }
        if (!values.isObject() || values.size() > 16) {
            errors.add(parent + ".values must be an object with at most 16 entries");
            return;
        }
        values.fields().forEachRemaining(entry -> {
            if (!entry.getKey().matches("\\d{1,3}") || !entry.getValue().isTextual()
                    || entry.getValue().asText().length() > 32) {
                errors.add(parent + ".values contains an invalid mapping");
            }
        });
    }

    private void validateGattAddress(JsonNode operation, String path, List<String> errors) {
        validateUuid(operation.path("serviceUuid"), path + ".serviceUuid", errors);
        validateUuid(operation.path("characteristicUuid"), path + ".characteristicUuid", errors);
    }

    private boolean validGoldenArray(JsonNode packets, List<String> errors) {
        if (!packets.isArray() || packets.isEmpty() || packets.size() > 32) {
            errors.add("$.goldenPackets must contain 1 to 32 entries");
            return false;
        }
        return true;
    }

    private String hash(JsonNode spec) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonicalJson(spec).getBytes(java.nio.charset.StandardCharsets.UTF_16LE)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    private JsonNode sort(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sorted = objectMapper.createObjectNode();
            List<String> names = new ArrayList<>();
            node.fieldNames().forEachRemaining(names::add);
            names.sort(Comparator.naturalOrder());
            names.forEach(name -> sorted.set(name, sort(node.get(name))));
            return sorted;
        }
        if (node.isArray()) {
            ArrayNode sorted = objectMapper.createArrayNode();
            node.forEach(value -> sorted.add(sort(value)));
            return sorted;
        }
        return node;
    }

    private static double decodeSfloat16(int value) {
        int mantissa = value & 0x0fff;
        if ((mantissa & 0x0800) != 0) {
            mantissa -= 0x1000;
        }
        int exponent = (value >> 12) & 0x0f;
        if ((exponent & 0x08) != 0) {
            exponent -= 0x10;
        }
        return mantissa * Math.pow(10, exponent);
    }

    private static int crc8Maxim(byte[] bytes, int length) {
        int crc = 0;
        for (int index = 0; index < length; index++) {
            int value = Byte.toUnsignedInt(bytes[index]);
            for (int bit = 0; bit < 8; bit++) {
                int mix = (crc ^ value) & 1;
                crc >>>= 1;
                if (mix != 0) {
                    crc ^= 0x8c;
                }
                value >>>= 1;
            }
        }
        return crc;
    }

    private static void rejectUnknown(JsonNode node, Set<String> allowed, String path, List<String> errors) {
        Iterator<String> names = node.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            if (!allowed.contains(name)) {
                errors.add(path + " contains unsupported field " + name);
            }
        }
    }

    private static int boundedInt(JsonNode node, int min, int max, String path, List<String> errors) {
        if (!node.canConvertToInt() || node.intValue() < min || node.intValue() > max) {
            errors.add(path + " must be an integer from " + min + " to " + max);
            return 0;
        }
        return node.intValue();
    }

    private static void requireFiniteNumber(JsonNode node, String path, List<String> errors) {
        if (!node.isNumber() || !Double.isFinite(node.doubleValue())) {
            errors.add(path + " must be a finite number");
        }
    }

    private static void validateUuid(JsonNode node, String path, List<String> errors) {
        try {
            UUID.fromString(node.asText());
        } catch (IllegalArgumentException exception) {
            errors.add(path + " must be a UUID");
        }
    }

    private static boolean validParameter(String value) {
        return value != null && value.matches("[a-z][a-z0-9_]{0,63}");
    }

    private static byte[] parseHex(JsonNode node, String path, boolean allowEmpty, List<String> errors) {
        if (!node.isTextual()) {
            errors.add(path + " must be hexadecimal text");
            return null;
        }
        String hex = node.asText();
        if ((!allowEmpty && hex.isEmpty()) || (hex.length() & 1) != 0 || !hex.matches("[0-9A-Fa-f]*")) {
            errors.add(path + " must be an even-length hexadecimal string");
            return null;
        }
        return HexFormat.of().parseHex(hex);
    }
}
