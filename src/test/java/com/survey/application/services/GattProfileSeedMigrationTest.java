package com.survey.application.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class GattProfileSeedMigrationTest {
    private static final Pattern PROFILE_ROW =
            Pattern.compile("\\(N'([^']+)', N'(\\{.*?\\})'\\)(?:,|;)", Pattern.DOTALL);

    @Test
    void migrationSeedsValidateAndCoverAllBuiltInBleProfiles() throws IOException {
        String migration;
        try (var stream = getClass().getResourceAsStream(
                "/db/migration/V31__create_gatt_profile_engine.sql")) {
            assertThat(stream).isNotNull();
            migration = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        GattProfileValidator validator = new GattProfileValidator(objectMapper);
        Map<String, JsonNode> profiles = new HashMap<>();
        Matcher matcher = PROFILE_ROW.matcher(migration);
        while (matcher.find()) {
            JsonNode profile = objectMapper.readTree(matcher.group(2));
            assertThat(validator.validate(profile).errors())
                    .as("seed profile %s", matcher.group(1))
                    .isEmpty();
            assertThat(validator.canonicalJson(profile))
                    .as("canonical seed profile %s", matcher.group(1))
                    .isEqualTo(matcher.group(2));
            profiles.put(matcher.group(1), profile);
        }

        assertThat(profiles).containsOnlyKeys(
                "xiaomi", "kestrel", "pc_60fw", "bluetooth_sig_plx",
                "flower_care", "xiaomi_door_sensor_2");
        assertThat(profiles.get("pc_60fw").at("/operations/0/acquisition/mode").asText())
                .isEqualTo("notification");
        assertThat(profiles.get("bluetooth_sig_plx").at("/operations/0/decoders/1/type").asText())
                .isEqualTo("sfloat16");
        assertThat(profiles.get("flower_care").at("/operations/0/payloadHex").asText())
                .isEqualTo("A01F");
        assertThat(profiles.get("flower_care").at("/operations/1/durationMs").asInt())
                .isBetween(500, 1000);
        assertThat(profiles.get("xiaomi_door_sensor_2").at("/advertisement/decoderId").asText())
                .isEqualTo("xiaomi_mibeacon_v4_v5");
        assertThat(profiles.get("xiaomi_door_sensor_2").at("/requiredSecrets/0").asText())
                .isEqualTo("bind_key");
    }
}
