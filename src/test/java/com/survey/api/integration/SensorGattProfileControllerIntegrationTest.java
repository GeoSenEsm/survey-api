package com.survey.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.GattProfileValidationDto;
import com.survey.application.dtos.SensorGattProfileDto;
import com.survey.application.dtos.SensorGattProfileWriteDto;
import com.survey.application.dtos.SensorTypeCreateDto;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.survey.application.dtos.AssignSensorRespondentDto;
import com.survey.application.dtos.MobileSensorSetupDto;
import com.survey.application.dtos.SensorDeviceSecretWriteDto;
import com.survey.application.services.SensorProfileTemplateService;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SensorMac;
import com.survey.domain.repository.SensorDeviceSecretRepository;
import com.survey.domain.repository.SensorMacRepository;
import com.survey.domain.repository.SensorTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "ADMIN_USER_PASSWORD=testAdminPassword",
        "sensor.secret-encryption-key=MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY="
})
@AutoConfigureWebTestClient
class SensorGattProfileControllerIntegrationTest {
    private final WebTestClient webTestClient;
    private final TestUtils testUtils;
    private final ObjectMapper objectMapper;
    private final SensorTypeRepository sensorTypeRepository;
    private final SensorMacRepository sensorMacRepository;
    private final SensorDeviceSecretRepository secretRepository;
    private final SensorProfileTemplateService templateService;

    @Autowired
    public SensorGattProfileControllerIntegrationTest(
            WebTestClient webTestClient,
            TestUtils testUtils,
            ObjectMapper objectMapper,
            SensorTypeRepository sensorTypeRepository,
            SensorMacRepository sensorMacRepository,
            SensorDeviceSecretRepository secretRepository,
            SensorProfileTemplateService templateService) {
        this.webTestClient = webTestClient;
        this.testUtils = testUtils;
        this.objectMapper = objectMapper;
        this.sensorTypeRepository = sensorTypeRepository;
        this.sensorMacRepository = sensorMacRepository;
        this.secretRepository = secretRepository;
        this.templateService = templateService;
    }

    @Test
    void bindKey_isWriteOnlyEncryptedAndRespondentScoped() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), "testAdminPassword");
        IdentityUser assigned = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), "assignedPassword");
        IdentityUser other = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), "otherPassword");
        String adminToken = testUtils.authenticateAndGenerateToken(admin, "testAdminPassword");
        String assignedToken = testUtils.authenticateAndGenerateToken(assigned, "assignedPassword");
        String otherToken = testUtils.authenticateAndGenerateToken(other, "otherPassword");
        UUID typeId = sensorTypeRepository.findByCode("xiaomi_door_sensor_2")
                .orElseGet(() -> {
                    templateService.install("xiaomi_door_sensor_2");
                    return sensorTypeRepository.findByCode("xiaomi_door_sensor_2").orElseThrow();
                })
                .getId();

        SensorMac sensor = new SensorMac();
        sensor.setId(UUID.randomUUID());
        sensor.setSensorId("door-" + UUID.randomUUID().toString().substring(0, 6));
        sensor.setSensorMac("AA:BB:CC:DD:EE:" + String.format("%02X", Math.abs(sensor.hashCode()) % 255));
        sensor.setSensorTypeId(typeId);
        // row_version is DB-generated and insertable=false; a manually-assigned id combined with a
        // null version makes Hibernate treat this as a stale detached entity instead of a new one,
        // so seed a placeholder (see SensorMacControllerIntegrationTest#saveSensorMacListDirectly).
        sensor.setRowVersion(new byte[8]);
        sensor = sensorMacRepository.save(sensor);

        webTestClient.put()
                .uri("/api/sensormac/{sensorId}/respondent", sensor.getSensorId())
                .header("Authorization", bearer(adminToken))
                .bodyValue(new AssignSensorRespondentDto(assigned.getId()))
                .exchange()
                .expectStatus().isOk();

        String bindKey = "00112233445566778899AABBCCDDEEFF";
        webTestClient.put()
                .uri("/api/sensorprofiles/devices/{sensorId}/secrets/bind_key", sensor.getId())
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorDeviceSecretWriteDto(bindKey))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        var stored = secretRepository.findBySensorMacIdAndSecretName(sensor.getId(), "bind_key").orElseThrow();
        // AssertJ's byte[] "contains" checks element membership, not subsequence order, so it would
        // spuriously fail whenever any single plaintext byte value happens to also occur among the
        // random ciphertext bytes. Check for the plaintext appearing as a literal run of bytes instead.
        assertThat(indexOfSubsequence(stored.getCiphertext(), bindKey.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .as("ciphertext should not contain the plaintext bind key")
                .isLessThan(0);

        MobileSensorSetupDto assignedSetup = webTestClient.get()
                .uri("/api/surveysettings/sensordata/mobile")
                .header("Authorization", bearer(assignedToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(MobileSensorSetupDto.class)
                .returnResult().getResponseBody();
        assertThat(assignedSetup).isNotNull();
        assertThat(assignedSetup.deviceSecrets()).singleElement()
                .satisfies(device -> assertThat(device.secrets()).containsEntry("bind_key", bindKey));

        MobileSensorSetupDto otherSetup = webTestClient.get()
                .uri("/api/surveysettings/sensordata/mobile")
                .header("Authorization", bearer(otherToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(MobileSensorSetupDto.class)
                .returnResult().getResponseBody();
        assertThat(otherSetup).isNotNull();
        assertThat(otherSetup.deviceSecrets()).isEmpty();
    }

    @Test
    void lifecycle_requiresAdminAndPreservesRevisionHistory() throws Exception {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), "testAdminPassword");
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), "respondentPassword");
        String adminToken = testUtils.authenticateAndGenerateToken(admin, "testAdminPassword");
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, "respondentPassword");
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        SensorTypeDtoOut type = webTestClient.post()
                .uri("/api/sensorprofiles/types")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeCreateDto("test_" + suffix, "Test " + suffix, "profile", null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeDtoOut.class)
                .returnResult().getResponseBody();
        assertThat(type).isNotNull();
        assertThat(type.getIntegrationMode()).isEqualTo("profile");

        SensorGattProfileWriteDto request = new SensorGattProfileWriteDto(validSpec(), "1.0.0");
        SensorGattProfileDto draft = webTestClient.post()
                .uri("/api/sensorprofiles/{sensorTypeId}/drafts", type.getId())
                .header("Authorization", bearer(adminToken))
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorGattProfileDto.class)
                .returnResult().getResponseBody();
        assertThat(draft).isNotNull();
        assertThat(draft.status()).isEqualTo("draft");
        assertThat(draft.revision()).isEqualTo(1);

        GattProfileValidationDto validation = webTestClient.post()
                .uri("/api/sensorprofiles/{profileId}/validate", draft.id())
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(GattProfileValidationDto.class)
                .returnResult().getResponseBody();
        assertThat(validation).isNotNull();
        assertThat(validation.valid()).isTrue();
        assertThat(validation.canonicalHash()).isEqualTo(draft.specHash());

        SensorGattProfileDto published = webTestClient.post()
                .uri("/api/sensorprofiles/{profileId}/publish", draft.id())
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SensorGattProfileDto.class)
                .returnResult().getResponseBody();
        assertThat(published).isNotNull();
        assertThat(published.status()).isEqualTo("published");

        SensorGattProfileDto rollback = webTestClient.post()
                .uri("/api/sensorprofiles/{sensorTypeId}/rollback/{revision}", type.getId(), 1)
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SensorGattProfileDto.class)
                .returnResult().getResponseBody();
        assertThat(rollback).isNotNull();
        assertThat(rollback.revision()).isEqualTo(2);
        assertThat(rollback.status()).isEqualTo("published");

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/sensorprofiles")
                        .queryParam("sensorTypeId", type.getId())
                        .build())
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SensorGattProfileDto.class)
                .hasSize(2);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/sensorprofiles")
                        .queryParam("sensorTypeId", type.getId())
                        .build())
                .header("Authorization", bearer(respondentToken))
                .exchange()
                .expectStatus().isForbidden();
    }

    private JsonNode validSpec() throws Exception {
        return objectMapper.readTree("""
                {
                  "schemaVersion":1,
                  "transport":"gatt_sequence",
                  "requiredSecrets":[],
                  "discovery":{
                    "nameExact":"TEST",
                    "serviceUuid":"12630000-cc25-497d-9854-9b6c02c77054"
                  },
                  "operations":[{
                    "kind":"acquire",
                    "serviceUuid":"12630000-cc25-497d-9854-9b6c02c77054",
                    "characteristicUuid":"12630001-cc25-497d-9854-9b6c02c77054",
                    "acquisition":{"mode":"read","timeoutMs":5000,"maxPackets":1},
                    "frame":{"length":3,"prefixHex":"07","checksum":"none"},
                    "assertions":[{"offset":0,"equals":7}],
                    "decoders":[{
                      "parameter":"temperature","type":"uint16","offset":1,"endian":"little",
                      "scale":0.01,"add":0,"min":-40,"max":125
                    }]
                  }],
                  "goldenPackets":[{
                    "characteristicUuid":"12630001-cc25-497d-9854-9b6c02c77054",
                    "packetHex":"073408",
                    "expected":{"temperature":21}
                  }]
                }
                """);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static int indexOfSubsequence(byte[] haystack, byte[] needle) {
        outer:
        for (int start = 0; start <= haystack.length - needle.length; start++) {
            for (int offset = 0; offset < needle.length; offset++) {
                if (haystack[start + offset] != needle[offset]) {
                    continue outer;
                }
            }
            return start;
        }
        return -1;
    }
}
