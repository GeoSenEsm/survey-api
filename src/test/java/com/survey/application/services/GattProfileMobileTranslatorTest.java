package com.survey.application.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.domain.models.SensorGattProfile;
import com.survey.domain.models.SensorType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every seeded profile must not just pass backend validation, it must also survive translation
 * into the shape the mobile GATT engine parses. This guards against contract drift between
 * {@link GattProfileValidator} (backend spec) and the mobile {@code GattProfile.fromJson} model,
 * which has stricter limits (e.g. discovery cannot carry both an exact name and a prefix, and
 * reads/actions are capped at 16 each).
 */
class GattProfileMobileTranslatorTest {
    private static final Pattern PROFILE_ROW =
            Pattern.compile("\\(N'([^']+)', N'(\\{.*?\\})'\\)(?:,|;)", Pattern.DOTALL);
    private static final int MOBILE_MAX_READS = 16;
    private static final int MOBILE_MAX_ACTIONS = 16;
    private static final int MOBILE_MAX_ASSERTIONS_PER_READ = 16;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GattProfileMobileTranslator translator = new GattProfileMobileTranslator(objectMapper);

    @Test
    void translatedSeedProfilesSatisfyMobileContract() throws IOException {
        for (Map.Entry<String, JsonNode> entry : loadSeedProfiles().entrySet()) {
            JsonNode mobile = translator.translate(toEntity(entry.getKey(), entry.getValue())).spec();

            JsonNode discovery = mobile.path("discovery");
            assertThat(discovery.has("exactName") && discovery.has("namePrefix"))
                    .as("seed profile %s: mobile discovery must not set both exactName and namePrefix",
                            entry.getKey())
                    .isFalse();

            assertThat(mobile.path("reads").size())
                    .as("seed profile %s: mobile reads must fit the %d-read limit",
                            entry.getKey(), MOBILE_MAX_READS)
                    .isLessThanOrEqualTo(MOBILE_MAX_READS);
            assertThat(mobile.path("actions").size())
                    .as("seed profile %s: mobile actions must fit the %d-action limit",
                            entry.getKey(), MOBILE_MAX_ACTIONS)
                    .isLessThanOrEqualTo(MOBILE_MAX_ACTIONS);

            for (JsonNode read : mobile.path("reads")) {
                assertThat(read.path("assertions").size())
                        .as("seed profile %s: each mobile read must fit the %d-assertion limit",
                                entry.getKey(), MOBILE_MAX_ASSERTIONS_PER_READ)
                        .isLessThanOrEqualTo(MOBILE_MAX_ASSERTIONS_PER_READ);
                assertThat(read.path("fields")).as("seed profile %s: mobile reads require fields",
                        entry.getKey()).isNotEmpty();
            }

            if ("ble_advertisement".equals(mobile.path("transport").asText())) {
                assertThat(mobile.path("advertisement").path("objects"))
                        .as("seed profile %s: mobile advertisement definitions require object mappings",
                                entry.getKey())
                        .isNotEmpty();
            }
        }
    }

    @Test
    void translatedAdvertisementObjectsCarryTheirScaleFactor() throws IOException {
        // Uses a synthetic spec rather than a catalog template: this test's only concern is
        // whether the translator propagates `scale`, independent of which sensor type happens to
        // use ble_advertisement (the xiaomi template moved to gatt_sequence after discovering real
        // LYWSD03MMC units broadcast encrypted, undecodable MiBeacon frames).
        JsonNode spec = objectMapper.readTree(
                "{\"schemaVersion\":1,\"transport\":\"ble_advertisement\",\"requiredSecrets\":[],"
                        + "\"advertisement\":{\"decoderId\":\"xiaomi_mibeacon_v4_v5\",\"matcher\":{\"productId\":1},"
                        + "\"objects\":[{\"objectId\":\"0x1004\",\"parameter\":\"temperature\",\"type\":\"int16\",\"scale\":0.1},"
                        + "{\"objectId\":\"0x1006\",\"parameter\":\"humidity\",\"type\":\"uint16\",\"scale\":0.1}]},"
                        + "\"goldenPackets\":[{\"advertisementHex\":\"0050010001000000000000041002D700061002C201\","
                        + "\"expected\":{\"humidity\":45.0,\"temperature\":21.5}}]}");
        JsonNode mobile = translator.translate(toEntity("synthetic_advertisement", spec)).spec();

        JsonNode objects = mobile.path("advertisement").path("objects");
        assertThat(objects).isNotEmpty();
        for (JsonNode object : objects) {
            if ("temperature".equals(object.path("parameterCode").asText())
                    || "humidity".equals(object.path("parameterCode").asText())) {
                assertThat(object.path("scale").asDouble())
                        .as("scale must survive translation for %s", object.path("parameterCode").asText())
                        .isEqualTo(0.1);
            }
        }
    }

    private Map<String, JsonNode> loadSeedProfiles() throws IOException {
        String migration;
        try (var stream = getClass().getResourceAsStream(
                "/db/migration/V31__create_gatt_profile_engine.sql")) {
            assertThat(stream).isNotNull();
            migration = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        Map<String, JsonNode> profiles = new HashMap<>();
        Matcher matcher = PROFILE_ROW.matcher(migration);
        while (matcher.find()) {
            profiles.put(matcher.group(1), objectMapper.readTree(matcher.group(2)));
        }
        assertThat(profiles).isNotEmpty();
        return profiles;
    }

    private SensorGattProfile toEntity(String code, JsonNode spec) {
        SensorType sensorType = new SensorType();
        sensorType.setId(UUID.randomUUID());
        sensorType.setCode(code);
        sensorType.setName(code);
        sensorType.setIntegrationMode("profile");

        SensorGattProfile entity = new SensorGattProfile();
        entity.setId(UUID.randomUUID());
        entity.setSensorType(sensorType);
        entity.setRevision(1);
        entity.setStatus("published");
        entity.setSchemaVersion(1);
        entity.setSpecJson(spec.toString());
        entity.setMinEngineVersion("1.0.0");
        return entity;
    }
}
