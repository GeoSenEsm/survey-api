package com.survey.api.integration;

import com.survey.api.TestUtils;
import com.survey.api.security.Role;
import com.survey.application.dtos.RespondentSensorAssignmentDto;
import com.survey.application.dtos.RespondentSensorAssignmentsUpdateDto;
import com.survey.application.dtos.SensorTypeCreateDto;
import com.survey.application.dtos.SensorTypeDtoOut;
import com.survey.application.dtos.SurveySensorDataSettingsDto;
import com.survey.application.dtos.SurveySensorDataSettingsWriteDto;
import com.survey.application.dtos.initialSurvey.CreateInitialSurveyOptionDto;
import com.survey.application.dtos.initialSurvey.CreateInitialSurveyQuestionDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.InitialSurveyRepository;
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

    @Autowired
    SensorDataSetupLockIntegrationTest(
            WebTestClient webTestClient,
            TestUtils testUtils,
            InitialSurveyRepository initialSurveyRepository) {
        this.webTestClient = webTestClient;
        this.testUtils = testUtils;
        this.initialSurveyRepository = initialSurveyRepository;
    }

    @BeforeEach
    @AfterEach
    void resetInitialSurvey() {
        initialSurveyRepository.deleteAll();
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
                .bodyValue(new SurveySensorDataSettingsWriteDto("no_sensor_data", List.of(), List.of()))
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
    void updateAssignments_isAllowedEvenAfterTheInitialSurveyIsPublished() {
        IdentityUser admin = testUtils.createUserWithRole(Role.ADMIN.getRoleName(), ADMIN_PASSWORD);
        String adminToken = testUtils.authenticateAndGenerateToken(admin, ADMIN_PASSWORD);
        IdentityUser respondent = testUtils.createUserWithRole(Role.RESPONDENT.getRoleName(), "irrelevant");

        publishInitialSurvey(adminToken);

        RespondentSensorAssignmentDto assignment = new RespondentSensorAssignmentDto(
                null, respondent.getId(), respondent.getUsername(), "manual", null, null, null, null, true, 0);

        webTestClient.put()
                .uri("/api/surveysettings/sensordata/assignments")
                .header("Authorization", bearer(adminToken))
                .bodyValue(new RespondentSensorAssignmentsUpdateDto(List.of(assignment)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SurveySensorDataSettingsDto.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body.assignments())
                        .extracting(RespondentSensorAssignmentDto::respondentId)
                        .containsExactly(respondent.getId()));
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
