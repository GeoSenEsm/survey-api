package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.AssignSensorRespondentDto;
import com.survey.application.dtos.SensorDataDto;
import com.survey.application.dtos.SensorDataValueDto;
import com.survey.application.dtos.SensorMacDtoIn;
import com.survey.application.dtos.SensorMacDtoOut;
import com.survey.application.dtos.SensorParameterDefinitionCreateDto;
import com.survey.application.dtos.SensorTypeCreateDto;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.survey.application.dtos.SensorTypeParameterCreateDto;
import com.survey.application.dtos.SensorTypeParameterDto;
import com.survey.application.dtos.SurveySensorDataSettingsDto;
import com.survey.application.dtos.SurveySensorDataSettingsWriteDto;
import com.survey.application.dtos.UseSensorTypeParameterDto;
import com.survey.application.dtos.initialSurvey.CreateInitialSurveyOptionDto;
import com.survey.application.dtos.initialSurvey.CreateInitialSurveyQuestionDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.InitialSurveyRepository;
import com.survey.domain.repository.SensorDataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Sensor data setup (sensor types, GATT profiles, and the survey-settings sensor data endpoint)
 * must stop accepting changes once the initial survey is published, since that is the point a
 * study is considered live. The initial survey state is a global singleton shared by every
 * integration test in this suite, so this class defensively resets it before and after each test
 * rather than relying on test ordering.
 */
@ExtendWith(IntegrationTestDatabaseInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "ADMIN_USER_PASSWORD=testAdminPassword")
@AutoConfigureWebTestClient
class SensorDataSetupLockIntegrationTest {
    private static final String ADMIN_PASSWORD = "testAdminPassword";

    private final WebTestClient webTestClient;
    private final TestUtils testUtils;
    private final InitialSurveyRepository initialSurveyRepository;
    private final SensorDataRepository sensorDataRepository;

    @Autowired
    SensorDataSetupLockIntegrationTest(
            WebTestClient webTestClient,
            TestUtils testUtils,
            InitialSurveyRepository initialSurveyRepository,
            SensorDataRepository sensorDataRepository) {
        this.webTestClient = webTestClient;
        this.testUtils = testUtils;
        this.initialSurveyRepository = initialSurveyRepository;
        this.sensorDataRepository = sensorDataRepository;
    }

    @BeforeEach
    @AfterEach
    void resetInitialSurvey() {
        initialSurveyRepository.deleteAll();
        sensorDataRepository.deleteAll();
    }

    @Test
    void createSensorType_isRejectedOnceTheInitialSurveyIsPublished() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        webTestClient.post()
                .uri("/api/sensorprofiles/types")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeCreateDto("before_publish_" + suffix, "Before publish " + suffix, "profile", null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeDtoOut.class);

        publishInitialSurvey(adminToken);

        webTestClient.post()
                .uri("/api/sensorprofiles/types")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeCreateDto("after_publish_" + suffix, "After publish " + suffix, "profile", null))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("already been published"));
    }

    @Test
    void updateSensorDataSettings_isRejectedOnceTheInitialSurveyIsPublished() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        publishInitialSurvey(adminToken);

        webTestClient.put()
                .uri("/api/surveysettings/sensordata")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SurveySensorDataSettingsWriteDto("no_sensor_data", List.of()))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("already been published"));
    }

    @Test
    void updateSensorDataSettings_isRejectedOnceSensorDataHasBeenCollected() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), "irrelevant");
        String respondentToken = testUtils.authenticateAndGenerateToken(respondent, "irrelevant");
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String parameterCode = "locktemp_" + suffix;

        webTestClient.post()
                .uri("/api/surveysettings/sensordata/parameters")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorParameterDefinitionCreateDto(
                        parameterCode, "Lock Temp " + suffix, "decimal", "C"))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/sensordata")
                .header("Authorization", bearer(respondentToken))
                .bodyValue(List.of(new SensorDataDto(
                        OffsetDateTime.now(ZoneOffset.UTC),
                        "manual",
                        List.of(new SensorDataValueDto(parameterCode, "21.0")))))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.put()
                .uri("/api/surveysettings/sensordata")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SurveySensorDataSettingsWriteDto("no_sensor_data", List.of()))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("already been collected"));
    }

    @Test
    void sensorTypeParameterCatalog_isRejectedOnceTheInitialSurveyIsPublished() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        SensorTypeDtoOut sensorType = webTestClient.post()
                .uri("/api/sensorprofiles/types")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeCreateDto("locktest_" + suffix, "Lock test " + suffix, "profile", null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SensorTypeDtoOut.class)
                .returnResult()
                .getResponseBody();

        publishInitialSurvey(adminToken);

        webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeParameterCreateDto("raw_temp", "Raw Temp", "decimal", "C"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("already been published"));
    }

    @Test
    void useSensorTypeParameter_isRejectedOnceTheInitialSurveyIsPublished() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        SensorTypeDtoOut sensorType = webTestClient.post()
                .uri("/api/sensorprofiles/types")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorTypeCreateDto("uselock_" + suffix, "Use lock " + suffix, "profile", null))
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

        publishInitialSurvey(adminToken);

        webTestClient.post()
                .uri("/api/sensorprofiles/types/" + sensorType.getId() + "/parameters/" + rawParameter.id() + "/use")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new UseSensorTypeParameterDto(null, "Temperature " + suffix, "decimal", "C"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("already been published"));
    }

    @Test
    void createSensorParameterDefinition_isRejectedOnceTheInitialSurveyIsPublished() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        publishInitialSurvey(adminToken);

        webTestClient.post()
                .uri("/api/surveysettings/sensordata/parameters")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new SensorParameterDefinitionCreateDto(
                        "locktest_" + suffix, "Lock test " + suffix, "decimal", null))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("already been published"));
    }

    @Test
    void installTemplate_isRejectedOnceTheInitialSurveyIsPublished() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);

        publishInitialSurvey(adminToken);

        webTestClient.post()
                .uri("/api/sensorprofiles/templates/xiaomi/install")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("already been published"));
    }

    @Test
    void assignRespondent_isAllowedEvenAfterTheInitialSurveyIsPublished() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), "irrelevant");
        UUID xiaomiTypeId = testUtils.getOrCreateXiaomiSensorType().getId();
        String sensorId = "lock-" + UUID.randomUUID().toString().substring(0, 8);

        webTestClient.post()
                .uri("/api/sensormac")
                .header("Authorization", bearer(adminToken))
                .bodyValue(List.of(new SensorMacDtoIn(sensorId, null, xiaomiTypeId)))
                .exchange()
                .expectStatus().isCreated();

        publishInitialSurvey(adminToken);

        // Which physical sensor a respondent has keeps changing throughout a live study, so
        // assignment (unlike the mode/sensor-type-catalog endpoints above) must stay unlocked.
        webTestClient.put()
                .uri("/api/sensormac/{sensorId}/respondent", sensorId)
                .header("Authorization", bearer(adminToken))
                .bodyValue(new AssignSensorRespondentDto(respondent.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SensorMacDtoOut.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body.getRespondentId())
                        .isEqualTo(respondent.getId()));
    }

    private void publishInitialSurvey(String adminToken) {
        CreateInitialSurveyOptionDto option = new CreateInitialSurveyOptionDto(0, "yes");
        CreateInitialSurveyQuestionDto question = new CreateInitialSurveyQuestionDto(0, "Consent", List.of(option));

        webTestClient.post()
                .uri("/api/initialsurvey")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(question))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.patch()
                .uri("/api/initialsurvey/publish")
                .header("Authorization", bearer(adminToken))
                .exchange()
                .expectStatus().isNoContent();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
