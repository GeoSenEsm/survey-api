package com.survey.application.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The built-in BLE profiles used to be seeded directly by V31/V33; they now live as installable
 * templates in {@link SensorProfileTemplateCatalog} (see {@code V35__purge_seeded_sensor_profile_templates.sql}).
 * This validates that catalog against the same rules the admin-authored JSON profiles must satisfy.
 */
class GattProfileSeedMigrationTest {

    @Test
    void catalogTemplatesValidateAndCoverAllBuiltInBleProfiles() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        GattProfileValidator validator = new GattProfileValidator(objectMapper);

        Map<String, JsonNode> profiles = SensorProfileTemplateCatalog.all().stream()
                .collect(Collectors.toMap(SensorProfileTemplate::code, template -> {
                    try {
                        return objectMapper.readTree(template.specJson());
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                }));

        for (SensorProfileTemplate template : SensorProfileTemplateCatalog.all()) {
            JsonNode profile = profiles.get(template.code());
            assertThat(validator.validate(profile).errors())
                    .as("template %s", template.code())
                    .isEmpty();
        }

        assertThat(profiles).containsOnlyKeys(
                "xiaomi", "kestrel", "pc_60fw", "bluetooth_sig_plx",
                "flower_care", "xiaomi_door_sensor_2", "inkbird_ibs_th1");
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
        assertThat(profiles.get("inkbird_ibs_th1").at("/operations/0/decoders/0/type").asText())
                .isEqualTo("int16");
    }
}
