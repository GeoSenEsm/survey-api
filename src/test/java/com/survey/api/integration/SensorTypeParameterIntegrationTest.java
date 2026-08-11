package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.SensorParameterDefinitionCreateDto;
import com.survey.application.dtos.SensorParameterDefinitionDto;
import com.survey.application.dtos.SensorParameterDefinitionEditDto;
import com.survey.application.dtos.SensorTypeCreateDto;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.survey.application.dtos.SensorTypeParameterCreateDto;
import com.survey.application.dtos.SensorTypeParameterDto;
import com.survey.application.dtos.SensorTypeSettingDto;
import com.survey.application.dtos.SurveySensorDataSettingsDto;
import com.survey.application.dtos.SurveySensorDataSettingsWriteDto;
import com.survey.application.dtos.UseSensorTypeParameterDto;
import com.survey.domain.models.IdentityUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end coverage of the raw-parameter-catalog -> "use" (promotion) -> single-row-edit flow
 * that replaced the old bulk parameter PUT (see {@link SurveySettingsServiceImplTest} for the
 * unit-level uniqueness checks).
 */
@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
class SensorTypeParameterIntegrationTest {
    private static final String ADMIN_PASSWORD = "testAdminPassword";

    private final WebTestClient webTestClient;
    private final TestUtils testUtils;

    @Autowired
    SensorTypeParameterIntegrationTest(WebTestClient webTestClient, TestUtils testUtils) {
        this.webTestClient = webTestClient;
        this.testUtils = testUtils;
    }

    @Test
    void rawParameter_canBePromotedIntoUsedSensorDataAndThenEdited() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        SensorTypeDtoOut sensorType = webTestClient.post()
                .uri("/api/sensorprofiles/types")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeCreateDto("promote_" + suffix, "Promote " + suffix, "profile", null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeDtoOut.class)
                .returnResult()
                .getResponseBody();

        SensorTypeParameterDto rawParameter = webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeParameterCreateDto("raw_temp_" + suffix, "Raw Temp", "decimal", "C"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(rawParameter.usedParameterId()).isNull();

        String usedName = "Temperature " + suffix;
        SensorTypeParameterDto usedRawParameter = webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters/" + rawParameter.id() + "/use")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new UseSensorTypeParameterDto(null, usedName, "decimal", "C", true))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(usedRawParameter.usedParameterId()).isNotNull();

        SurveySensorDataSettingsDto settings = webTestClient.get()
                .uri("/api/surveysettings/sensordata")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SurveySensorDataSettingsDto.class)
                .returnResult()
                .getResponseBody();

        SensorParameterDefinitionDto usedParameter = settings.parameters().stream()
                .filter(p -> p.id().equals(usedRawParameter.usedParameterId()))
                .findFirst()
                .orElseThrow();
        assertThat(usedParameter.name()).isEqualTo(usedName);
        assertThat(usedParameter.sources()).hasSize(1);
        assertThat(usedParameter.sources().get(0).sensorTypeCode()).isEqualTo(sensorType.getCode());
        assertThat(usedParameter.sources().get(0).rawParameterCode()).isEqualTo("raw_temp_" + suffix);

        // A harmless rename (no conflict) should succeed.
        webTestClient.put()
                .uri("/api/surveysettings/sensordata/parameters/" + usedParameter.id())
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorParameterDefinitionEditDto(
                        "Renamed " + suffix, "decimal", "C2", true, usedParameter.displayOrder()))
                .exchange()
                .expectStatus().isOk();

        webTestClient.post()
                .uri("/api/surveysettings/sensordata/parameters")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorParameterDefinitionCreateDto(
                        "other_" + suffix, "Other " + suffix, "decimal", "x", false))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.put()
                .uri("/api/surveysettings/sensordata/parameters/" + usedParameter.id())
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorParameterDefinitionEditDto(
                        "Other " + suffix, "decimal", "x", true, usedParameter.displayOrder()))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("already used"));
    }

    @Test
    void disablingSensorType_removesItsSourceAndDeletesParameterLeftWithNoSources() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        SensorTypeDtoOut sensorType = webTestClient.post()
                .uri("/api/sensorprofiles/types")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeCreateDto("disable_" + suffix, "Disable " + suffix, "profile", null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeDtoOut.class)
                .returnResult()
                .getResponseBody();

        SensorTypeParameterDto rawParameter = webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeParameterCreateDto("raw_temp_" + suffix, "Raw Temp", "decimal", "C"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();

        SensorTypeParameterDto usedRawParameter = webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters/" + rawParameter.id() + "/use")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new UseSensorTypeParameterDto(null, "Temperature " + suffix, "decimal", "C", true))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SensorTypeParameterDto.class)
                .returnResult()
                .getResponseBody();
        UUID usedParameterId = usedRawParameter.usedParameterId();
        assertThat(usedParameterId).isNotNull();

        SurveySensorDataSettingsDto beforeDisable = getSensorDataSettings(adminToken);
        assertThat(beforeDisable.parameters()).anyMatch(p -> usedParameterId.equals(p.id()));

        List<SensorTypeSettingDto> disabledSensorTypes = beforeDisable.sensorTypes().stream()
                .map(setting -> setting.sensorTypeCode().equals(sensorType.getCode())
                        ? disable(setting)
                        : setting)
                .toList();

        webTestClient.put()
                .uri("/api/surveysettings/sensordata")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SurveySensorDataSettingsWriteDto(beforeDisable.mode(), disabledSensorTypes))
                .exchange()
                .expectStatus().isOk();

        SurveySensorDataSettingsDto afterDisable = getSensorDataSettings(adminToken);
        assertThat(afterDisable.parameters()).noneMatch(p -> usedParameterId.equals(p.id()));
    }

    private SurveySensorDataSettingsDto getSensorDataSettings(String adminToken) {
        return webTestClient.get()
                .uri("/api/surveysettings/sensordata")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SurveySensorDataSettingsDto.class)
                .returnResult()
                .getResponseBody();
    }

    private static SensorTypeSettingDto disable(SensorTypeSettingDto setting) {
        return new SensorTypeSettingDto(
                setting.id(),
                setting.sensorTypeCode(),
                setting.sensorTypeName(),
                setting.integrationMode(),
                setting.adapterKey(),
                false,
                setting.connectionTimeoutSeconds(),
                setting.displayOrder());
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
