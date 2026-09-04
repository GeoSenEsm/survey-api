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
import com.survey.application.dtos.SensorTypeParameterCreateDto;
import com.survey.domain.models.IdentityUser;
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
        "ADMIN_USER_PASSWORD=testAdminPassword"
})
@AutoConfigureWebTestClient
class SensorGattProfileControllerIntegrationTest {
    private final WebTestClient webTestClient;
    private final TestUtils testUtils;
    private final ObjectMapper objectMapper;

    @Autowired
    public SensorGattProfileControllerIntegrationTest(
            WebTestClient webTestClient,
            TestUtils testUtils,
            ObjectMapper objectMapper) {
        this.webTestClient = webTestClient;
        this.testUtils = testUtils;
        this.objectMapper = objectMapper;
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

        webTestClient.post()
                .uri("/api/sensorprofiles/types/{sensorTypeId}/parameters", type.getId())
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeParameterCreateDto("temperature", "Temperature", "decimal", null))
                .exchange()
                .expectStatus().isCreated();

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
}
